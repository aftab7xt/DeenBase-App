package com.deenbase.app.features.quran.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.deenbase.app.R
import com.deenbase.app.features.quran.data.BISMILLAH
import com.deenbase.app.features.quran.data.SURAH_NO_BISMILLAH_HEADER
import com.deenbase.app.features.quran.viewmodel.ReadingViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalFoundationApi::class)
@Composable
fun ReadingScreen(
    surahId: Int,
    surahName: String = "",
    startVerse: Int = 1,
    trackGoal: Boolean = false,
    onNavigateBack: () -> Unit,
    onNextSurah: (Int) -> Unit = {},
    viewModel: ReadingViewModel = viewModel()
) {
    val verses by viewModel.verses.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val currentSurahId by viewModel.currentSurahId.collectAsState()
    val loadedSurahName by viewModel.surahName.collectAsState()
    val currentJuz by viewModel.currentJuz.collectAsState()
    val arabicFontSize by viewModel.arabicFontSize.collectAsState()
    val translationFontSize by viewModel.translationFontSize.collectAsState()
    val showTranslation by viewModel.showTranslation.collectAsState()
    val arabicFontStyle by viewModel.arabicFontStyle.collectAsState()
    val translationLang by viewModel.translationLang.collectAsState()

    val urduFontFamily = remember { FontFamily(Font(R.font.noto_nastaliq_urdu)) }

    // Use passed surahName immediately — fall back to loaded once available
    val displayName = if (surahName.isNotEmpty()) surahName
                      else if (loadedSurahName.isNotEmpty()) loadedSurahName
                      else "Surah $surahId"

    val arabicFontFamily = remember(arabicFontStyle) {
        val fontRes = when (arabicFontStyle) {
            "indopak_nastaleeq" -> R.font.indopak_nastaleeq
            else -> R.font.hafs_uthmanic_regular
        }
        FontFamily(Font(fontRes))
    }
    val arabicTextColor = MaterialTheme.colorScheme.onSurface

    LaunchedEffect(surahId) { viewModel.loadSurah(surahId) }

    val pagerState = rememberPagerState(
        initialPage = (startVerse - 1).coerceAtLeast(0),
        pageCount = { verses.size.coerceAtLeast(1) }
    )
    val scope = rememberCoroutineScope()

    LaunchedEffect(pagerState.currentPage, verses) {
        verses.getOrNull(pagerState.currentPage)?.let { viewModel.updateCurrentJuz(it.juz) }
    }

    // Save progress and increment count on every page change (swipe OR button)
    var lastTrackedPage by remember { mutableIntStateOf(-1) }
    LaunchedEffect(pagerState.currentPage, verses) {
        if (trackGoal && verses.isNotEmpty() && pagerState.currentPage != lastTrackedPage) {
            val page = pagerState.currentPage
            if (lastTrackedPage >= 0 && page > lastTrackedPage) {
                // Moving forward — save new position and count the verse
                verses.getOrNull(page)?.let {
                    viewModel.saveProgress(currentSurahId, it.verseNumber)
                    viewModel.incrementTodayCount()
                }
            }
            lastTrackedPage = page
        }
    }

    LaunchedEffect(verses) {
        if (verses.isNotEmpty() && startVerse > 1) {
            val targetPage = (startVerse - 1).coerceIn(0, verses.size - 1)
            pagerState.scrollToPage(targetPage)
            lastTrackedPage = targetPage
        }
    }

    var showNextSurahDialog by remember { mutableStateOf(false) }
    if (showNextSurahDialog) {
        AlertDialog(
            onDismissRequest = { showNextSurahDialog = false },
            title = { Text("Surah Complete") },
            text = { Text("You've finished $displayName. Continue to the next surah?") },
            confirmButton = {
                Button(onClick = {
                    showNextSurahDialog = false
                    val nextId = currentSurahId + 1
                    if (nextId <= 114) { viewModel.saveProgress(nextId, 1); onNextSurah(nextId) }
                }) { Text("Next Surah") }
            },
            dismissButton = {
                TextButton(onClick = { showNextSurahDialog = false; onNavigateBack() }) { Text("Go Back") }
            }
        )
    }

    val gradientBrush = Brush.verticalGradient(
        colors = listOf(MaterialTheme.colorScheme.background, Color.Transparent)
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                modifier = Modifier.background(brush = gradientBrush),
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = displayName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (currentJuz > 0) {
                            Text(
                                text = "Juz $currentJuz",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                )
            )
        },
        bottomBar = {
            if (verses.isNotEmpty()) {
                ReaderBottomNavigation(
                    onPreviousClick = {
                        if (pagerState.currentPage > 0) scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage - 1, animationSpec = tween(300))
                        }
                    },
                    onNextClick = {
                        if (pagerState.currentPage < verses.size - 1) {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1, animationSpec = tween(300))
                            }
                        } else {
                            if (trackGoal) showNextSurahDialog = true
                        }
                    },
                    onDoneClick = { onNavigateBack() },
                    isPreviousEnabled = pagerState.currentPage > 0,
                    isLastVerse = pagerState.currentPage == verses.size - 1
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (isLoading || verses.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularWavyProgressIndicator()
            }
        } else {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                beyondViewportPageCount = 1
            ) { page ->
                val verse = verses[page]
                val scrollState = rememberScrollState()
                LaunchedEffect(page) { scrollState.scrollTo(0) }

                // Temporary state — will be hooked up later
                var isFavourite by remember { mutableStateOf(false) }
                var isBookmarked by remember { mutableStateOf(false) }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                        .verticalScroll(scrollState),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(innerPadding.calculateTopPadding() + 8.dp))

                    // Bismillah header on verse 1
                    if (verse.verseNumber == 1 && surahId !in SURAH_NO_BISMILLAH_HEADER) {
                        Text(
                            text = BISMILLAH,
                            fontFamily = arabicFontFamily,
                            fontSize = (arabicFontSize * 0.85f).sp,
                            textAlign = TextAlign.Center,
                            lineHeight = (arabicFontSize * 1.8f).sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), thickness = 0.5.dp)
                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    // ── VERSE CARD ────────────────────────────────────────
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        // Top section — header with play, surah info, actions
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 4.dp, bottomEnd = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Verse number on the left
                                Box(
                                    modifier = Modifier
                                        .size(48.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${verse.verseNumber}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                // Play + Favourite + Bookmark on the right
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { /* TODO: audio */ }) {
                                        Icon(
                                            imageVector = Icons.Filled.PlayArrow,
                                            contentDescription = "Play",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(26.dp)
                                        )
                                    }
                                    IconButton(onClick = { isFavourite = !isFavourite }) {
                                        Icon(
                                            imageVector = if (isFavourite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                            contentDescription = "Favourite",
                                            tint = if (isFavourite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    IconButton(onClick = { isBookmarked = !isBookmarked }) {
                                        Icon(
                                            imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                                            contentDescription = "Bookmark",
                                            tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Middle section — Arabic text
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(4.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Text(
                                text = verse.arabicText,
                                fontFamily = arabicFontFamily,
                                fontSize = arabicFontSize.sp,
                                fontWeight = FontWeight.Normal,
                                textAlign = TextAlign.Right,
                                lineHeight = (arabicFontSize * 2.0f).sp,
                                color = arabicTextColor,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 24.dp)
                            )
                        }

                        // Bottom section — share
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 24.dp, bottomEnd = 24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.End
                            ) {
                                IconButton(onClick = { /* TODO: share */ }) {
                                    Icon(
                                        imageVector = Icons.Filled.Share,
                                        contentDescription = "Share",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Translation
                    if (showTranslation) {
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = verse.translationText,
                            fontSize = translationFontSize.sp,
                            lineHeight = (translationFontSize * if (translationLang == "urdu") 2.4f else 1.7f).sp,
                            textAlign = if (translationLang == "urdu") TextAlign.Right else TextAlign.Justify,
                            fontFamily = if (translationLang == "urdu") urduFontFamily else null,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(innerPadding.calculateBottomPadding() + 100.dp))
                }
            }
        }
    }
}

@Composable
fun SpringIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color,
    contentColor: Color,
    size: Int = 48,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.82f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "scale"
    )
    FilledIconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.size(size.dp).scale(scale),
        interactionSource = interactionSource,
        colors = IconButtonDefaults.filledIconButtonColors(containerColor = containerColor, contentColor = contentColor)
    ) { content() }
}

@Composable
fun ReaderBottomNavigation(
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onDoneClick: () -> Unit,
    isPreviousEnabled: Boolean,
    isLastVerse: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .background(
                Brush.verticalGradient(colors = listOf(Color.Transparent, MaterialTheme.colorScheme.background))
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ← Pill button
            MorphPillButton(
                onClick = onPreviousClick,
                enabled = isPreviousEnabled,
                modifier = Modifier.weight(1f),
                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Previous",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            // Done Reading — rounded rectangle middle
            MorphRectButton(
                onClick = onDoneClick,
                modifier = Modifier.weight(2f),
                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                Text(
                    text = "Done Reading",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // → Pill button
            MorphPillButton(
                onClick = onNextClick,
                modifier = Modifier.weight(1f),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Next",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MorphPillButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color,
    contentColor: Color,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPhysicallyPressed by interactionSource.collectIsPressedAsState()
    var forceSquish by remember { mutableStateOf(false) }
    val isSquished = isPhysicallyPressed || forceSquish
    val scope = rememberCoroutineScope()

    val corner by animateDpAsState(
        targetValue = if (isSquished) 14.dp else 50.dp,
        animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
        label = "pill_corner"
    )
    val scale by animateFloatAsState(
        targetValue = if (isSquished) 0.95f else 1.0f,
        animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
        label = "pill_scale"
    )

    FilledTonalButton(
        onClick = {
            onClick()
            scope.launch { forceSquish = true; delay(90); forceSquish = false }
        },
        enabled = enabled,
        interactionSource = interactionSource,
        shape = RoundedCornerShape(corner),
        contentPadding = PaddingValues(0.dp),
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor.copy(alpha = 0.38f),
            disabledContentColor = contentColor.copy(alpha = 0.38f)
        ),
        modifier = modifier.height(52.dp).scale(scale)
    ) {
        content()
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MorphRectButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color,
    contentColor: Color,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPhysicallyPressed by interactionSource.collectIsPressedAsState()
    var forceSquish by remember { mutableStateOf(false) }
    val isSquished = isPhysicallyPressed || forceSquish
    val scope = rememberCoroutineScope()

    val corner by animateDpAsState(
        targetValue = if (isSquished) 6.dp else 16.dp,
        animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
        label = "rect_corner"
    )
    val scale by animateFloatAsState(
        targetValue = if (isSquished) 0.95f else 1.0f,
        animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
        label = "rect_scale"
    )

    FilledTonalButton(
        onClick = {
            onClick()
            scope.launch { forceSquish = true; delay(90); forceSquish = false }
        },
        interactionSource = interactionSource,
        shape = RoundedCornerShape(corner),
        contentPadding = PaddingValues(0.dp),
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        modifier = modifier.height(52.dp).scale(scale)
    ) {
        content()
    }
}
