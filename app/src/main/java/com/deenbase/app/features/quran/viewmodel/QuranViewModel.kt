package com.deenbase.app.features.quran.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.deenbase.app.features.quran.data.QuranRepository
import com.deenbase.app.features.quran.data.SurahInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class QuranViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = QuranRepository()

    private val _surahs = MutableStateFlow<List<SurahInfo>>(emptyList())
    val surahs: StateFlow<List<SurahInfo>> = _surahs.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

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
