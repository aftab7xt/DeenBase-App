package com.deenbase.app.features.quran.data

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

// ── Data classes ──────────────────────────────────────────────────────────────

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

// Surahs where Bismillah header is NOT shown:
// 1 (Al-Fatiha) — Bismillah IS verse 1, shown naturally
// 9 (At-Tawbah) — no Bismillah at all
val SURAH_NO_BISMILLAH_HEADER = setOf(1, 9)

// ── Repository ────────────────────────────────────────────────────────────────

class QuranRepository {

    private val TURSO_URL   = com.deenbase.app.BuildConfig.TURSO_URL
    private val TURSO_TOKEN = com.deenbase.app.BuildConfig.TURSO_TOKEN

    private val JSON_MEDIA = "application/json".toMediaType()

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    // ── Low-level Turso query (blocking) ─────────────────────────────────────

    private fun query(sql: String, args: List<Any?> = emptyList()): List<Map<String, String?>> {
        val argsArray = JSONArray()
        for (arg in args) {
            argsArray.put(when (arg) {
                null    -> JSONObject().put("type", "null")
                is Int  -> JSONObject().put("type", "integer").put("value", arg.toString())
                is Long -> JSONObject().put("type", "integer").put("value", arg.toString())
                else    -> JSONObject().put("type", "text").put("value", arg.toString())
            })
        }

        val stmt = JSONObject()
            .put("sql", sql)
            .put("args", argsArray)

        val body = JSONObject()
            .put("requests", JSONArray()
                .put(JSONObject().put("type", "execute").put("stmt", stmt))
                .put(JSONObject().put("type", "close"))
            )

        val request = Request.Builder()
            .url(TURSO_URL)
            .addHeader("Authorization", "Bearer $TURSO_TOKEN")
            .addHeader("Content-Type", "application/json")
            .post(body.toString().toRequestBody(JSON_MEDIA))
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: error("Empty response from Turso")
        if (!response.isSuccessful) error("Turso HTTP ${response.code}: $responseBody")

        val json   = JSONObject(responseBody)
        val result = json.getJSONArray("results")
            .getJSONObject(0)
            .getJSONObject("response")
            .getJSONObject("result")

        val cols = result.getJSONArray("cols")
        val rows = result.getJSONArray("rows")

        val colNames = (0 until cols.length()).map { cols.getJSONObject(it).getString("name") }

        return (0 until rows.length()).map { rowIdx ->
            val row = rows.getJSONArray(rowIdx)
            colNames.mapIndexed { colIdx, name ->
                val cell = row.getJSONObject(colIdx)
                name to if (cell.getString("type") == "null") null
                         else cell.optString("value")
            }.toMap()
        }
    }

    // ── Public API ────────────────────────────────────────────────────────────

    fun getSurahsList(): List<SurahInfo> {
        val rows = query(
            "SELECT surah_number, name_arabic, name_transliteration, name_english, total_verses, revelation_type FROM surahs ORDER BY surah_number"
        )
        return rows.map { row ->
            SurahInfo(
                id                  = row["surah_number"]?.toIntOrNull() ?: 0,
                nameArabic          = row["name_arabic"] ?: "",
                nameTransliteration = row["name_transliteration"] ?: "",
                nameEnglish         = row["name_english"] ?: "",
                type                = row["revelation_type"] ?: "",
                totalVerses         = row["total_verses"]?.toIntOrNull() ?: 0
            )
        }
    }

    fun getVersesForSurah(surahId: Int, lang: String): List<Verse> {
        val rows = query(
            """
            SELECT v.id, v.surah_number, v.verse_number, v.arabic_text,
                   v.english_text, v.urdu_text, v.juz,
                   s.name_transliteration, s.name_arabic
            FROM verses v
            JOIN surahs s ON v.surah_number = s.surah_number
            WHERE v.surah_number = ?
            ORDER BY v.verse_number
            """.trimIndent(),
            listOf(surahId)
        )

        return rows.map { row ->
            val translationText = if (lang == "urdu") {
                row["urdu_text"] ?: row["english_text"] ?: ""
            } else {
                row["english_text"] ?: ""
            }

            Verse(
                id              = row["id"]?.toIntOrNull() ?: 0,
                surahId         = row["surah_number"]?.toIntOrNull() ?: surahId,
                verseNumber     = row["verse_number"]?.toIntOrNull() ?: 0,
                arabicText      = row["arabic_text"] ?: "",
                translationText = translationText,
                juz             = row["juz"]?.toIntOrNull() ?: 0,
                surahName       = row["name_transliteration"] ?: "",
                surahNameArabic = row["name_arabic"] ?: ""
            )
        }
    }
}
