package com.deenbase.app.features.quran.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.deenbase.app.features.quran.data.Verse
import com.deenbase.app.features.quran.viewmodel.SavedVersesViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SavedVersesScreen(
    onNavigateBack: () -> Unit,
    onVerseClick: (surahId: Int, verseNumber: Int, surahName: String) -> Unit,
    viewModel: SavedVersesViewModel = viewModel()
) {
    val favourites by viewModel.favourites.collectAsState()
    val bookmarks by viewModel.bookmarks.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }

    val gradientBrush = Brush.verticalGradient(
        colors = listOf(MaterialTheme.colorScheme.background, Color.Transparent)
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                modifier = Modifier.background(brush = gradientBrush),
                title = {
                    Text(
                        "Saved Verses",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
        ) {
            // ── Tabs ──────────────────────────────────────────────────────────
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Filled.Favorite, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("Favourites")
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Filled.Bookmark, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("Bookmarks")
                        }
                    }
                )
            }

            // ── Content ───────────────────────────────────────────────────────
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularWavyProgressIndicator()
                }
            } else {
                val currentList = if (selectedTab == 0) favourites else bookmarks
                val emptyLabel = if (selectedTab == 0)
                    "No favourites yet.\nTap ♡ while reading a verse."
                else
                    "No bookmarks yet.\nTap 🔖 while reading a verse."

                if (currentList.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = emptyLabel,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 48.dp)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(top = 12.dp, bottom = 100.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        itemsIndexed(
                            items = currentList,
                            key = { _, verse -> "${verse.surahId}:${verse.verseNumber}" }
                        ) { index, verse ->
                            SavedVerseItem(
                                verse = verse,
                                isFirst = index == 0,
                                isLast = index == currentList.size - 1,
                                onClick = {
                                    onVerseClick(verse.surahId, verse.verseNumber, verse.surahName)
                                },
                                onRemove = {
                                    if (selectedTab == 0) viewModel.removeFavourite(verse.surahId, verse.verseNumber)
                                    else viewModel.removeBookmark(verse.surahId, verse.verseNumber)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SavedVerseItem(
    verse: Verse,
    isFirst: Boolean,
    isLast: Boolean,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    val topRadius = if (isFirst) 20.dp else 4.dp
    val bottomRadius = if (isLast) 20.dp else 4.dp
    val interactionSource = remember { MutableInteractionSource() }

    ListItem(
        headlineContent = {
            Text(
                text = "${verse.surahName} • ${verse.verseNumber}",
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        supportingContent = {
            Text(
                text = verse.arabicText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )
        },
        trailingContent = {
            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Remove",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        },
        modifier = Modifier
            .clip(
                RoundedCornerShape(
                    topStart = topRadius, topEnd = topRadius,
                    bottomStart = bottomRadius, bottomEnd = bottomRadius
                )
            )
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(),
                onClick = onClick
            ),
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    )
}
