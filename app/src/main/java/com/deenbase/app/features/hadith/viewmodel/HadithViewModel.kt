package com.deenbase.app.features.hadith.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.deenbase.app.BuildConfig
import com.deenbase.app.data.SettingsManager
import com.deenbase.app.features.hadith.data.Book
import com.deenbase.app.features.hadith.data.Chapter
import com.deenbase.app.features.hadith.data.Hadith
import com.deenbase.app.features.hadith.data.HadithRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

// ── Books screen state ────────────────────────────────────────────────────────
data class BooksUiState(
    val books: List<Book> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

// ── Chapters screen state ─────────────────────────────────────────────────────
data class ChaptersUiState(
    val chapters: List<Chapter> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

// ── Hadith list screen state ──────────────────────────────────────────────────
data class HadithListUiState(
    val hadiths: List<Hadith> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val currentPage: Int = 1,
    val hasMorePages: Boolean = false,
    val totalCount: Int = 0
)

// ── Search screen state ───────────────────────────────────────────────────────
data class SearchUiState(
    val hadiths: List<Hadith> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val currentPage: Int = 1,
    val hasMorePages: Boolean = false,
    val totalCount: Int = 0,
    val query: String = ""
)

class HadithViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsManager = SettingsManager(application)

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    // ── Books ─────────────────────────────────────────────────────────────────
    private val _booksState = MutableStateFlow(BooksUiState())
    val booksState: StateFlow<BooksUiState> = _booksState.asStateFlow()

    // ── Chapters ──────────────────────────────────────────────────────────────
    private val _chaptersState = MutableStateFlow(ChaptersUiState())
    val chaptersState: StateFlow<ChaptersUiState> = _chaptersState.asStateFlow()

    // ── Hadith list ───────────────────────────────────────────────────────────
    private val _hadithListState = MutableStateFlow(HadithListUiState())
    val hadithListState: StateFlow<HadithListUiState> = _hadithListState.asStateFlow()

    // ── Search ────────────────────────────────────────────────────────────────
    private val _searchState = MutableStateFlow(SearchUiState())
    val searchState: StateFlow<SearchUiState> = _searchState.asStateFlow()

    // Raw text field value — not used for auto-triggering searches
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Track whether a search has been run (for showing history vs results)
    private val _hasSearched = MutableStateFlow(false)
    val hasSearched: StateFlow<Boolean> = _hasSearched.asStateFlow()

    // Search history
    val searchHistory = settingsManager.hadithSearchHistory.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    private var searchJob: Job? = null

    // ── Selected items ────────────────────────────────────────────────────────
    private val _selectedHadith = MutableStateFlow<Hadith?>(null)
    val selectedHadith: StateFlow<Hadith?> = _selectedHadith.asStateFlow()

    private val _selectedBook = MutableStateFlow<Book?>(null)
    val selectedBook: StateFlow<Book?> = _selectedBook.asStateFlow()

    private val _selectedChapter = MutableStateFlow<Chapter?>(null)
    val selectedChapter: StateFlow<Chapter?> = _selectedChapter.asStateFlow()

    init {
        loadBooks()
    }

    // ── Books ─────────────────────────────────────────────────────────────────
    fun loadBooks() {
        _booksState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            HadithRepository.getBooks()
                .onSuccess { books -> _booksState.update { it.copy(books = books, isLoading = false) } }
                .onFailure { error -> _booksState.update { it.copy(isLoading = false, error = error.message ?: "Failed to load books") } }
        }
    }

    fun selectBook(book: Book) {
        val slug = book.slug
        if (slug.isNullOrBlank()) {
            _chaptersState.update { it.copy(isLoading = false, error = "Invalid book data. Please try another book.") }
            return
        }
        _selectedBook.value = book
        _chaptersState.value = ChaptersUiState()
        loadChapters(slug)
    }

    // ── Chapters ──────────────────────────────────────────────────────────────
    private fun loadChapters(bookSlug: String?) {
        if (bookSlug.isNullOrBlank()) {
            _chaptersState.update { it.copy(isLoading = false, error = "Invalid book.") }
            return
        }
        _chaptersState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            HadithRepository.getChapters(bookSlug)
                .onSuccess { chapters -> _chaptersState.update { it.copy(chapters = chapters, isLoading = false) } }
                .onFailure { error -> _chaptersState.update { it.copy(isLoading = false, error = error.message ?: "Failed to load chapters") } }
        }
    }

    fun retryChapters() {
        _selectedBook.value?.slug?.let { loadChapters(it) }
    }

    fun selectChapter(chapter: Chapter) {
        _selectedChapter.value = chapter
        _hadithListState.value = HadithListUiState()
        loadHadiths(reset = true)
    }

    // ── Hadith list ───────────────────────────────────────────────────────────
    private fun loadHadiths(reset: Boolean) {
        val book    = _selectedBook.value ?: return
        val chapter = _selectedChapter.value ?: return
        val state   = _hadithListState.value
        val page    = if (reset) 1 else state.currentPage + 1

        _hadithListState.update {
            if (reset) it.copy(isLoading = true, error = null, hadiths = emptyList())
            else it.copy(isLoadingMore = true, error = null)
        }

        viewModelScope.launch {
            HadithRepository.getHadiths(
                book    = book.slug,
                chapter = chapter.chapterNumber.toIntOrNull(),
                page    = page
            ).onSuccess { response ->
                val newHadiths = response.hadiths.data
                val allHadiths = if (reset) newHadiths else state.hadiths + newHadiths
                _hadithListState.update {
                    it.copy(hadiths = allHadiths, isLoading = false, isLoadingMore = false, currentPage = response.hadiths.currentPage, hasMorePages = response.hadiths.nextPageUrl != null, totalCount = response.hadiths.total, error = null)
                }
            }.onFailure { error ->
                _hadithListState.update { it.copy(isLoading = false, isLoadingMore = false, error = error.message ?: "Failed to load hadiths") }
            }
        }
    }

    fun loadMoreHadiths() {
        val state = _hadithListState.value
        if (state.isLoadingMore || !state.hasMorePages) return
        loadHadiths(reset = false)
    }

    fun retryHadiths() { loadHadiths(reset = true) }

    // ── Search ────────────────────────────────────────────────────────────────

    // Just updates the text field — no search triggered
    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            _hasSearched.value = false
            _searchState.value = SearchUiState()
        }
    }

    // Called when user hits the IME search button
    fun search() {
        val q = _searchQuery.value.trim()
        if (q.isBlank()) return
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _hasSearched.value = true
            _searchState.update { it.copy(isLoading = true, error = null, hadiths = emptyList(), query = q) }
            try {
                val expandedKeywords = withContext(Dispatchers.IO) { expandQueryWithGroq(q) }
                loadSearchResults(reset = true, keywords = expandedKeywords)
            } catch (e: Exception) {
                // Groq failed — fall back to raw query
                loadSearchResults(reset = true, keywords = q)
            }
        }
    }

    // Run a search from history (tapping a history item)
    fun searchFromHistory(query: String) {
        _searchQuery.value = query
        _hasSearched.value = true
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _searchState.update { it.copy(isLoading = true, error = null, hadiths = emptyList(), query = query) }
            try {
                val expandedKeywords = withContext(Dispatchers.IO) { expandQueryWithGroq(query) }
                loadSearchResults(reset = true, keywords = expandedKeywords)
            } catch (e: Exception) {
                loadSearchResults(reset = true, keywords = query)
            }
        }
    }

    fun loadMoreSearch() {
        val state = _searchState.value
        if (state.isLoadingMore || !state.hasMorePages || state.query.isBlank()) return
        viewModelScope.launch {
            loadSearchResults(reset = false, keywords = state.query)
        }
    }

    fun removeHistoryItem(query: String) {
        viewModelScope.launch { settingsManager.removeHadithSearchHistoryItem(query) }
    }

    fun clearHistory() {
        viewModelScope.launch { settingsManager.clearHadithSearchHistory() }
    }

    // ── Groq keyword expansion ────────────────────────────────────────────────
    private fun expandQueryWithGroq(userQuery: String): String {
        val prompt = """
            You are a hadith search assistant. Your job is to expand a user's search query into the best FTS5 keyword string for searching an English hadith database.
            
            Rules:
            - Return ONLY the keyword string, nothing else — no explanation, no punctuation, no quotes
            - Include synonyms, related Islamic terms, and relevant English words
            - Include transliterations of Arabic terms if relevant (e.g. salah prayer, sawm fasting, zakat charity)
            - Keep it concise: 3–8 keywords max
            - Example: "what does Islam say about patience" → "patience sabr hardship trial perseverance endure"
            
            Query: $userQuery
        """.trimIndent()

        val body = JSONObject()
            .put("model", "llama-3.3-70b-versatile")
            .put("max_tokens", 60)
            .put("messages", JSONArray()
                .put(JSONObject().put("role", "user").put("content", prompt))
            )

        val request = Request.Builder()
            .url("https://api.groq.com/openai/v1/chat/completions")
            .addHeader("Authorization", "Bearer ${BuildConfig.GROQ_API_KEY}")
            .addHeader("Content-Type", "application/json")
            .post(body.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: return userQuery
        if (!response.isSuccessful) return userQuery

        return JSONObject(responseBody)
            .getJSONArray("choices")
            .getJSONObject(0)
            .getJSONObject("message")
            .getString("content")
            .trim()
            .ifBlank { userQuery }
    }

    private suspend fun loadSearchResults(reset: Boolean, keywords: String) {
        val state = _searchState.value
        val page  = if (reset) 1 else state.currentPage + 1

        if (!reset) {
            _searchState.update { it.copy(isLoadingMore = true, error = null) }
        }

        HadithRepository.getHadiths(
            searchEnglish = keywords,
            page          = page
        ).onSuccess { response ->
            val newHadiths = response.hadiths.data
            val allHadiths = if (reset) newHadiths else state.hadiths + newHadiths
            // Save to history only on a successful fresh search
            if (reset && newHadiths.isNotEmpty()) {
                settingsManager.addHadithSearchHistoryItem(_searchQuery.value.trim())
            }
            _searchState.update {
                it.copy(hadiths = allHadiths, isLoading = false, isLoadingMore = false, currentPage = response.hadiths.currentPage, hasMorePages = response.hadiths.nextPageUrl != null, totalCount = response.hadiths.total, error = null)
            }
        }.onFailure { error ->
            _searchState.update { it.copy(isLoading = false, isLoadingMore = false, error = error.message ?: "Search failed") }
        }
    }

    // ── Detail navigation ─────────────────────────────────────────────────────
    fun selectHadith(hadith: Hadith) {
        _selectedHadith.value = hadith
    }
}
