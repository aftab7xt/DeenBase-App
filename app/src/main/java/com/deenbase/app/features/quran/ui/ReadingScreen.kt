package com.deenbase.app.features.quran.ui

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Picture
import android.widget.Toast
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.deenbase.app.R
import com.deenbase.app.features.quran.data.BISMILLAH
import com.deenbase.app.features.quran.data.SURAH_NO_BISMILLAH_HEADER
import com.deenbase.app.features.quran.data.Verse
import com.deenbase.app.features.quran.viewmodel.ReadingViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

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

    val context = LocalContext.current

    val favouriteVerses by viewModel.favouriteVerses.collectAsState()
    val bookmarkedVerses by viewModel.bookmarkedVerses.collectAsState()

    val urduFontFamily = remember { FontFamily(Font(R.font.noto_nastaliq_urdu)) }

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

    // State for the Image Share Dialog
    var verseToShare by remember { mutableStateOf<Verse?>(null) }

    LaunchedEffect(surahId) { viewModel.loadSurah(surahId) }

    val pagerState = rememberPagerState(
        initialPage = (startVerse - 1).coerceAtLeast(0),
        pageCount = { verses.size.coerceAtLeast(1) }
    )
    val scope = rememberCoroutineScope()

    LaunchedEffect(pagerState.currentPage, verses) {
        verses.getOrNull(pagerState.currentPage)?.let { viewModel.updateCurrentJuz(it.juz) }
    }

    var lastTrackedPage by remember { mutableIntStateOf(-1) }
    LaunchedEffect(pagerState.currentPage, verses) {
        if (trackGoal && verses.isNotEmpty() && pagerState.currentPage != lastTrackedPage) {
            val page = pagerState.currentPage
            if (lastTrackedPage >= 0 && page > lastTrackedPage) {
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

    // Render the Image Share Bottom Sheet if triggered
    verseToShare?.let { verse ->
        ShareImageBottomSheet(
            verse = verse,
            arabicFontFamily = arabicFontFamily,
            urduFontFamily = if (translationLang == "urdu") urduFontFamily else null,
            context = context,
            onDismiss = { verseToShare = null }
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

                val verseKey = "${verse.surahId}:${verse.verseNumber}"
                val isFavourite = verseKey in favouriteVerses
                val isBookmarked = verseKey in bookmarkedVerses

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                        .verticalScroll(scrollState),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(innerPadding.calculateTopPadding() + 8.dp))

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

                    // ── VERSE CARD ────────────────────────────────────────────
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
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
                                Box(
                                    modifier = Modifier.size(48.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${verse.verseNumber}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { /* TODO: audio */ }) {
                                        Icon(
                                            imageVector = Icons.Filled.PlayArrow,
                                            contentDescription = "Play",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(26.dp)
                                        )
                                    }
                                    IconButton(onClick = {
                                        viewModel.toggleFavourite(verse.surahId, verse.verseNumber)
                                    }) {
                                        Icon(
                                            imageVector = if (isFavourite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                            contentDescription = "Favourite",
                                            tint = if (isFavourite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    IconButton(onClick = {
                                        viewModel.toggleBookmark(verse.surahId, verse.verseNumber)
                                    }) {
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
                                IconButton(onClick = { verseToShare = verse }) {
                                    Icon(
                                        imageVector = Icons.Filled.Share,
                                        contentDescription = "Share Image",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareImageBottomSheet(
    verse: Verse,
    arabicFontFamily: FontFamily,
    urduFontFamily: FontFamily?,
    context: Context,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val picture = remember { Picture() }
    val scope = rememberCoroutineScope()
    
    // Explicit sizing bypasses the CanvasDrawScope bug
    var captureWidth by remember { mutableIntStateOf(0) }
    var captureHeight by remember { mutableIntStateOf(0) }
    
    val surfaceColor = MaterialTheme.colorScheme.surfaceContainer
    val primaryColor = MaterialTheme.colorScheme.primary

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Image Preview",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // ── THE VIEW THAT WILL BE CAPTURED AS AN IMAGE ────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .onGloballyPositioned {
                        captureWidth = it.size.width
                        captureHeight = it.size.height
                    }
                    .drawWithCache {
                        val width = this.size.width.toInt()
                        val height = this.size.height.toInt()
                        onDrawWithContent {
                            val pictureCanvas = androidx.compose.ui.graphics.Canvas(
                                picture.beginRecording(width, height)
                            )
                            
                            // Swap canvas safely to bypass CanvasDrawScope exceptions
                            val prevCanvas = drawContext.canvas
                            drawContext.canvas = pictureCanvas
                            drawContent()
                            drawContext.canvas = prevCanvas
                            
                            picture.endRecording()

                            drawIntoCanvas { canvas ->
                                canvas.nativeCanvas.drawPicture(picture)
                            }
                        }
                    }
                    // Apply background + padding so the captured image has a nice solid frame
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Top Card: Arabic and Translation
                Card(
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 4.dp, bottomEnd = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = surfaceColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 24.dp)
                    ) {
                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                            Text(
                                text = verse.arabicText,
                                fontFamily = arabicFontFamily,
                                fontSize = 24.sp,
                                textAlign = TextAlign.Right,
                                lineHeight = 48.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        Text(
                            text = verse.translationText,
                            fontSize = 16.sp,
                            lineHeight = 24.sp,
                            textAlign = if (urduFontFamily != null) TextAlign.Right else TextAlign.Justify,
                            fontFamily = urduFontFamily,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Bottom Card: Reference
                Card(
                    shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 24.dp, bottomEnd = 24.dp),
                    colors = CardDefaults.cardColors(containerColor = surfaceColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${verse.surahName} - ${verse.verseNumber}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor
                        )
                        
                        Text(
                            text = "DeenBase App",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    scope.launch {
                        sharePictureAsImage(context, picture, captureWidth, captureHeight)
                        sheetState.hide()
                        onDismiss()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Filled.Image, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                Text("Share Image", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

private fun sharePictureAsImage(context: Context, picture: Picture, width: Int, height: Int) {
    if (width <= 0 || height <= 0) {
        Toast.makeText(context, "Please wait a moment for the image to load.", Toast.LENGTH_SHORT).show()
        return
    }

    try {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        canvas.drawColor(android.graphics.Color.WHITE) // Safety fallback so transparent areas don't turn black on WhatsApp
        canvas.drawPicture(picture)

        val file = File(context.cacheDir, "shared_verse.png")
        val outputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        outputStream.flush()
        outputStream.close()

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        val chooser = Intent.createChooser(intent, "Share Verse")
        context.startActivity(chooser)
        
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Error sharing image: ${e.message}", Toast.LENGTH_LONG).show()
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ReaderBottomNavigation(
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onDoneClick: () -> Unit,
    isPreviousEnabled: Boolean,
    isLastVerse: Boolean
) {
    val haptic = LocalHapticFeedback.current
    val hapticsEnabled = com.deenbase.app.ui.LocalHapticsEnabled.current

    val prevSource = remember { MutableInteractionSource() }
    val doneSource = remember { MutableInteractionSource() }
    val nextSource = remember { MutableInteractionSource() }

    val prevPressed by prevSource.collectIsPressedAsState()
    val donePressed by doneSource.collectIsPressedAsState()
    val nextPressed by nextSource.collectIsPressedAsState()

    val prevWeight by animateFloatAsState(
        targetValue = when {
            prevPressed -> 1.4f
            donePressed || nextPressed -> 0.8f
            else -> 1f
        },
        animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
        label = "prevWeight"
    )
    val doneWeight by animateFloatAsState(
        targetValue = when {
            donePressed -> 2.4f
            prevPressed || nextPressed -> 1.6f
            else -> 2f
        },
        animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
        label = "doneWeight"
    )
    val nextWeight by animateFloatAsState(
        targetValue = when {
            nextPressed -> 1.4f
            donePressed || prevPressed -> 0.8f
            else -> 1f
        },
        animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
        label = "nextWeight"
    )

    val prevCorner by animateDpAsState(
        targetValue = if (prevPressed) 14.dp else 50.dp,
        animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
        label = "prevCorner"
    )
    val doneCorner by animateDpAsState(
        targetValue = if (donePressed) 6.dp else 16.dp,
        animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
        label = "doneCorner"
    )
    val nextCorner by animateDpAsState(
        targetValue = if (nextPressed) 14.dp else 50.dp,
        animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
        label = "nextCorner"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color.Transparent, MaterialTheme.colorScheme.background)
                )
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
            FilledTonalButton(
                onClick = {
                    onPreviousClick()
                    if (hapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
                },
                enabled = isPreviousEnabled,
                interactionSource = prevSource,
                shape = RoundedCornerShape(prevCorner),
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.38f),
                    disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                ),
                modifier = Modifier
                    .height(52.dp)
                    .weight(prevWeight)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous")
            }

            FilledTonalButton(
                onClick = {
                    onDoneClick()
                    if (hapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
                },
                interactionSource = doneSource,
                shape = RoundedCornerShape(doneCorner),
                contentPadding = PaddingValues(horizontal = 16.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier
                    .height(52.dp)
                    .weight(doneWeight)
            ) {
                Text(
                    text = "Done Reading",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
            }

            FilledTonalButton(
                onClick = {
                    onNextClick()
                    if (hapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
                },
                interactionSource = nextSource,
                shape = RoundedCornerShape(nextCorner),
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier
                    .height(52.dp)
                    .weight(nextWeight)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next")
            }
        }
    }
}

