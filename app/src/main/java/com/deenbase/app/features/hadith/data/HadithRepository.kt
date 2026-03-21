package com.deenbase.app.features.hadith.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

object HadithRepository {

    private val TURSO_URL   = com.deenbase.app.BuildConfig.TURSO_URL
    private val TURSO_TOKEN = com.deenbase.app.BuildConfig.TURSO_TOKEN

    private val PAGE_SIZE = 20

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val JSON_MEDIA = "application/json".toMediaType()

    private suspend fun query(sql: String, args: List<Any?> = emptyList()): List<Map<String, String?>> = withContext(Dispatchers.IO) {
        val argsArray = JSONArray()
        for (arg in args) {
            argsArray.put(when (arg) {
                null    -> JSONObject().put("type", "null")
                is Int  -> JSONObject().put("type", "integer").put("value", arg.toString())
                is Long -> JSONObject().put("type", "integer").put("value", arg.toString())
                else    -> JSONObject().put("type", "text").put("value", arg.toString())
            })
        }

        val stmt = JSONObject().put("sql", sql).put("args", argsArray)

        val body = JSONObject()
            .put("requests", JSONArray()
                .put(JSONObject().put("type", "execute").put("stmt", stmt))
                .put(JSONObject().put("type", "close"))
            )

        val request = Request.Builder()
            .url(TURSO_URL)
            .addHeader("Authorization", "Bearer $TURSO_TOKEN")
            .addHeader("Content-Type", "application/json")
            .post(body.toString().toRequestBody(JSON_MEDIA))
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: error("Empty response from Turso")
        if (!response.isSuccessful) error("Turso HTTP ${response.code}: $responseBody")

        val json   = JSONObject(responseBody)
        val result = json.getJSONArray("results")
            .getJSONObject(0)
            .getJSONObject("response")
            .getJSONObject("result")

        val cols     = result.getJSONArray("cols")
        val rows     = result.getJSONArray("rows")
        val colNames = (0 until cols.length()).map { cols.getJSONObject(it).getString("name") }

        (0 until rows.length()).map { rowIdx ->
            val row = rows.getJSONArray(rowIdx)
            colNames.mapIndexed { colIdx, name ->
                val cell = row.getJSONObject(colIdx)
                name to if (cell.getString("type") == "null") null else cell.optString("value")
            }.toMap()
        }
    }

    private fun Throwable.toUserMessage(): String = when (this) {
        is UnknownHostException   -> "No internet connection."
        is SocketTimeoutException -> "Connection timed out. Check your connection."
        else -> message ?: "Something went wrong. Please try again."
    }

    // ── Books ─────────────────────────────────────────────────────────────────

    suspend fun getBooks(): Result<List<Book>> = runCatching {
        val rows = query("""
            SELECT b.edition, b.name_english, b.name_arabic,
                   (SELECT COUNT(*) FROM hadiths h WHERE h.book_edition = b.edition) AS hadith_count
            FROM books b
            ORDER BY b.rowid
        """.trimIndent())

        rows.map { row ->
            Book(
                bookName       = row["name_english"] ?: "",
                slug           = row["edition"] ?: "",
                bookNameArabic = row["name_arabic"],
                hadithsCount   = row["hadith_count"]?.toIntOrNull() ?: 0
            )
        }
    }.recoverCatching { error(it.toUserMessage()) }

    // ── Chapters ──────────────────────────────────────────────────────────────

    suspend fun getChapters(bookSlug: String): Result<List<Chapter>> = runCatching {
        val rows = query(
            "SELECT id, chapter_number, name_english, name_arabic FROM chapters WHERE book_edition = ? AND chapter_number != 0 ORDER BY chapter_number",
            listOf(bookSlug)
        )
        rows.map { row ->
            Chapter(
                id             = row["id"]?.toIntOrNull() ?: 0,
                chapterNumber  = row["chapter_number"] ?: "0",
                chapterEnglish = row["name_english"] ?: "",
                chapterArabic  = row["name_arabic"],
                bookSlug       = bookSlug
            )
        }
    }.recoverCatching { error(it.toUserMessage()) }

    // ── Hadiths (paginated) ───────────────────────────────────────────────────

    suspend fun getHadiths(
        book: String?          = null,
        chapter: Int?          = null,
        page: Int              = 1,
        paginate: Int          = PAGE_SIZE,
        searchEnglish: String? = null,
        searchUrdu: String?    = null,
        searchArabic: String?  = null
    ): Result<HadithsResponse> = runCatching {

        val offset = (page - 1) * paginate

        if (searchEnglish != null) {
            // ── FTS5 search ───────────────────────────────────────────────────
            // Each word becomes a prefix token joined with OR so any match returns results.
            // e.g. "forgiveness pardon mercy" → "forgiveness* OR pardon* OR mercy*"
            val ftsQuery = searchEnglish.trim()
                .split("\\s+".toRegex())
                .filter { it.isNotBlank() }
                .joinToString(" OR ") { "${it.replace("\"", "")}*" }

            val countRows = query(
                "SELECT COUNT(*) AS c FROM hadiths_fts WHERE hadiths_fts MATCH ?",
                listOf(ftsQuery)
            )
            val total = countRows.firstOrNull()?.get("c")?.toIntOrNull() ?: 0

            val rows = query("""
                SELECT h.id, h.book_edition, h.hadith_number, h.chapter_number,
                       h.text_english, h.text_arabic, h.grade,
                       b.name_english AS book_name,
                       c.name_english AS chapter_name
                FROM hadiths_fts
                JOIN hadiths  h ON hadiths_fts.rowid = h.id
                JOIN books    b ON h.book_edition = b.edition
                LEFT JOIN chapters c ON h.book_edition = c.book_edition
                                    AND h.chapter_number = c.chapter_number
                WHERE hadiths_fts MATCH ?
                ORDER BY rank
                LIMIT ? OFFSET ?
            """.trimIndent(), listOf(ftsQuery, paginate, offset))

            val hadiths = rows.toHadiths()
            val hasMore = (offset + hadiths.size) < total

            HadithsResponse(HadithPagination(
                currentPage = page,
                data        = hadiths,
                nextPageUrl = if (hasMore) "next" else null,
                total       = total,
                perPage     = paginate,
                lastPage    = if (total == 0) 1 else (total + paginate - 1) / paginate
            ))

        } else {
            // ── Chapter browse ────────────────────────────────────────────────
            requireNotNull(book)    { "book must be provided for chapter browse" }
            requireNotNull(chapter) { "chapter must be provided for chapter browse" }

            val countRows = query(
                "SELECT COUNT(*) AS c FROM hadiths WHERE book_edition = ? AND chapter_number = ?",
                listOf(book, chapter)
            )
            val total = countRows.firstOrNull()?.get("c")?.toIntOrNull() ?: 0

            val (whereClause, queryArgs, countTotal) = if (total > 0) {
                Triple("h.book_edition = ? AND h.chapter_number = ?", listOf(book, chapter, paginate, offset), total)
            } else {
                val allCount = query("SELECT COUNT(*) AS c FROM hadiths WHERE book_edition = ?", listOf(book))
                    .firstOrNull()?.get("c")?.toIntOrNull() ?: 0
                Triple("h.book_edition = ?", listOf(book, paginate, offset), allCount)
            }

            val rows = query("""
                SELECT h.id, h.book_edition, h.hadith_number, h.chapter_number,
                       h.text_english, h.text_arabic, h.grade,
                       b.name_english AS book_name,
                       c.name_english AS chapter_name
                FROM hadiths h
                JOIN books b ON h.book_edition = b.edition
                LEFT JOIN chapters c ON h.book_edition = c.book_edition AND h.chapter_number = c.chapter_number
                WHERE $whereClause
                ORDER BY h.hadith_number
                LIMIT ? OFFSET ?
            """.trimIndent(), queryArgs)

            val hadiths = rows.toHadiths()
            val hasMore = (offset + hadiths.size) < countTotal

            HadithsResponse(HadithPagination(
                currentPage = page,
                data        = hadiths,
                nextPageUrl = if (hasMore) "next" else null,
                total       = countTotal,
                perPage     = paginate,
                lastPage    = if (countTotal == 0) 1 else (countTotal + paginate - 1) / paginate
            ))
        }
    }.recoverCatching { error("DB error: ${it.javaClass.simpleName}: ${it.message}") }

    // ── Daily Hadith ──────────────────────────────────────────────────────────

    suspend fun getDailyHadith(dayOfYear: Int): Result<Hadith?> = runCatching {
        val offset = dayOfYear % 35903
        val rows = query("""
            SELECT h.id, h.book_edition, h.hadith_number, h.chapter_number,
                   h.text_english, h.text_arabic, h.grade,
                   b.name_english AS book_name,
                   c.name_english AS chapter_name
            FROM hadiths h
            JOIN books b ON h.book_edition = b.edition
            LEFT JOIN chapters c ON h.book_edition = c.book_edition
                                 AND h.chapter_number = c.chapter_number
            LIMIT 1 OFFSET ?
        """.trimIndent(), listOf(offset))

        rows.firstOrNull()?.let { row ->
            val bookEdition = row["book_edition"] ?: ""
            Hadith(
                id              = row["id"]?.toIntOrNull() ?: 0,
                hadithNumber    = row["hadith_number"] ?: "",
                hadithEnglish   = row["text_english"] ?: "",
                hadithArabic    = row["text_arabic"] ?: "",
                hadithUrdu      = "",
                englishNarrator = "",
                urduNarrator    = "",
                chapterNumber   = row["chapter_number"],
                chapterEnglish  = row["chapter_name"],
                bookSlug        = bookEdition,
                status          = row["grade"],
                book            = BookInfo(bookName = row["book_name"] ?: bookEdition, slug = bookEdition)
            )
        }
    }.recoverCatching { error(it.toUserMessage()) }

    // ── Row → Hadith mapping ──────────────────────────────────────────────────

    private fun List<Map<String, String?>>.toHadiths(): List<Hadith> = map { row ->
        val bookEdition = row["book_edition"] ?: ""
        Hadith(
            id              = row["id"]?.toIntOrNull() ?: 0,
            hadithNumber    = row["hadith_number"] ?: "",
            hadithEnglish   = row["text_english"] ?: "",
            hadithArabic    = row["text_arabic"] ?: "",
            hadithUrdu      = "",
            englishNarrator = "",
            urduNarrator    = "",
            chapterNumber   = row["chapter_number"],
            chapterEnglish  = row["chapter_name"],
            bookSlug        = bookEdition,
            status          = row["grade"],
            book            = BookInfo(bookName = row["book_name"] ?: bookEdition, slug = bookEdition)
        )
    }
}
