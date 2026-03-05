package com.deenbase.app.features.dhikr.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.deenbase.app.data.SettingsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

class DhikrViewModel(application: Application) : AndroidViewModel(application) {
    private val sm = SettingsManager(application)

    // In-memory count per session (resets when screen opens)
    private val _count = MutableStateFlow(0)
    val count: StateFlow<Int> = _count.asStateFlow()

    private val _isComplete = MutableStateFlow(false)
    val isComplete: StateFlow<Boolean> = _isComplete.asStateFlow()

    private var dhikrId: String = ""
    private var period: String = ""
    private val target = 3

    fun init(dhikrId: String, period: String) {
        this.dhikrId = dhikrId
        this.period = period
        _count.value = 0
        viewModelScope.launch {
            val today = LocalDate.now().toString()
            val savedDate = sm.getDhikrCompletionDate(dhikrId, period)
            _isComplete.value = savedDate == today
        }
    }

    fun increment() {
        if (_isComplete.value) return
        val newCount = _count.value + 1
        _count.value = newCount
        if (newCount >= target) {
            viewModelScope.launch {
                val today = LocalDate.now().toString()
                sm.setDhikrCompletionDate(dhikrId, period, today)
                _isComplete.value = true
            }
        }
    }
}
