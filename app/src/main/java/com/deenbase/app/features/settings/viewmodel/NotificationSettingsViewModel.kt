package com.deenbase.app.features.settings.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.deenbase.app.data.SettingsManager
import com.deenbase.app.notifications.NotificationHelper
import com.deenbase.app.notifications.NotificationPrefs
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NotificationSettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val sm = SettingsManager(application)
    private val ctx = application.applicationContext

    val quranEnabled       = sm.quranNotifEnabled.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val quranHour1         = sm.quranNotifHour1.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 8)
    val quranMinute1       = sm.quranNotifMinute1.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val quranSecondEnabled = sm.quranNotifSecondEnabled.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val quranHour2         = sm.quranNotifHour2.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 8)
    val quranMinute2       = sm.quranNotifMinute2.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val dhikrEnabled       = sm.dhikrNotifEnabled.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val dhikrHour1         = sm.dhikrNotifHour1.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 7)
    val dhikrMinute1       = sm.dhikrNotifMinute1.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val dhikrSecondEnabled = sm.dhikrNotifSecondEnabled.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val dhikrHour2         = sm.dhikrNotifHour2.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 7)
    val dhikrMinute2       = sm.dhikrNotifMinute2.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private fun reschedule() {
        viewModelScope.launch {
            val prefs = NotificationPrefs(
                quranEnabled = quranEnabled.value,
                quranHour1 = quranHour1.value, quranMinute1 = quranMinute1.value,
                quranSecondEnabled = quranSecondEnabled.value,
                quranHour2 = quranHour2.value, quranMinute2 = quranMinute2.value,
                dhikrEnabled = dhikrEnabled.value,
                dhikrHour1 = dhikrHour1.value, dhikrMinute1 = dhikrMinute1.value,
                dhikrSecondEnabled = dhikrSecondEnabled.value,
                dhikrHour2 = dhikrHour2.value, dhikrMinute2 = dhikrMinute2.value
            )
            NotificationHelper.rescheduleAll(ctx, prefs)
        }
    }

    fun setQuranEnabled(v: Boolean) { viewModelScope.launch { sm.setQuranNotifEnabled(v); reschedule() } }
    fun setQuranTime1(h: Int, m: Int) { viewModelScope.launch { sm.setQuranNotifHour1(h); sm.setQuranNotifMinute1(m); reschedule() } }
    fun setQuranSecondEnabled(v: Boolean) { viewModelScope.launch { sm.setQuranNotifSecondEnabled(v); reschedule() } }
    fun setQuranTime2(h: Int, m: Int) { viewModelScope.launch { sm.setQuranNotifHour2(h); sm.setQuranNotifMinute2(m); reschedule() } }
    fun setDhikrEnabled(v: Boolean) { viewModelScope.launch { sm.setDhikrNotifEnabled(v); reschedule() } }
    fun setDhikrTime1(h: Int, m: Int) { viewModelScope.launch { sm.setDhikrNotifHour1(h); sm.setDhikrNotifMinute1(m); reschedule() } }
    fun setDhikrSecondEnabled(v: Boolean) { viewModelScope.launch { sm.setDhikrNotifSecondEnabled(v); reschedule() } }
    fun setDhikrTime2(h: Int, m: Int) { viewModelScope.launch { sm.setDhikrNotifHour2(h); sm.setDhikrNotifMinute2(m); reschedule() } }
}
