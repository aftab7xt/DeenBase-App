package com.deenbase.app.features.settings.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.deenbase.app.data.SettingsManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class QuranSettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val settingsManager = SettingsManager(application)

    val translationLang = settingsManager.translationLang.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), "english"
    )
    val arabicFontStyle = settingsManager.arabicFontStyle.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), "al_majeed_quran"
    )
    val arabicFontSize = settingsManager.arabicFontSize.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), 32f
    )
    val translationFontSize = settingsManager.translationFontSize.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), 18f
    )
    val showTranslation = settingsManager.showTranslation.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), true
    )

    // True once all values have been loaded from DataStore
    val isLoaded = combine(
        settingsManager.arabicFontSize,
        settingsManager.translationFontSize,
        settingsManager.arabicFontStyle,
        settingsManager.translationLang,
        settingsManager.showTranslation
    ) { _, _, _, _, _ -> true }.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), false
    )

    fun setTranslationLang(lang: String) {
        viewModelScope.launch { settingsManager.setTranslationLang(lang) }
    }
    fun setArabicFontStyle(style: String) {
        viewModelScope.launch { settingsManager.setArabicFontStyle(style) }
    }
    fun setArabicFontSize(size: Float) {
        viewModelScope.launch { settingsManager.setArabicFontSize(size) }
    }
    fun setTranslationFontSize(size: Float) {
        viewModelScope.launch { settingsManager.setTranslationFontSize(size) }
    }
    fun setShowTranslation(show: Boolean) {
        viewModelScope.launch { settingsManager.setShowTranslation(show) }
    }
}
