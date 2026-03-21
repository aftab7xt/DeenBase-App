package com.deenbase.app.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray

private val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsManager(private val context: Context) {
    companion object {
        // Quran display settings
        val TRANSLATION_LANG      = stringPreferencesKey("translation_lang")
        val ARABIC_FONT_STYLE     = stringPreferencesKey("arabic_font_style")
        val ARABIC_FONT_SIZE      = floatPreferencesKey("arabic_font_size")
        val TRANSLATION_FONT_SIZE = floatPreferencesKey("translation_font_size")
        val SHOW_TRANSLATION      = booleanPreferencesKey("show_translation")

        // Goal tracking
        val GOAL_SURAH_ID         = intPreferencesKey("goal_surah_id")
        val GOAL_VERSE            = intPreferencesKey("goal_verse")
        val GOAL_DAILY_TARGET     = intPreferencesKey("goal_daily_target")
        val GOAL_TODAY_COUNT      = intPreferencesKey("goal_today_count")
        val GOAL_TODAY_DATE       = stringPreferencesKey("goal_today_date")

        // Subhanallahi wa bihamdihi 100x
        val SUBHANALLAH_COUNT     = intPreferencesKey("subhanallah_count")
        val SUBHANALLAH_DATE      = stringPreferencesKey("subhanallah_date")

        // App preferences
        val THEME_MODE            = stringPreferencesKey("theme_mode")
        val HAPTICS_ENABLED       = booleanPreferencesKey("haptics_enabled")
        val OLED_MODE             = booleanPreferencesKey("oled_mode")

        // Onboarding
        val ONBOARDING_DONE       = booleanPreferencesKey("onboarding_done")

        // Notification preferences
        val QURAN_NOTIF_ENABLED        = booleanPreferencesKey("quran_notif_enabled")
        val QURAN_NOTIF_HOUR_1         = intPreferencesKey("quran_notif_hour_1")
        val QURAN_NOTIF_MINUTE_1       = intPreferencesKey("quran_notif_minute_1")
        val QURAN_NOTIF_SECOND_ENABLED = booleanPreferencesKey("quran_notif_second_enabled")
        val QURAN_NOTIF_HOUR_2         = intPreferencesKey("quran_notif_hour_2")
        val QURAN_NOTIF_MINUTE_2       = intPreferencesKey("quran_notif_minute_2")
        val DHIKR_NOTIF_ENABLED        = booleanPreferencesKey("dhikr_notif_enabled")
        val DHIKR_NOTIF_HOUR_1         = intPreferencesKey("dhikr_notif_hour_1")
        val DHIKR_NOTIF_MINUTE_1       = intPreferencesKey("dhikr_notif_minute_1")
        val DHIKR_NOTIF_SECOND_ENABLED = booleanPreferencesKey("dhikr_notif_second_enabled")
        val DHIKR_NOTIF_HOUR_2         = intPreferencesKey("dhikr_notif_hour_2")
        val DHIKR_NOTIF_MINUTE_2       = intPreferencesKey("dhikr_notif_minute_2")

        // Dhikr completion tracking (value = date string "yyyy-MM-dd")
        fun dhikrCompletionKey(dhikrId: String, period: String) =
            stringPreferencesKey("dhikr_${dhikrId}_${period}_date")

        // Verse favourites and bookmarks — stored as "surahId:verseNumber" e.g. "2:255"
        val FAVOURITE_VERSES  = stringSetPreferencesKey("favourite_verses")
        val BOOKMARKED_VERSES = stringSetPreferencesKey("bookmarked_verses")

        // Quran search history — stored as JSON array string, newest first, max 10
        val QURAN_SEARCH_HISTORY  = stringPreferencesKey("quran_search_history")
        val HADITH_SEARCH_HISTORY = stringPreferencesKey("hadith_search_history")
        const val SEARCH_HISTORY_MAX = 10
    }

    // ── Quran display ─────────────────────────────────────────────────────────
    val translationLang: Flow<String> = context.dataStore.data.map { it[TRANSLATION_LANG] ?: "english" }
    val arabicFontStyle: Flow<String> = context.dataStore.data.map { it[ARABIC_FONT_STYLE] ?: "indopak_nastaleeq" }
    val arabicFontSize: Flow<Float>   = context.dataStore.data.map { it[ARABIC_FONT_SIZE] ?: 32f }
    val translationFontSize: Flow<Float> = context.dataStore.data.map { it[TRANSLATION_FONT_SIZE] ?: 18f }
    val showTranslation: Flow<Boolean>   = context.dataStore.data.map { it[SHOW_TRANSLATION] ?: true }

    suspend fun setTranslationLang(lang: String)    { context.dataStore.edit { it[TRANSLATION_LANG] = lang } }
    suspend fun setArabicFontStyle(style: String)   { context.dataStore.edit { it[ARABIC_FONT_STYLE] = style } }
    suspend fun setArabicFontSize(size: Float)      { context.dataStore.edit { it[ARABIC_FONT_SIZE] = size } }
    suspend fun setTranslationFontSize(size: Float) { context.dataStore.edit { it[TRANSLATION_FONT_SIZE] = size } }
    suspend fun setShowTranslation(show: Boolean)   { context.dataStore.edit { it[SHOW_TRANSLATION] = show } }

    // ── Goal tracking ─────────────────────────────────────────────────────────
    val goalSurahId: Flow<Int>      = context.dataStore.data.map { it[GOAL_SURAH_ID] ?: 1 }
    val goalVerse: Flow<Int>        = context.dataStore.data.map { it[GOAL_VERSE] ?: 1 }
    val goalDailyTarget: Flow<Int>  = context.dataStore.data.map { it[GOAL_DAILY_TARGET] ?: 10 }
    val goalTodayCount: Flow<Int>   = context.dataStore.data.map { it[GOAL_TODAY_COUNT] ?: 0 }
    val goalTodayDate: Flow<String> = context.dataStore.data.map { it[GOAL_TODAY_DATE] ?: "" }

    suspend fun setGoalSurahId(id: Int)        { context.dataStore.edit { it[GOAL_SURAH_ID] = id } }
    suspend fun setGoalVerse(verse: Int)        { context.dataStore.edit { it[GOAL_VERSE] = verse } }
    suspend fun setGoalDailyTarget(target: Int) { context.dataStore.edit { it[GOAL_DAILY_TARGET] = target } }
    suspend fun setGoalTodayCount(count: Int)   { context.dataStore.edit { it[GOAL_TODAY_COUNT] = count } }
    suspend fun setGoalTodayDate(date: String)  { context.dataStore.edit { it[GOAL_TODAY_DATE] = date } }

    // ── Subhanallahi wa bihamdihi 100x ────────────────────────────────────────
    val subhanallahCount: Flow<Int>   = context.dataStore.data.map { it[SUBHANALLAH_COUNT] ?: 0 }
    val subhanallahDate: Flow<String> = context.dataStore.data.map { it[SUBHANALLAH_DATE] ?: "" }

    suspend fun setSubhanallahCount(count: Int) { context.dataStore.edit { it[SUBHANALLAH_COUNT] = count } }
    suspend fun setSubhanallahDate(date: String) { context.dataStore.edit { it[SUBHANALLAH_DATE] = date } }

    // ── App preferences ───────────────────────────────────────────────────────
    val themeMode: Flow<String>      = context.dataStore.data.map { it[THEME_MODE] ?: "system" }
    val hapticsEnabled: Flow<Boolean> = context.dataStore.data.map { it[HAPTICS_ENABLED] ?: true }
    val oledMode: Flow<Boolean>       = context.dataStore.data.map { it[OLED_MODE] ?: false }

    suspend fun setThemeMode(mode: String)      { context.dataStore.edit { it[THEME_MODE] = mode } }
    suspend fun setHapticsEnabled(enabled: Boolean) { context.dataStore.edit { it[HAPTICS_ENABLED] = enabled } }
    suspend fun setOledMode(enabled: Boolean)   { context.dataStore.edit { it[OLED_MODE] = enabled } }

    // ── Onboarding ────────────────────────────────────────────────────────────
    val onboardingDone: Flow<Boolean> = context.dataStore.data.map { it[ONBOARDING_DONE] ?: false }

    suspend fun setOnboardingDone(done: Boolean) { context.dataStore.edit { it[ONBOARDING_DONE] = done } }

    // ── Notification preferences ──────────────────────────────────────────────
    val quranNotifEnabled: Flow<Boolean>       = context.dataStore.data.map { it[QURAN_NOTIF_ENABLED] ?: false }
    val quranNotifHour1: Flow<Int>             = context.dataStore.data.map { it[QURAN_NOTIF_HOUR_1] ?: 8 }
    val quranNotifMinute1: Flow<Int>           = context.dataStore.data.map { it[QURAN_NOTIF_MINUTE_1] ?: 0 }
    val quranNotifSecondEnabled: Flow<Boolean> = context.dataStore.data.map { it[QURAN_NOTIF_SECOND_ENABLED] ?: false }
    val quranNotifHour2: Flow<Int>             = context.dataStore.data.map { it[QURAN_NOTIF_HOUR_2] ?: 8 }
    val quranNotifMinute2: Flow<Int>           = context.dataStore.data.map { it[QURAN_NOTIF_MINUTE_2] ?: 0 }
    val dhikrNotifEnabled: Flow<Boolean>       = context.dataStore.data.map { it[DHIKR_NOTIF_ENABLED] ?: false }
    val dhikrNotifHour1: Flow<Int>             = context.dataStore.data.map { it[DHIKR_NOTIF_HOUR_1] ?: 7 }
    val dhikrNotifMinute1: Flow<Int>           = context.dataStore.data.map { it[DHIKR_NOTIF_MINUTE_1] ?: 0 }
    val dhikrNotifSecondEnabled: Flow<Boolean> = context.dataStore.data.map { it[DHIKR_NOTIF_SECOND_ENABLED] ?: false }
    val dhikrNotifHour2: Flow<Int>             = context.dataStore.data.map { it[DHIKR_NOTIF_HOUR_2] ?: 7 }
    val dhikrNotifMinute2: Flow<Int>           = context.dataStore.data.map { it[DHIKR_NOTIF_MINUTE_2] ?: 0 }

    suspend fun setQuranNotifEnabled(v: Boolean)       { context.dataStore.edit { it[QURAN_NOTIF_ENABLED] = v } }
    suspend fun setQuranNotifHour1(v: Int)             { context.dataStore.edit { it[QURAN_NOTIF_HOUR_1] = v } }
    suspend fun setQuranNotifMinute1(v: Int)           { context.dataStore.edit { it[QURAN_NOTIF_MINUTE_1] = v } }
    suspend fun setQuranNotifSecondEnabled(v: Boolean) { context.dataStore.edit { it[QURAN_NOTIF_SECOND_ENABLED] = v } }
    suspend fun setQuranNotifHour2(v: Int)             { context.dataStore.edit { it[QURAN_NOTIF_HOUR_2] = v } }
    suspend fun setQuranNotifMinute2(v: Int)           { context.dataStore.edit { it[QURAN_NOTIF_MINUTE_2] = v } }
    suspend fun setDhikrNotifEnabled(v: Boolean)       { context.dataStore.edit { it[DHIKR_NOTIF_ENABLED] = v } }
    suspend fun setDhikrNotifHour1(v: Int)             { context.dataStore.edit { it[DHIKR_NOTIF_HOUR_1] = v } }
    suspend fun setDhikrNotifMinute1(v: Int)           { context.dataStore.edit { it[DHIKR_NOTIF_MINUTE_1] = v } }
    suspend fun setDhikrNotifSecondEnabled(v: Boolean) { context.dataStore.edit { it[DHIKR_NOTIF_SECOND_ENABLED] = v } }
    suspend fun setDhikrNotifHour2(v: Int)             { context.dataStore.edit { it[DHIKR_NOTIF_HOUR_2] = v } }
    suspend fun setDhikrNotifMinute2(v: Int)           { context.dataStore.edit { it[DHIKR_NOTIF_MINUTE_2] = v } }

    // ── Dhikr completion tracking ─────────────────────────────────────────────
    suspend fun getDhikrCompletionDate(dhikrId: String, period: String): String {
        val key = dhikrCompletionKey(dhikrId, period)
        return context.dataStore.data.first()[key] ?: ""
    }
    suspend fun setDhikrCompletionDate(dhikrId: String, period: String, date: String) {
        val key = dhikrCompletionKey(dhikrId, period)
        context.dataStore.edit { it[key] = date }
    }

    // ── Verse favourites ──────────────────────────────────────────────────────
    val favouriteVerses: Flow<Set<String>> = context.dataStore.data.map { it[FAVOURITE_VERSES] ?: emptySet() }

    suspend fun toggleFavouriteVerse(surahId: Int, verseNumber: Int) {
        val key = "$surahId:$verseNumber"
        context.dataStore.edit { prefs ->
            val current = prefs[FAVOURITE_VERSES] ?: emptySet()
            prefs[FAVOURITE_VERSES] = if (key in current) current - key else current + key
        }
    }

    // ── Verse bookmarks ───────────────────────────────────────────────────────
    val bookmarkedVerses: Flow<Set<String>> = context.dataStore.data.map { it[BOOKMARKED_VERSES] ?: emptySet() }

    suspend fun toggleBookmarkVerse(surahId: Int, verseNumber: Int) {
        val key = "$surahId:$verseNumber"
        context.dataStore.edit { prefs ->
            val current = prefs[BOOKMARKED_VERSES] ?: emptySet()
            prefs[BOOKMARKED_VERSES] = if (key in current) current - key else current + key
        }
    }

    // ── Quran search history ──────────────────────────────────────────────────
    // Stored as a JSON array string, newest first, capped at SEARCH_HISTORY_MAX

    val quranSearchHistory: Flow<List<String>> = context.dataStore.data.map { prefs ->
        parseHistory(prefs[QURAN_SEARCH_HISTORY])
    }

    suspend fun addSearchHistoryItem(query: String) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return
        context.dataStore.edit { prefs ->
            val current = parseHistory(prefs[QURAN_SEARCH_HISTORY])
            // Remove duplicate if exists, add to front, cap at max
            val updated = (listOf(trimmed) + current.filter { it != trimmed })
                .take(SEARCH_HISTORY_MAX)
            prefs[QURAN_SEARCH_HISTORY] = serializeHistory(updated)
        }
    }

    suspend fun removeSearchHistoryItem(query: String) {
        context.dataStore.edit { prefs ->
            val current = parseHistory(prefs[QURAN_SEARCH_HISTORY])
            prefs[QURAN_SEARCH_HISTORY] = serializeHistory(current.filter { it != query })
        }
    }

    suspend fun clearSearchHistory() {
        context.dataStore.edit { it.remove(QURAN_SEARCH_HISTORY) }
    }

    // ── Hadith search history ─────────────────────────────────────────────────
    val hadithSearchHistory: Flow<List<String>> = context.dataStore.data.map { prefs ->
        parseHistory(prefs[HADITH_SEARCH_HISTORY])
    }

    suspend fun addHadithSearchHistoryItem(query: String) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return
        context.dataStore.edit { prefs ->
            val current = parseHistory(prefs[HADITH_SEARCH_HISTORY])
            val updated = (listOf(trimmed) + current.filter { it != trimmed }).take(SEARCH_HISTORY_MAX)
            prefs[HADITH_SEARCH_HISTORY] = serializeHistory(updated)
        }
    }

    suspend fun removeHadithSearchHistoryItem(query: String) {
        context.dataStore.edit { prefs ->
            val current = parseHistory(prefs[HADITH_SEARCH_HISTORY])
            prefs[HADITH_SEARCH_HISTORY] = serializeHistory(current.filter { it != query })
        }
    }

    suspend fun clearHadithSearchHistory() {
        context.dataStore.edit { it.remove(HADITH_SEARCH_HISTORY) }
    }

    private fun parseHistory(raw: String?): List<String> {
        if (raw.isNullOrBlank()) return emptyList()
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { arr.getString(it) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun serializeHistory(list: List<String>): String {
        val arr = JSONArray()
        list.forEach { arr.put(it) }
        return arr.toString()
    }

    // ── Reset ─────────────────────────────────────────────────────────────────
    suspend fun resetAllData() {
        context.dataStore.edit { prefs ->
            prefs.remove(GOAL_SURAH_ID)
            prefs.remove(GOAL_VERSE)
            prefs.remove(GOAL_DAILY_TARGET)
            prefs.remove(GOAL_TODAY_COUNT)
            prefs.remove(GOAL_TODAY_DATE)
            prefs.remove(SUBHANALLAH_COUNT)
            prefs.remove(SUBHANALLAH_DATE)
        }
    }
}
