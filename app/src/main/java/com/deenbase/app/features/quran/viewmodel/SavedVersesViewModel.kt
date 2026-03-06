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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SavedVersesViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsManager = SettingsManager(application)
    private val repository = QuranRepository()

    private val translationLang = settingsManager.translationLang.stateIn(
        viewModelScope, SharingStarted.Eagerly, "english"
    )

    private val _favourites = MutableStateFlow<List<Verse>>(emptyList())
    val favourites: StateFlow<List<Verse>> = _favourites.asStateFlow()

    private val _bookmarks = MutableStateFlow<List<Verse>>(emptyList())
    val bookmarks: StateFlow<List<Verse>> = _bookmarks.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        observeSaved()
    }

    private fun observeSaved() {
        viewModelScope.launch {
            settingsManager.favouriteVerses.collectLatest { keys ->
                _isLoading.value = true
                _favourites.value = fetchVerses(keys)
                _isLoading.value = false
            }
        }
        viewModelScope.launch {
            settingsManager.bookmarkedVerses.collectLatest { keys ->
                _bookmarks.value = fetchVerses(keys)
            }
        }
    }

    private suspend fun fetchVerses(keys: Set<String>): List<Verse> {
        if (keys.isEmpty()) return emptyList()
        val lang = translationLang.value
        return withContext(Dispatchers.IO) {
            keys.mapNotNull { key ->
                val parts = key.split(":")
                val surahId = parts.getOrNull(0)?.toIntOrNull() ?: return@mapNotNull null
                val verseNum = parts.getOrNull(1)?.toIntOrNull() ?: return@mapNotNull null
                try {
                    repository.getVersesForSurah(surahId, lang)
                        .find { it.verseNumber == verseNum }
                } catch (e: Exception) {
                    null
                }
            }.sortedWith(compareBy({ it.surahId }, { it.verseNumber }))
        }
    }

    fun removeFavourite(surahId: Int, verseNumber: Int) {
        viewModelScope.launch { settingsManager.toggleFavouriteVerse(surahId, verseNumber) }
    }

    fun removeBookmark(surahId: Int, verseNumber: Int) {
        viewModelScope.launch { settingsManager.toggleBookmarkVerse(surahId, verseNumber) }
    }
}
