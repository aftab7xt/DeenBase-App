package com.deenbase.app.features.quran.data

import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class SurahInfo(
    val id: Int,
    val nameArabic: String,
    val nameTransliteration: String,
    val nameEnglish: String,
    val type: String,
    val totalVerses: Int
)

data class Verse(
    val id: Int,
    val surahId: Int,
    val verseNumber: Int,
    val arabicText: String,
    val translationText: String,
    val juz: Int = 0,
    val surahName: String = "",
    val surahNameArabic: String = ""
)

// Bismillah in Uthmani script — shown as a decorative header, not as a verse
const val BISMILLAH = "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ"

// Surahs where Bismillah is NOT shown as a header:
// 1 (Al-Fatiha) — it IS verse 1 so we show it as a verse naturally
// 9 (At-Tawbah) — no Bismillah at all
val SURAH_NO_BISMILLAH_HEADER = setOf(1, 9)

class QuranRepository {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val baseUrl = "https://api.alquran.cloud/v1"

    private fun translationEdition(lang: String) =
        if (lang == "urdu") "ur.jalandhry" else "en.sahih"

    fun getSurahsList(): List<SurahInfo> {
        val request = Request.Builder().url("$baseUrl/surah").build()
        val response = client.newCall(request).execute()
        val body = response.body?.string() ?: return emptyList()
        val data = JSONObject(body).getJSONArray("data")
        val surahs = mutableListOf<SurahInfo>()
        for (i in 0 until data.length()) {
            val obj = data.getJSONObject(i)
            surahs.add(
                SurahInfo(
                    id = obj.getInt("number"),
                    nameArabic = obj.getString("name"),
                    nameTransliteration = obj.getString("englishName"),
                    nameEnglish = obj.getString("englishNameTranslation"),
                    type = obj.getString("revelationType"),
                    totalVerses = obj.getInt("numberOfAyahs")
                )
            )
        }
        return surahs
    }

    fun getVersesForSurah(surahId: Int, lang: String): List<Verse> {
        val edition = translationEdition(lang)
        val url = "$baseUrl/surah/$surahId/editions/quran-uthmani,$edition"
        val request = Request.Builder().url(url).build()
        val response = client.newCall(request).execute()
        val body = response.body?.string() ?: return emptyList()

        val data = JSONObject(body).getJSONArray("data")
        val arabicAyahs = data.getJSONObject(0).getJSONArray("ayahs")
        val transAyahs  = data.getJSONObject(1).getJSONArray("ayahs")

        val verses = mutableListOf<Verse>()
        for (i in 0 until arabicAyahs.length()) {
            val arabic = arabicAyahs.getJSONObject(i)
            val trans  = transAyahs.getJSONObject(i)
            val verseNum = arabic.getInt("numberInSurah")
            var arabicText = arabic.getString("text")
            var transText = trans.getString("text")

            // For surahs that get a Bismillah header (not 1 or 9),
            // strip the Bismillah from verse 1. The API always ends Bismillah
            // with the word for "Merciful" (رحيم) so we split on that boundary.
            if (verseNum == 1 && surahId !in SURAH_NO_BISMILLAH_HEADER) {
                // Strip diacritics to reliably find "رحيم" (rahim) — the last word of Bismillah
                // regardless of which Unicode diacritic codepoints the API uses
                // Strip diacritics using char code ranges, find rahim (رحيم) base letters
                fun isDiacritic(c: Char): Boolean {
                    val cp = c.code
                    return cp in 0x0610..0x061A || cp in 0x064B..0x065F ||
                           cp == 0x0670 || cp in 0x06D6..0x06DC ||
                           cp in 0x06DF..0x06E4 || cp == 0x06E7 ||
                           cp == 0x06E8 || cp in 0x06EA..0x06ED
                }
                val stripped = arabicText.filter { !isDiacritic(it) }
                val rahimBase = "رحيم" // رحيم without diacritics
                val idx = stripped.indexOf(rahimBase)
                if (idx != -1) {
                    // Map stripped index back to original string position
                    var origPos = 0
                    var strippedCount = 0
                    val targetStrippedPos = idx + rahimBase.length
                    while (origPos < arabicText.length && strippedCount < targetStrippedPos) {
                        if (!isDiacritic(arabicText[origPos])) strippedCount++
                        origPos++
                    }
                    while (origPos < arabicText.length && isDiacritic(arabicText[origPos])) origPos++
                    arabicText = arabicText.substring(origPos).trimStart()
                }
                // Strip bismillah from English translation
                val engBismillah = Regex("""^In the name of Allah[^.]+\.\s*""", RegexOption.IGNORE_CASE)
                transText = engBismillah.replace(transText, "").trimStart()
            }

            val surahObj = arabic.optJSONObject("surah")
            val surahName = surahObj?.optString("englishName") ?: ""
            val surahNameArabic = surahObj?.optString("name") ?: ""
            verses.add(
                Verse(
                    id = arabic.getInt("number"),
                    surahId = surahId,
                    verseNumber = verseNum,
                    arabicText = arabicText,
                    translationText = transText,
                    juz = arabic.optInt("juz", 0),
                    surahName = surahName,
                    surahNameArabic = surahNameArabic
                )
            )
        }
        return verses
    }
}
