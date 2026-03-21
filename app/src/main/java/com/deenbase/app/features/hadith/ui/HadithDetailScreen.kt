package com.deenbase.app.features.hadith.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Picture
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.core.content.FileProvider
import kotlinx.coroutines.launch
import com.deenbase.app.R
import com.deenbase.app.features.hadith.data.Hadith
import com.deenbase.app.ui.springOverscroll
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HadithDetailScreen(
    hadith: Hadith,
    onNavigateBack: () -> Unit
) {
    val arabicFont = remember { FontFamily(Font(R.font.indopak_nastaleeq)) }
    val hasGrade   = !hadith.status.isNullOrBlank()
    val context    = LocalContext.current

    var arabicExpanded     by remember { mutableStateOf(false) }
    var fabExpanded        by remember { mutableStateOf(false) }
    var showImageSheet     by remember { mutableStateOf(false) }

    // FAB morphs from rounded square (closed) to circle (open)
    val fabCorner by animateDpAsState(
        targetValue   = if (fabExpanded) 50.dp else 16.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label         = "fabCorner"
    )

    val gradientBrush = Brush.verticalGradient(
        colors = listOf(MaterialTheme.colorScheme.background, Color.Transparent)
    )

    // ── Entrance animation ────────────────────────────────────────────────────
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    val contentAlpha by animateFloatAsState(targetValue = if (visible) 1f else 0f, animationSpec = tween(400, 60), label = "alpha")
    val contentScale by animateFloatAsState(targetValue = if (visible) 1f else 0.97f, animationSpec = tween(400, 60), label = "scale")

    // Helper: build plain-text share string
    fun buildShareText(): String = buildString {
        if (hadith.englishNarrator.isNotBlank()) appendLine(hadith.englishNarrator)
        appendLine(hadith.hadithEnglish)
        appendLine()
        append(hadith.book?.bookName ?: hadith.bookSlug)
        if (hadith.hadithNumber.isNotBlank()) append(" #${hadith.hadithNumber}")
    }

    if (showImageSheet) {
        HadithShareImageBottomSheet(
            hadith    = hadith,
            context   = context,
            onDismiss = { showImageSheet = false }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text       = hadith.book?.bookName ?: hadith.bookSlug,
                            style      = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text  = "Hadith #${hadith.hadithNumber}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
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
        floatingActionButton = {
            // ── FAB menu ──────────────────────────────────────────────────────
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Share Image pill — topmost, last to appear / first to leave
                AnimatedVisibility(
                    visible = fabExpanded,
                    enter   = slideInVertically(tween(220, delayMillis = 80)) { 60 } + fadeIn(tween(220, delayMillis = 80)),
                    exit    = slideOutVertically(tween(160)) { 60 } + fadeOut(tween(160))
                ) {
                    FabMenuItem(
                        label   = "Share Image",
                        icon    = Icons.Filled.Image,
                        onClick = { fabExpanded = false; showImageSheet = true }
                    )
                }

                // Share Text pill
                AnimatedVisibility(
                    visible = fabExpanded,
                    enter   = slideInVertically(tween(220, delayMillis = 40)) { 60 } + fadeIn(tween(220, delayMillis = 40)),
                    exit    = slideOutVertically(tween(160, delayMillis = 30)) { 60 } + fadeOut(tween(160, delayMillis = 30))
                ) {
                    FabMenuItem(
                        label   = "Share Text",
                        icon    = Icons.Filled.TextFields,
                        onClick = {
                            fabExpanded = false
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, buildShareText())
                            }
                            context.startActivity(Intent.createChooser(intent, "Share Hadith"))
                        }
                    )
                }

                // Copy pill — closest to FAB, first to appear / last to leave
                AnimatedVisibility(
                    visible = fabExpanded,
                    enter   = slideInVertically(tween(220)) { 60 } + fadeIn(tween(220)),
                    exit    = slideOutVertically(tween(160, delayMillis = 60)) { 60 } + fadeOut(tween(160, delayMillis = 60))
                ) {
                    FabMenuItem(
                        label   = "Copy",
                        icon    = Icons.Filled.ContentCopy,
                        onClick = {
                            fabExpanded = false
                            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            cm.setPrimaryClip(ClipData.newPlainText("Hadith", buildShareText()))
                            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                // Main FAB — morphs from rounded square to circle
                FloatingActionButton(
                    onClick        = { fabExpanded = !fabExpanded },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor   = MaterialTheme.colorScheme.onPrimaryContainer,
                    shape          = RoundedCornerShape(fabCorner)
                ) {
                    Icon(
                        imageVector        = if (fabExpanded) Icons.Filled.Close else Icons.Filled.Share,
                        contentDescription = if (fabExpanded) "Close" else "Share"
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().springOverscroll()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
                    .alpha(contentAlpha)
                    .scale(contentScale),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Spacer(modifier = Modifier.height(innerPadding.calculateTopPadding() + 4.dp))

                // ── Arabic ────────────────────────────────────────────────────
                if (hadith.hadithArabic.isNotBlank()) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        SectionLabel("Arabic")
                        Card(
                            onClick  = { arabicExpanded = !arabicExpanded },
                            modifier = Modifier.fillMaxWidth(),
                            shape    = RoundedCornerShape(20.dp),
                            colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth().animateContentSize(tween(300))) {
                                Text(
                                    text       = hadith.hadithArabic,
                                    modifier   = Modifier.fillMaxWidth().padding(20.dp),
                                    textAlign  = TextAlign.End,
                                    lineHeight = 48.sp,
                                    fontSize   = 24.sp,
                                    fontFamily = arabicFont,
                                    color      = MaterialTheme.colorScheme.onSurface,
                                    maxLines   = if (arabicExpanded) Int.MAX_VALUE else 3,
                                    overflow   = if (arabicExpanded) androidx.compose.ui.text.style.TextOverflow.Clip
                                                 else androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                                if (!arabicExpanded) {
                                    Text(
                                        text     = "tap to expand",
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                        textAlign = TextAlign.Center,
                                        style    = MaterialTheme.typography.labelSmall,
                                        color    = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                    )
                                }
                            }
                        }
                    }
                }

                // ── English ───────────────────────────────────────────────────
                if (hadith.hadithEnglish.isNotBlank()) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        SectionLabel("English")
                        val englishCardShape = if (hasGrade)
                            RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 4.dp, bottomEnd = 4.dp)
                        else
                            RoundedCornerShape(20.dp)

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape    = englishCardShape,
                            colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (hadith.englishNarrator.isNotBlank()) {
                                    Text(
                                        text      = hadith.englishNarrator,
                                        style     = MaterialTheme.typography.bodySmall,
                                        color     = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontStyle = FontStyle.Italic
                                    )
                                }
                                Text(
                                    text       = hadith.hadithEnglish,
                                    style      = MaterialTheme.typography.bodyLarge,
                                    lineHeight = 26.sp
                                )
                            }
                        }

                        if (hasGrade) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape    = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 20.dp, bottomEnd = 20.dp),
                                colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text  = "Grade",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text       = hadith.status!!,
                                        style      = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = when (hadith.status.lowercase()) {
                                            "sahih" -> MaterialTheme.colorScheme.primary
                                            "hasan" -> MaterialTheme.colorScheme.tertiary
                                            else    -> MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // ── Urdu ──────────────────────────────────────────────────────
                if (hadith.hadithUrdu.isNotBlank()) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        SectionLabel("اردو")
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape    = RoundedCornerShape(20.dp),
                            colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (hadith.urduNarrator.isNotBlank()) {
                                    Text(
                                        text      = hadith.urduNarrator,
                                        modifier  = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.End,
                                        style     = MaterialTheme.typography.bodySmall,
                                        color     = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text       = hadith.hadithUrdu,
                                    modifier   = Modifier.fillMaxWidth(),
                                    textAlign  = TextAlign.End,
                                    lineHeight = 28.sp,
                                    fontSize   = 16.sp
                                )
                            }
                        }
                    }
                }

                // ── Chapter ───────────────────────────────────────────────────
                hadith.chapterEnglish?.let { chapter ->
                    if (chapter.isNotBlank()) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            SectionLabel("Chapter")
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape    = RoundedCornerShape(20.dp),
                                colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                            ) {
                                Text(
                                    text     = chapter,
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    style    = MaterialTheme.typography.bodyMedium,
                                    color    = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(100.dp))
            }

            // Scrim — sits on top of scroll content, under top gradient and FAB
            AnimatedVisibility(
                visible = fabExpanded,
                enter   = fadeIn(tween(200)),
                exit    = fadeOut(tween(200)),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.35f))
                )
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

// ── FAB pill item ─────────────────────────────────────────────────────────────

@Composable
private fun FabMenuItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    FilledTonalButton(
        onClick        = onClick,
        shape          = RoundedCornerShape(50.dp),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
        colors         = ButtonDefaults.filledTonalButtonColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor   = MaterialTheme.colorScheme.onSecondaryContainer
        )
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = null,
            modifier           = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
    }
}

// ── Hadith image share bottom sheet ──────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HadithShareImageBottomSheet(
    hadith: Hadith,
    context: Context,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val picture    = remember { Picture() }
    val scope      = rememberCoroutineScope()

    var captureWidth  by remember { mutableIntStateOf(0) }
    var captureHeight by remember { mutableIntStateOf(0) }

    val surfaceColor = MaterialTheme.colorScheme.surfaceContainer
    val primaryColor = MaterialTheme.colorScheme.primary

    val referenceText = buildString {
        append(hadith.book?.bookName ?: hadith.bookSlug)
        if (hadith.hadithNumber.isNotBlank()) append(" #${hadith.hadithNumber}")
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        dragHandle       = { BottomSheetDefaults.DragHandle() },
        containerColor   = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Image Preview",
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier   = Modifier.padding(bottom = 16.dp)
            )

            // ── Scrollable preview — bounded so button stays visible ──────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .onGloballyPositioned {
                            captureWidth  = it.size.width
                            captureHeight = it.size.height
                        }
                        .drawWithCache {
                            val w = this.size.width.toInt()
                            val h = this.size.height.toInt()
                            onDrawWithContent {
                                val pictureCanvas = androidx.compose.ui.graphics.Canvas(
                                    picture.beginRecording(w, h)
                                )
                                val prev = drawContext.canvas
                                drawContext.canvas = pictureCanvas
                                drawContent()
                                drawContext.canvas = prev
                                picture.endRecording()
                                drawIntoCanvas { it.nativeCanvas.drawPicture(picture) }
                            }
                        }
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    // Top card — narrator + hadith text
                    Card(
                        shape     = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 4.dp, bottomEnd = 4.dp),
                        colors    = CardDefaults.cardColors(containerColor = surfaceColor),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        modifier  = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (hadith.englishNarrator.isNotBlank()) {
                                Text(
                                    text      = hadith.englishNarrator,
                                    fontSize  = 11.sp,
                                    lineHeight = 16.sp,
                                    color     = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontStyle = FontStyle.Italic
                                )
                            }
                            Text(
                                text       = hadith.hadithEnglish,
                                fontSize   = 13.sp,
                                lineHeight = 20.sp,
                                color      = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Bottom card — reference + logo
                    Card(
                        shape     = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 24.dp, bottomEnd = 24.dp),
                        colors    = CardDefaults.cardColors(containerColor = surfaceColor),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        modifier  = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            Text(
                                text       = referenceText,
                                style      = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color      = primaryColor
                            )
                            Image(
                                painter            = painterResource(id = R.drawable.ic_db_logo),
                                contentDescription = "DeenBase",
                                modifier           = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }

            // ── Share button — always pinned below the preview ────────────────
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick  = {
                    scope.launch {
                        shareHadithAsImage(context, picture, captureWidth, captureHeight)
                        sheetState.hide()
                        onDismiss()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(56.dp),
                shape    = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Filled.Image, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                Text("Share Image", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

private fun shareHadithAsImage(context: Context, picture: Picture, width: Int, height: Int) {
    if (width <= 0 || height <= 0) {
        Toast.makeText(context, "Please wait a moment for the image to load.", Toast.LENGTH_SHORT).show()
        return
    }
    try {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        canvas.drawColor(android.graphics.Color.WHITE)
        canvas.drawPicture(picture)

        val file = File(context.cacheDir, "shared_hadith.png")
        FileOutputStream(file).use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Hadith"))
    } catch (e: Exception) {
        Toast.makeText(context, "Error sharing image: ${e.message}", Toast.LENGTH_LONG).show()
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text     = text,
        style    = MaterialTheme.typography.labelLarge,
        color    = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
    )
}
