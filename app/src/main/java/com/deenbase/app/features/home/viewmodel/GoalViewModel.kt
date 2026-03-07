package com.deenbase.app.features.home.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.deenbase.app.data.SettingsManager
import com.deenbase.app.features.hadith.data.Hadith
import com.deenbase.app.features.hadith.data.HadithRepository
import com.deenbase.app.features.quran.data.QuranRepository
import com.deenbase.app.features.quran.data.SurahInfo
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
import java.time.LocalDate

class GoalViewModel(application: Application) : AndroidViewModel(application) {
    private val settingsManager = SettingsManager(application)
    private val quranRepository = QuranRepository()

    val goalSurahId = settingsManager.goalSurahId.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1)
    val goalVerse   = settingsManager.goalVerse.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1)
    val dailyTarget = settingsManager.goalDailyTarget.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 10)
    val todayCount  = settingsManager.goalTodayCount.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _surahs = MutableStateFlow<List<SurahInfo>>(emptyList())
    val surahs: StateFlow<List<SurahInfo>> = _surahs.asStateFlow()

    private val _dailyVerse = MutableStateFlow<Verse?>(null)
    val dailyVerse: StateFlow<Verse?> = _dailyVerse.asStateFlow()

    private val _dailyHadith = MutableStateFlow<Hadith?>(null)
    val dailyHadith: StateFlow<Hadith?> = _dailyHadith.asStateFlow()

    init {
        val dayOfYear = LocalDate.now().dayOfYear
        loadSurahs()
        resetIfNewDay()
        loadDailyContent(dayOfYear)
    }

    private fun loadSurahs() {
        viewModelScope.launch {
            val list = withContext(Dispatchers.IO) { quranRepository.getSurahsList() }
            _surahs.value = list
        }
    }

    private fun loadDailyContent(dayOfYear: Int) {
        viewModelScope.launch {
            val verse = withContext(Dispatchers.IO) { quranRepository.getDailyVerse(dayOfYear) }
            _dailyVerse.value = verse
        }
        viewModelScope.launch {
            HadithRepository.getDailyHadith(dayOfYear)
                .onSuccess { _dailyHadith.value = it }
        }
    }

    private fun resetIfNewDay() {
        viewModelScope.launch {
            // use .first() to wait for the real DataStore value — avoids resetting
            // count on every launch before DataStore has loaded
            val savedDate = settingsManager.goalTodayDate.first()
            val today = LocalDate.now().toString()
            if (savedDate != today) {
                settingsManager.setGoalTodayCount(0)
                settingsManager.setGoalTodayDate(today)
            }
        }
    }

    fun setDailyTarget(target: Int) {
        viewModelScope.launch { settingsManager.setGoalDailyTarget(target) }
    }

    fun setGoalPosition(surahId: Int, verse: Int) {
        viewModelScope.launch {
            settingsManager.setGoalSurahId(surahId)
            settingsManager.setGoalVerse(verse)
        }
    }

    fun incrementTodayCount(verses: Int = 1) {
        viewModelScope.launch {
            val current = todayCount.value
            settingsManager.setGoalTodayCount(current + verses)
        }
    }
}
