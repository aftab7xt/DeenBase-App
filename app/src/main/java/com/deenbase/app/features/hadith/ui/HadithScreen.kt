package com.deenbase.app.features.hadith.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.deenbase.app.R
import com.deenbase.app.features.hadith.data.Book
import com.deenbase.app.features.hadith.viewmodel.HadithViewModel

private fun calligraphyRes(bookName: String): Int? = when {
    bookName.contains("Bukhari",  ignoreCase = true)                              -> R.drawable.ic_bukhari
    bookName.contains("Muslim",   ignoreCase = true)                              -> R.drawable.ic_muslim
    bookName.contains("Dawud",    ignoreCase = true)                              -> R.drawable.ic_abu_dawud
    bookName.contains("Tirmidhi", ignoreCase = true)                              -> R.drawable.ic_at_tirmidhi
    bookName.contains("Nasai",    ignoreCase = true) ||
    bookName.contains("Nasa'i",   ignoreCase = true)                              -> R.drawable.ic_an_nasai
    bookName.contains("Majah",    ignoreCase = true)                              -> R.drawable.ic_ibn_majah
    bookName.contains("Malik",    ignoreCase = true)                              -> R.drawable.ic_muwatta_malik
    else                                                                           -> null
}

// Corner radius for connected grid cells
// isFullWidth = true when the item spans both columns (last odd item)
private fun cornerShape(
    index: Int,
    total: Int,
    isFullWidth: Boolean
): RoundedCornerShape {
    if (isFullWidth) return RoundedCornerShape(
        topStart = 4.dp, topEnd = 4.dp,
        bottomStart = 20.dp, bottomEnd = 20.dp
    )

    val isLeft     = index % 2 == 0
    val isRight    = index % 2 == 1
    val isFirstRow = index < 2
    // Whether there's something below — if total is odd and this is in the second-to-last row,
    // the full-width item is below, so corners stay inner
    val hasBelow   = index + 2 < total

    val topStart    : Dp = if (isFirstRow && isLeft) 20.dp else 4.dp
    val topEnd      : Dp = if (isFirstRow && isRight) 20.dp else 4.dp
    val bottomStart : Dp = if (isLeft && !hasBelow) 4.dp else 4.dp
    val bottomEnd   : Dp = if (isRight && !hasBelow) 4.dp else 4.dp

    return RoundedCornerShape(
        topStart    = topStart,
        topEnd      = topEnd,
        bottomStart = bottomStart,
        bottomEnd   = bottomEnd
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HadithScreen(
    onNavigateBack: () -> Unit,
    onBookClick: (Book) -> Unit,
    onSearchClick: () -> Unit,
    viewModel: HadithViewModel = viewModel()
) {
    val state = viewModel.booksState.collectAsState().value
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(MaterialTheme.colorScheme.background, Color.Transparent)
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Hadiths",
                        style      = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onSearchClick) {
                        Icon(Icons.Filled.Search, contentDescription = "Search")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                state.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularWavyProgressIndicator()
                    }
                }
                state.error != null -> {
                    Column(
                        modifier            = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("Failed to load books", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text      = state.error!!,
                            style     = MaterialTheme.typography.bodySmall,
                            color     = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier  = Modifier.padding(horizontal = 32.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = viewModel::loadBooks) { Text("Retry") }
                    }
                }
                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(
                            top    = innerPadding.calculateTopPadding() + 8.dp,
                            bottom = innerPadding.calculateBottomPadding()
                        ),
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalArrangement   = Arrangement.spacedBy(2.dp)
                    ) {
                        val isOddTotal = state.books.size % 2 != 0
                        itemsIndexed(
                            items = state.books,
                            span  = { index, _ ->
                                // Last item spans full width if total is odd
                                if (isOddTotal && index == state.books.lastIndex)
                                    GridItemSpan(2)
                                else
                                    GridItemSpan(1)
                            }
                        ) { index, book ->
                            val isFullWidth = isOddTotal && index == state.books.lastIndex
                            BookCard(
                                book        = book,
                                index       = index,
                                total       = state.books.size,
                                isFullWidth = isFullWidth,
                                onClick     = { onBookClick(book) }
                            )
                        }
                    }
                }
            }

            // Top gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(innerPadding.calculateTopPadding() + 32.dp)
                    .background(brush = gradientBrush)
                    .align(Alignment.TopCenter)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun BookCard(
    book: Book,
    index: Int,
    total: Int,
    isFullWidth: Boolean,
    onClick: () -> Unit
) {
    val calligraphyRes = calligraphyRes(book.bookName)
    val shape          = cornerShape(index, total, isFullWidth)

    Card(
        onClick  = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .then(if (isFullWidth) Modifier.height(140.dp) else Modifier.aspectRatio(1f)),
        shape  = shape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        if (isFullWidth) {
            // Full width — calligraphy left, text right
            Row(
                modifier              = Modifier.fillMaxSize().padding(24.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Box(
                    modifier         = Modifier.weight(1f).fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    if (calligraphyRes != null) {
                        Image(
                            painter            = painterResource(id = calligraphyRes),
                            contentDescription = book.bookName,
                            contentScale       = ContentScale.Fit,
                            colorFilter        = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                            modifier           = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text(
                            text  = "☽",
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                        )
                    }
                }
                Column(
                    modifier            = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text       = book.bookName,
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text  = "${book.hadithsCount} hadiths",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            // Normal square card — calligraphy top, text bottom
            Column(
                modifier            = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier         = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    if (calligraphyRes != null) {
                        Image(
                            painter            = painterResource(id = calligraphyRes),
                            contentDescription = book.bookName,
                            contentScale       = ContentScale.Fit,
                            colorFilter        = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                            modifier           = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    } else {
                        Text(
                            text  = "☽",
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                        )
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text       = book.bookName,
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines   = 2,
                        textAlign  = TextAlign.Start
                    )
                    Text(
                        text  = "${book.hadithsCount} hadiths",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
