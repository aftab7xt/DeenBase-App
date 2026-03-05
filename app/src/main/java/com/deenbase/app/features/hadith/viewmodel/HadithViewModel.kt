package com.deenbase.app.features.hadith.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deenbase.app.features.hadith.data.Book
import com.deenbase.app.features.hadith.data.Chapter
import com.deenbase.app.features.hadith.data.Hadith
import com.deenbase.app.features.hadith.data.HadithRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

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

@OptIn(FlowPreview::class)
class HadithViewModel : ViewModel() {

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

    private val _searchQuery = MutableStateFlow("")

    // ── Selected items (for detail navigation) ────────────────────────────────
    private val _selectedHadith = MutableStateFlow<Hadith?>(null)
    val selectedHadith: StateFlow<Hadith?> = _selectedHadith.asStateFlow()

    private val _selectedBook = MutableStateFlow<Book?>(null)
    val selectedBook: StateFlow<Book?> = _selectedBook.asStateFlow()

    private val _selectedChapter = MutableStateFlow<Chapter?>(null)
    val selectedChapter: StateFlow<Chapter?> = _selectedChapter.asStateFlow()

    init {
        loadBooks()

        // Debounce search
        viewModelScope.launch {
            _searchQuery
                .debounce(500)
                .distinctUntilChanged()
                .collect { query ->
                    if (query.isNotBlank()) {
                        _searchState.update { it.copy(query = query) }
                        loadSearchResults(reset = true)
                    } else {
                        _searchState.value = SearchUiState()
                    }
                }
        }
    }

    // ── Books ─────────────────────────────────────────────────────────────────
    fun loadBooks() {
        _booksState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            HadithRepository.getBooks()
                .onSuccess { books ->
                    _booksState.update { it.copy(books = books, isLoading = false) }
                }
                .onFailure { error ->
                    _booksState.update { it.copy(isLoading = false, error = error.message ?: "Failed to load books") }
                }
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
                .onSuccess { chapters ->
                    _chaptersState.update { it.copy(chapters = chapters, isLoading = false) }
                }
                .onFailure { error ->
                    _chaptersState.update { it.copy(isLoading = false, error = error.message ?: "Failed to load chapters") }
                }
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
        val book = _selectedBook.value ?: return
        val chapter = _selectedChapter.value ?: return
        val state = _hadithListState.value
        val page = if (reset) 1 else state.currentPage + 1

        _hadithListState.update {
            if (reset) it.copy(isLoading = true, error = null, hadiths = emptyList())
            else it.copy(isLoadingMore = true, error = null)
        }

        viewModelScope.launch {
            HadithRepository.getHadiths(
                book = book.slug,
                chapter = chapter.chapterNumber.toIntOrNull(),
                page = page
            ).onSuccess { response ->
                val newHadiths = response.hadiths.data
                val allHadiths = if (reset) newHadiths else state.hadiths + newHadiths
                _hadithListState.update {
                    it.copy(
                        hadiths = allHadiths,
                        isLoading = false,
                        isLoadingMore = false,
                        currentPage = response.hadiths.currentPage,
                        hasMorePages = response.hadiths.nextPageUrl != null,
                        totalCount = response.hadiths.total,
                        error = null
                    )
                }
            }.onFailure { error ->
                _hadithListState.update {
                    it.copy(
                        isLoading = false,
                        isLoadingMore = false,
                        error = error.message ?: "Failed to load hadiths"
                    )
                }
            }
        }
    }

    fun loadMoreHadiths() {
        val state = _hadithListState.value
        if (state.isLoadingMore || !state.hasMorePages) return
        loadHadiths(reset = false)
    }

    fun retryHadiths() {
        loadHadiths(reset = true)
    }

    // ── Search ────────────────────────────────────────────────────────────────
    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) _searchState.value = SearchUiState()
    }

    fun loadMoreSearch() {
        val state = _searchState.value
        if (state.isLoadingMore || !state.hasMorePages) return
        loadSearchResults(reset = false)
    }

    private fun loadSearchResults(reset: Boolean) {
        val query = _searchQuery.value.trim()
        if (query.isBlank()) return
        val state = _searchState.value
        val page = if (reset) 1 else state.currentPage + 1

        _searchState.update {
            if (reset) it.copy(isLoading = true, error = null, hadiths = emptyList())
            else it.copy(isLoadingMore = true, error = null)
        }

        viewModelScope.launch {
            HadithRepository.getHadiths(
                searchEnglish = query,
                page = page
            ).onSuccess { response ->
                val newHadiths = response.hadiths.data
                val allHadiths = if (reset) newHadiths else state.hadiths + newHadiths
                _searchState.update {
                    it.copy(
                        hadiths = allHadiths,
                        isLoading = false,
                        isLoadingMore = false,
                        currentPage = response.hadiths.currentPage,
                        hasMorePages = response.hadiths.nextPageUrl != null,
                        totalCount = response.hadiths.total,
                        error = null
                    )
                }
            }.onFailure { error ->
                _searchState.update {
                    it.copy(
                        isLoading = false,
                        isLoadingMore = false,
                        error = error.message ?: "Search failed"
                    )
                }
            }
        }
    }

    // ── Detail navigation ─────────────────────────────────────────────────────
    fun selectHadith(hadith: Hadith) {
        _selectedHadith.value = hadith
    }
}
