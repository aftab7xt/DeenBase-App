package com.deenbase.app.features.quran.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.deenbase.app.R
import com.deenbase.app.features.quran.data.Verse
import com.deenbase.app.features.quran.viewmodel.QuranSearchViewModel
import com.deenbase.app.ui.springOverscroll

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun QuranSearchScreen(
    onBack: () -> Unit,
    onVerseClick: (surahId: Int, surahName: String, verseNumber: Int) -> Unit,
    viewModel: QuranSearchViewModel = viewModel()
) {
    val query           by viewModel.query.collectAsState()
    val results         by viewModel.results.collectAsState()
    val isLoading       by viewModel.isLoading.collectAsState()
    val hasSearched     by viewModel.hasSearched.collectAsState()
    val error           by viewModel.error.collectAsState()
    val arabicFontStyle by viewModel.arabicFontStyle.collectAsState()
    val arabicFontSize  by viewModel.arabicFontSize.collectAsState()
    val searchHistory   by viewModel.searchHistory.collectAsState()

    val arabicFontFamily = remember(arabicFontStyle) {
        val fontRes = when (arabicFontStyle) {
            "indopak_nastaleeq" -> R.font.indopak_nastaleeq
            else                -> R.font.hafs_uthmanic_regular
        }
        FontFamily(Font(fontRes))
    }

    val focusRequester     = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    val gradientBrush = Brush.verticalGradient(
        colors = listOf(MaterialTheme.colorScheme.background, Color.Transparent)
    )

    // Only auto-focus when opening fresh — not when returning from a verse
    LaunchedEffect(hasSearched) {
        if (!hasSearched) focusRequester.requestFocus()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    OutlinedTextField(
                        value         = query,
                        onValueChange = { viewModel.onQueryChange(it) },
                        placeholder   = { Text("Search Quran...") },
                        trailingIcon  = {
                            if (query.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onQueryChange("") }) {
                                    Icon(Icons.Filled.Close, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine      = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = {
                            keyboardController?.hide()
                            viewModel.search()
                        }),
                        shape    = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor     = MaterialTheme.colorScheme.outlineVariant,
                            focusedBorderColor       = MaterialTheme.colorScheme.primary,
                            focusedContainerColor    = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.7f),
                            unfocusedContainerColor  = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.7f)
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().springOverscroll()) {
            when {
                // ── Loading ───────────────────────────────────────────────────
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            LoadingIndicator(
                                modifier = Modifier.size(64.dp),
                                color    = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Searching...",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // ── Error ─────────────────────────────────────────────────────
                error != null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            error!!,
                            style     = MaterialTheme.typography.bodyMedium,
                            color     = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            modifier  = Modifier.padding(32.dp)
                        )
                    }
                }

                // ── Pre-search: history or empty state ────────────────────────
                !hasSearched -> {
                    if (searchHistory.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector        = Icons.Filled.Search,
                                    contentDescription = null,
                                    modifier           = Modifier.size(48.dp),
                                    tint               = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    "Search by topic, theme, or keyword",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    "Powered by Llama 3.3 on Groq",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            item { Spacer(modifier = Modifier.height(innerPadding.calculateTopPadding() + 4.dp)) }
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 4.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment     = Alignment.CenterVertically
                                ) {
                                    Text("Recent", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                                    TextButton(onClick = { viewModel.clearHistory() }) {
                                        Text("Clear all", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                            itemsIndexed(searchHistory) { index, item ->
                                val isFirst      = index == 0
                                val isLast       = index == searchHistory.lastIndex
                                val topRadius    = if (isFirst) 20.dp else 4.dp
                                val bottomRadius = if (isLast) 20.dp else 4.dp
                                val interactionSource = remember { MutableInteractionSource() }

                                ListItem(
                                    headlineContent = { Text(text = item, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Medium) },
                                    leadingContent  = { Icon(imageVector = Icons.Filled.History, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp)) },
                                    trailingContent = {
                                        IconButton(onClick = { viewModel.removeHistoryItem(item) }) {
                                            Icon(imageVector = Icons.Filled.Close, contentDescription = "Remove", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                                        }
                                    },
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(topStart = topRadius, topEnd = topRadius, bottomStart = bottomRadius, bottomEnd = bottomRadius))
                                        .clickable(interactionSource = interactionSource, indication = ripple()) {
                                            keyboardController?.hide()
                                            viewModel.searchFromHistory(item)
                                        },
                                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                                )
                                if (!isLast) Spacer(modifier = Modifier.height(2.dp))
                            }
                            item { Spacer(modifier = Modifier.height(100.dp)) }
                        }
                    }
                }

                // ── No results ────────────────────────────────────────────────
                results.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No results found", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // ── Results ───────────────────────────────────────────────────
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        item { Spacer(modifier = Modifier.height(innerPadding.calculateTopPadding() + 4.dp)) }
                        itemsIndexed(results) { index, verse ->
                            SearchResultItem(
                                verse            = verse,
                                index            = index,
                                totalItems       = results.size,
                                arabicFontFamily = arabicFontFamily,
                                arabicFontSize   = arabicFontSize,
                                onClick          = { onVerseClick(verse.surahId, verse.surahName, verse.verseNumber) }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(100.dp)) }
                    }
                }
            }

            // ── Top gradient — content scrolls under the search bar ───────────
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

@Composable
private fun SearchResultItem(
    verse: Verse,
    index: Int,
    totalItems: Int,
    arabicFontFamily: FontFamily,
    arabicFontSize: Float,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val topRadius    = if (index == 0) 20.dp else 4.dp
    val bottomRadius = if (index == totalItems - 1) 20.dp else 4.dp

    Card(
        modifier  = Modifier.fillMaxWidth().clickable(interactionSource = interactionSource, indication = ripple(), onClick = onClick),
        shape     = RoundedCornerShape(topStart = topRadius, topEnd = topRadius, bottomStart = bottomRadius, bottomEnd = bottomRadius),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(text = verse.surahName, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                Text(text = "${verse.surahId}:${verse.verseNumber}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text       = verse.arabicText,
                fontFamily = arabicFontFamily,
                fontSize   = (arabicFontSize * 0.75f).sp,
                fontWeight = FontWeight.Normal,
                textAlign  = TextAlign.Right,
                lineHeight = (arabicFontSize * 1.5f).sp,
                color      = MaterialTheme.colorScheme.onSurface,
                maxLines   = 1,
                overflow   = TextOverflow.Ellipsis,
                modifier   = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text     = verse.translationText,
                style    = MaterialTheme.typography.bodySmall,
                color    = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
