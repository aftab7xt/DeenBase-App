package com.deenbase.app.features.settings.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.deenbase.app.data.SettingsManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppPreferencesViewModel(application: Application) : AndroidViewModel(application) {
    private val settingsManager = SettingsManager(application)

    val themeMode = settingsManager.themeMode.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), "system"
    )
    val hapticsEnabled = settingsManager.hapticsEnabled.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), true
    )

    fun setThemeMode(mode: String) {
        viewModelScope.launch { settingsManager.setThemeMode(mode) }
    }
    fun setHapticsEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsManager.setHapticsEnabled(enabled) }
    }
    fun resetAllData() {
        viewModelScope.launch { settingsManager.resetAllData() }
    }
}
