package com.deenbase.app.features.hadith.data

// ── Books ─────────────────────────────────────────────────────────────────────

data class Book(
    val id: Int = 0,
    val bookName: String,
    val slug: String,
    val bookNameArabic: String? = null,
    val hadithsCount: Int = 0
)

// ── Chapters ──────────────────────────────────────────────────────────────────

data class Chapter(
    val id: Int,
    val chapterNumber: String,          // kept as String to match UI usage
    val chapterEnglish: String = "",    // non-null — UI calls ?: directly
    val chapterArabic: String? = null,
    val bookSlug: String = ""
)

// ── Hadiths ───────────────────────────────────────────────────────────────────

data class Hadith(
    val id: Int,
    val hadithNumber: String,
    val englishNarrator: String = "",   // non-null — UI calls .isNotBlank()
    val hadithEnglish: String = "",     // non-null — UI calls .isNotBlank()
    val hadithUrdu: String = "",        // non-null — UI calls .isNotBlank()
    val hadithArabic: String = "",      // non-null — UI uses directly
    val urduNarrator: String = "",      // non-null — UI calls .isNotBlank()
    val headingEnglish: String? = null,
    val headingUrdu: String? = null,
    val headingArabic: String? = null,
    val chapterNumber: String? = null,
    val chapterEnglish: String? = null,
    val bookSlug: String,
    val book: BookInfo? = null,
    val status: String? = null          // grade string: "Sahih", "Hasan", "Da'if" etc.
)

data class BookInfo(
    val bookName: String,
    val slug: String
)

// ── Pagination wrapper (keeps ViewModel unchanged) ────────────────────────────

data class HadithsResponse(
    val hadiths: HadithPagination
)

data class HadithPagination(
    val currentPage: Int,
    val data: List<Hadith>,
    val nextPageUrl: String?,
    val total: Int,
    val perPage: Int = 20,
    val lastPage: Int = 1
)
