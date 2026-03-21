package com.deenbase.app.features.quran.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.deenbase.app.data.SettingsManager
import com.deenbase.app.features.quran.data.QuranRepository
import com.deenbase.app.features.quran.data.SurahInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class QuranViewModel(application: Application) : AndroidViewModel(application) {
    private val repository      = QuranRepository()
    private val settingsManager = SettingsManager(application)

    private val _surahs = MutableStateFlow<List<SurahInfo>>(emptyList())
    val surahs: StateFlow<List<SurahInfo>> = _surahs.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Counts for the hub cards
    val favouriteCount = settingsManager.favouriteVerses
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val bookmarkCount = settingsManager.bookmarkedVerses
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    init {
        loadSurahs()
    }

    fun loadSurahs() {
        viewModelScope.launch {
            _error.value = null
            delay(250)
            try {
                val data = withContext(Dispatchers.IO) { repository.getSurahsList() }
                _surahs.value = data
            } catch (e: Exception) {
                _error.value = "Failed to load. Check your connection."
            }
        }
    }
}
