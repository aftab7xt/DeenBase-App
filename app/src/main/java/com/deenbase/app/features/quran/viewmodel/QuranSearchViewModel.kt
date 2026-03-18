package com.deenbase.app.features.quran.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.deenbase.app.BuildConfig
import com.deenbase.app.data.SettingsManager
import com.deenbase.app.features.quran.data.QuranRepository
import com.deenbase.app.features.quran.data.Verse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class QuranSearchViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = QuranRepository()
    private val settingsManager = SettingsManager(application)

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    val arabicFontStyle = settingsManager.arabicFontStyle.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), "indopak_nastaleeq"
    )
    val arabicFontSize = settingsManager.arabicFontSize.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), 32f
    )

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _results = MutableStateFlow<List<Verse>>(emptyList())
    val results: StateFlow<List<Verse>> = _results.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _hasSearched = MutableStateFlow(false)
    val hasSearched: StateFlow<Boolean> = _hasSearched.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var searchJob: Job? = null

    // Just updates the text field — no API call
    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
        if (newQuery.isBlank()) {
            _results.value = emptyList()
            _hasSearched.value = false
            _error.value = null
        }
    }

    // Called only when user taps the search key on keyboard
    fun search() {
        val q = _query.value.trim()
        if (q.isBlank()) return
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _isLoading.value = true
            _hasSearched.value = true
            _error.value = null
            try {
                val lang = settingsManager.translationLang.first()
                val refs = withContext(Dispatchers.IO) { askGeminiForRefs(q) }
                val verses = withContext(Dispatchers.IO) { repository.fetchVersesByReferences(refs, lang) }
                _results.value = verses
            } catch (e: Exception) {
                _error.value = e.javaClass.simpleName + ": " + (e.message ?: "unknown")
                _results.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun askGeminiForRefs(query: String): List<Pair<Int, Int>> {
        val prompt = """
            You are a Quran reference assistant. Given a search query, return the most relevant Quran verse references.
            
            Rules:
            - Return ONLY a valid JSON array, nothing else
            - No markdown, no backticks, no explanation
            - Format exactly: [{"surah":2,"verse":255},{"surah":3,"verse":18}]
            - Return 5 to 10 of the most relevant verses
            - If query mentions a specific surah or verse number, include it
            
            Query: $query
        """.trimIndent()

        val requestBody = JSONObject()
            .put("contents", JSONArray()
                .put(JSONObject()
                    .put("parts", JSONArray()
                        .put(JSONObject().put("text", prompt))
                    )
                )
            )

        val request = Request.Builder()
            .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=${BuildConfig.GEMINI_API_KEY}")
            .addHeader("Content-Type", "application/json")
            .post(requestBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val response = client.newCall(request).execute()
        val body = response.body?.string() ?: error("Empty Gemini response")
        if (!response.isSuccessful) error("Gemini HTTP ${response.code}")

        val text = JSONObject(body)
            .getJSONArray("candidates")
            .getJSONObject(0)
            .getJSONObject("content")
            .getJSONArray("parts")
            .getJSONObject(0)
            .getString("text")
            .trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()

        val jsonArray = JSONArray(text)
        return (0 until jsonArray.length()).mapNotNull { i ->
            val obj = jsonArray.getJSONObject(i)
            val surah = obj.optInt("surah", 0)
            val verse = obj.optInt("verse", 0)
            if (surah in 1..114 && verse > 0) Pair(surah, verse) else null
        }
    }
}
