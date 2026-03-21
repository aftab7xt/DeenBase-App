package com.deenbase.app.features.hadith.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.deenbase.app.features.hadith.data.Hadith
import com.deenbase.app.features.hadith.viewmodel.HadithViewModel
import com.deenbase.app.ui.springOverscroll

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HadithSearchScreen(
    onNavigateBack: () -> Unit,
    onHadithClick: (Hadith) -> Unit,
    viewModel: HadithViewModel = viewModel()
) {
    val state         by viewModel.searchState.collectAsState()
    val searchQuery   by viewModel.searchQuery.collectAsState()
    val hasSearched   by viewModel.hasSearched.collectAsState()
    val searchHistory by viewModel.searchHistory.collectAsState()
    val listState     = rememberLazyListState()
    val focusRequester     = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    val gradientBrush = Brush.verticalGradient(
        colors = listOf(MaterialTheme.colorScheme.background, Color.Transparent)
    )

    // Auto-focus on open, not on return
    LaunchedEffect(hasSearched) {
        if (!hasSearched) focusRequester.requestFocus()
    }

    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val total = listState.layoutInfo.totalItemsCount
            lastVisible >= total - 4 && !state.isLoadingMore && state.hasMorePages
        }
    }
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) viewModel.loadMoreSearch()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    OutlinedTextField(
                        value         = searchQuery,
                        onValueChange = { viewModel.onSearchQueryChange(it) },
                        placeholder   = { Text("Search hadiths...") },
                        trailingIcon  = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
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
                            unfocusedBorderColor    = MaterialTheme.colorScheme.outlineVariant,
                            focusedBorderColor      = MaterialTheme.colorScheme.primary,
                            focusedContainerColor   = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.7f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.7f)
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
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
                state.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularWavyProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Searching...", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                // ── Pre-search: history or empty state ────────────────────────
                !hasSearched -> {
                    if (searchHistory.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(imageVector = Icons.Filled.Search, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("Search hadiths by topic or keyword", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Powered by Llama 3.3 on Groq", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f))
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
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp),
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

                // ── Error ─────────────────────────────────────────────────────
                state.error != null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Search failed. Try again.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
                    }
                }

                // ── No results ────────────────────────────────────────────────
                state.hadiths.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No results found", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // ── Results ───────────────────────────────────────────────────
                else -> {
                    LazyColumn(
                        state    = listState,
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        item { Spacer(modifier = Modifier.height(innerPadding.calculateTopPadding() + 4.dp)) }

                        if (state.totalCount > 0) {
                            item {
                                Text(text = "${state.totalCount} results", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 4.dp, bottom = 4.dp))
                            }
                        }

                        itemsIndexed(state.hadiths) { index, hadith ->
                            val isFirst      = index == 0
                            val isLast       = index == state.hadiths.lastIndex && !state.hasMorePages
                            val topRadius    = if (isFirst) 20.dp else 4.dp
                            val bottomRadius = if (isLast) 20.dp else 4.dp

                            Card(
                                onClick  = { onHadithClick(hadith) },
                                modifier = Modifier.fillMaxWidth(),
                                shape    = RoundedCornerShape(topStart = topRadius, topEnd = topRadius, bottomStart = bottomRadius, bottomEnd = bottomRadius),
                                colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                            ) {
                                Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = hadith.book?.bookName ?: hadith.bookSlug, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                                        Text(text = "#${hadith.hadithNumber}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    if (hadith.englishNarrator.isNotBlank()) {
                                        Text(text = hadith.englishNarrator, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                    Text(text = hadith.hadithEnglish, style = MaterialTheme.typography.bodyMedium, maxLines = 3, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }

                        if (state.isLoadingMore) {
                            item {
                                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                    CircularWavyProgressIndicator()
                                }
                            }
                        }

                        item { Spacer(modifier = Modifier.height(100.dp)) }
                    }
                }
            }

            // ── Top gradient ──────────────────────────────────────────────────
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
