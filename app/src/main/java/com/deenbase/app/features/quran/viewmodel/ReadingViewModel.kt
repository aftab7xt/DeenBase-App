package com.deenbase.app.features.quran.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.deenbase.app.data.SettingsManager
import com.deenbase.app.features.quran.data.QuranRepository
import com.deenbase.app.features.quran.data.Verse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ReadingViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = QuranRepository()
    private val settingsManager = SettingsManager(application)

    private val _verses = MutableStateFlow<List<Verse>>(emptyList())
    val verses: StateFlow<List<Verse>> = _verses.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _currentSurahId = MutableStateFlow(1)
    val currentSurahId: StateFlow<Int> = _currentSurahId.asStateFlow()

    private val _surahName = MutableStateFlow("")
    val surahName: StateFlow<String> = _surahName.asStateFlow()

    private val _surahNameArabic = MutableStateFlow("")
    val surahNameArabic: StateFlow<String> = _surahNameArabic.asStateFlow()

    private val _currentJuz = MutableStateFlow(0)
    val currentJuz: StateFlow<Int> = _currentJuz.asStateFlow()

    val arabicFontSize = settingsManager.arabicFontSize.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), 32f
    )
    val translationFontSize = settingsManager.translationFontSize.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), 18f
    )
    val arabicFontStyle = settingsManager.arabicFontStyle.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), "al_majeed_quran"
    )
    val showTranslation = settingsManager.showTranslation.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), true
    )
    val translationLang = settingsManager.translationLang.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), "english"
    )

    fun loadSurah(surahId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _verses.value = emptyList()
            _currentSurahId.value = surahId
            try {
                val lang = settingsManager.translationLang.first()
                val data = withContext(Dispatchers.IO) {
                    repository.getVersesForSurah(surahId, lang)
                }
                _verses.value = data
                if (data.isNotEmpty()) {
                    _surahName.value = data.first().surahName
                    _surahNameArabic.value = data.first().surahNameArabic
                    _currentJuz.value = data.first().juz
                }
            } catch (e: Exception) {
                _error.value = "Failed to load. Check your connection."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun saveProgress(surahId: Int, verseNumber: Int) {
        viewModelScope.launch {
            settingsManager.setGoalSurahId(surahId)
            settingsManager.setGoalVerse(verseNumber)
        }
    }

    fun updateCurrentJuz(juz: Int) {
        _currentJuz.value = juz
    }

    fun incrementTodayCount() {
        viewModelScope.launch {
            val current = settingsManager.goalTodayCount.first()
            settingsManager.setGoalTodayCount(current + 1)
        }
    }
}
