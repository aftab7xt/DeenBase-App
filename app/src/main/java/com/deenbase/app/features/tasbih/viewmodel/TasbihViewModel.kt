package com.deenbase.app.features.tasbih.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class TasbihViewModel : ViewModel() {
    // This is the "Source of Truth" for the count
    var count = mutableStateOf(0)
        private set

    fun increment() {
        count.value++
    }

    fun reset() {
        count.value = 0
    }
}
