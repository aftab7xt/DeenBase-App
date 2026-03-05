package com.deenbase.app.features.tasbih.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.deenbase.app.data.SettingsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate

class SubhanallahViewModel(app: Application) : AndroidViewModel(app) {

    private val settings = SettingsManager(app)

    private val _count = MutableStateFlow(0)
    val count: StateFlow<Int> = _count

    private val _isComplete = MutableStateFlow(false)
    val isComplete: StateFlow<Boolean> = _isComplete

    init {
        viewModelScope.launch {
            val today = LocalDate.now().toString()
            val savedDate = settings.subhanallahDate.first()
            val savedCount = settings.subhanallahCount.first()

            if (savedDate == today) {
                _count.value = savedCount
                _isComplete.value = savedCount >= 100
            } else {
                // New day — reset
                _count.value = 0
                _isComplete.value = false
                settings.setSubhanallahCount(0)
                settings.setSubhanallahDate(today)
            }
        }
    }

    fun increment() {
        if (_isComplete.value) return
        val next = (_count.value + 1).coerceAtMost(100)
        _count.value = next
        viewModelScope.launch {
            val today = LocalDate.now().toString()
            settings.setSubhanallahCount(next)
            settings.setSubhanallahDate(today)
            if (next >= 100) _isComplete.value = true
        }
    }
}
