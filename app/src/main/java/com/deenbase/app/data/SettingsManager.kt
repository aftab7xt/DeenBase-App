package com.deenbase.app.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

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
    }

    // ── Quran display ─────────────────────────────────────────────────────────
    val translationLang: Flow<String> = context.dataStore.data.map {
        it[TRANSLATION_LANG] ?: "english"
    }
    val arabicFontStyle: Flow<String> = context.dataStore.data.map {
        it[ARABIC_FONT_STYLE] ?: "indopak_nastaleeq"
    }
    val arabicFontSize: Flow<Float> = context.dataStore.data.map {
        it[ARABIC_FONT_SIZE] ?: 32f
    }
    val translationFontSize: Flow<Float> = context.dataStore.data.map {
        it[TRANSLATION_FONT_SIZE] ?: 18f
    }
    val showTranslation: Flow<Boolean> = context.dataStore.data.map {
        it[SHOW_TRANSLATION] ?: true
    }

    suspend fun setTranslationLang(lang: String) {
        context.dataStore.edit { it[TRANSLATION_LANG] = lang }
    }
    suspend fun setArabicFontStyle(style: String) {
        context.dataStore.edit { it[ARABIC_FONT_STYLE] = style }
    }
    suspend fun setArabicFontSize(size: Float) {
        context.dataStore.edit { it[ARABIC_FONT_SIZE] = size }
    }
    suspend fun setTranslationFontSize(size: Float) {
        context.dataStore.edit { it[TRANSLATION_FONT_SIZE] = size }
    }
    suspend fun setShowTranslation(show: Boolean) {
        context.dataStore.edit { it[SHOW_TRANSLATION] = show }
    }

    // ── Goal tracking ─────────────────────────────────────────────────────────
    val goalSurahId: Flow<Int> = context.dataStore.data.map {
        it[GOAL_SURAH_ID] ?: 1
    }
    val goalVerse: Flow<Int> = context.dataStore.data.map {
        it[GOAL_VERSE] ?: 1
    }
    val goalDailyTarget: Flow<Int> = context.dataStore.data.map {
        it[GOAL_DAILY_TARGET] ?: 10
    }
    val goalTodayCount: Flow<Int> = context.dataStore.data.map {
        it[GOAL_TODAY_COUNT] ?: 0
    }
    val goalTodayDate: Flow<String> = context.dataStore.data.map {
        it[GOAL_TODAY_DATE] ?: ""
    }

    suspend fun setGoalSurahId(id: Int) {
        context.dataStore.edit { it[GOAL_SURAH_ID] = id }
    }
    suspend fun setGoalVerse(verse: Int) {
        context.dataStore.edit { it[GOAL_VERSE] = verse }
    }
    suspend fun setGoalDailyTarget(target: Int) {
        context.dataStore.edit { it[GOAL_DAILY_TARGET] = target }
    }
    suspend fun setGoalTodayCount(count: Int) {
        context.dataStore.edit { it[GOAL_TODAY_COUNT] = count }
    }
    suspend fun setGoalTodayDate(date: String) {
        context.dataStore.edit { it[GOAL_TODAY_DATE] = date }
    }

    // ── Subhanallahi wa bihamdihi 100x ────────────────────────────────────────
    val subhanallahCount: Flow<Int> = context.dataStore.data.map {
        it[SUBHANALLAH_COUNT] ?: 0
    }
    val subhanallahDate: Flow<String> = context.dataStore.data.map {
        it[SUBHANALLAH_DATE] ?: ""
    }

    suspend fun setSubhanallahCount(count: Int) {
        context.dataStore.edit { it[SUBHANALLAH_COUNT] = count }
    }
    suspend fun setSubhanallahDate(date: String) {
        context.dataStore.edit { it[SUBHANALLAH_DATE] = date }
    }

    // ── App preferences ───────────────────────────────────────────────────────
    val themeMode: Flow<String> = context.dataStore.data.map {
        it[THEME_MODE] ?: "system"
    }
    val hapticsEnabled: Flow<Boolean> = context.dataStore.data.map {
        it[HAPTICS_ENABLED] ?: true
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { it[THEME_MODE] = mode }
    }
    suspend fun setHapticsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[HAPTICS_ENABLED] = enabled }
    }

    // ── Onboarding ────────────────────────────────────────────────────────────
    val onboardingDone: Flow<Boolean> = context.dataStore.data.map {
        it[ONBOARDING_DONE] ?: false
    }

    suspend fun setOnboardingDone(done: Boolean) {
        context.dataStore.edit { it[ONBOARDING_DONE] = done }
    }

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
