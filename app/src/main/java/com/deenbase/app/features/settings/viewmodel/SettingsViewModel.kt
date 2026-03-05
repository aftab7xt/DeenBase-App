package com.deenbase.app.features.settings.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.deenbase.app.data.SettingsManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val settingsManager = SettingsManager(application)

    // Expose the current language as State so the UI automatically updates
    val translationLang = settingsManager.translationLang.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "english"
    )

    // Toggles between English and Urdu
    fun toggleTranslation() {
        viewModelScope.launch {
            val current = translationLang.value
            val nextLang = if (current == "english") "urdu" else "english"
            settingsManager.setTranslationLang(nextLang)
        }
    }
}
