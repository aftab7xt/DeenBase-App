package com.deenbase.app.features.settings.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.motionScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.deenbase.app.ui.springOverscroll
import com.deenbase.app.R
import com.deenbase.app.features.settings.viewmodel.QuranSettingsViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun QuranSettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: QuranSettingsViewModel = viewModel()
) {
    val translationLang     by viewModel.translationLang.collectAsState()
    val arabicFontStyle     by viewModel.arabicFontStyle.collectAsState()
    val arabicFontSize      by viewModel.arabicFontSize.collectAsState()
    val translationFontSize by viewModel.translationFontSize.collectAsState()
    val showTranslation     by viewModel.showTranslation.collectAsState()
    val isLoaded            by viewModel.isLoaded.collectAsState()

    val arabicFontFamily = remember(arabicFontStyle) {
        val fontRes = when (arabicFontStyle) {
            "indopak_nastaleeq" -> R.font.indopak_nastaleeq
            else -> R.font.hafs_uthmanic_regular
        }
        FontFamily(Font(fontRes))
    }
    val urduFontFamily = remember { FontFamily(Font(R.font.noto_nastaliq_urdu)) }

    val gradientBrush = Brush.verticalGradient(
        colors = listOf(MaterialTheme.colorScheme.background, Color.Transparent)
    )

    // ── Entrance animation ────────────────────────────────────────────────────
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    val contentAlpha by animateFloatAsState(targetValue = if (visible) 1f else 0f, animationSpec = tween(400, 60), label = "alpha")
    val contentScale by animateFloatAsState(targetValue = if (visible) 1f else 0.97f, animationSpec = tween(400, 60), label = "scale")

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                modifier = Modifier.background(brush = gradientBrush),
                title = { Text("Quran Settings", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent, scrolledContainerColor = Color.Transparent)
            )
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
                    .scale(contentScale)
            ) {
                Spacer(modifier = Modifier.height(innerPadding.calculateTopPadding()))

                // ── LIVE PREVIEW ──────────────────────────────────────────────
                Spacer(modifier = Modifier.height(8.dp))
                if (isLoaded) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape  = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            AnimatedContent(
                                targetState = Triple(arabicFontFamily, arabicFontSize, arabicFontStyle),
                                transitionSpec = { fadeIn() togetherWith fadeOut() },
                                label = "arabic_preview"
                            ) { (fontFamily, fontSize, _) ->
                                Text(
                                    text       = "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ",
                                    fontFamily = fontFamily,
                                    fontSize   = fontSize.sp,
                                    lineHeight = (fontSize * 2.2f).sp,
                                    textAlign  = TextAlign.Center,
                                    color      = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            if (showTranslation) {
                                Spacer(modifier = Modifier.height(16.dp))
                                AnimatedContent(
                                    targetState = Pair(translationLang, translationFontSize),
                                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                                    label = "translation_preview"
                                ) { (lang, fontSize) ->
                                    Text(
                                        text       = if (lang == "english") "In the name of Allah, the Entirely Merciful, the Especially Merciful" else "اللہ کے نام سے جو رحمان و رحیم ہے",
                                        fontSize   = fontSize.sp,
                                        lineHeight = (fontSize * if (lang == "urdu") 2.4f else 1.6f).sp,
                                        textAlign  = if (lang == "urdu") TextAlign.Right else TextAlign.Center,
                                        fontFamily = if (lang == "urdu") urduFontFamily else null,
                                        color      = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                SectionLabel("Text & Display")

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    SegmentedSettingsItem(
                        title     = "Arabic Font Style",
                        options   = listOf("hafs_uthmanic" to "Uthmanic", "indopak_nastaleeq" to "Indopak"),
                        selected  = arabicFontStyle,
                        index     = 0, totalItems = 3,
                        onSelect  = { viewModel.setArabicFontStyle(it) }
                    )
                    SliderSettingsItem(title = "Arabic Font Size", value = arabicFontSize, valueRange = 20f..48f, displayValue = "${arabicFontSize.toInt()}sp", index = 1, totalItems = 3, onValueChange = { viewModel.setArabicFontSize(it) })
                    SliderSettingsItem(title = "Translation Font Size", value = translationFontSize, valueRange = 12f..28f, displayValue = "${translationFontSize.toInt()}sp", index = 2, totalItems = 3, onValueChange = { viewModel.setTranslationFontSize(it) })
                }

                SectionLabel("Translation")

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    SegmentedSettingsItem(
                        title    = "Translation Language",
                        options  = listOf("english" to "English", "urdu" to "اردو"),
                        selected = translationLang,
                        index    = 0, totalItems = 2,
                        onSelect = { viewModel.setTranslationLang(it) }
                    )
                    SwitchSettingsItem(title = "Show Translation", subtitle = "Display translation below Arabic verse", checked = showTranslation, index = 1, totalItems = 2, onCheckedChange = { viewModel.setShowTranslation(it) })
                }

                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text = text, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(start = 8.dp, bottom = 8.dp, top = 24.dp))
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SegmentedSettingsItem(title: String, options: List<Pair<String, String>>, selected: String, index: Int, totalItems: Int, onSelect: (String) -> Unit) {
    val topRadius    by animateDpAsState(targetValue = if (index == 0 || totalItems == 1) 20.dp else 4.dp, animationSpec = motionScheme.fastSpatialSpec(), label = "top")
    val bottomRadius by animateDpAsState(targetValue = if (index == totalItems - 1 || totalItems == 1) 20.dp else 4.dp, animationSpec = motionScheme.fastSpatialSpec(), label = "bottom")

    Surface(shape = RoundedCornerShape(topStart = topRadius, topEnd = topRadius, bottomStart = bottomRadius, bottomEnd = bottomRadius), color = MaterialTheme.colorScheme.surfaceContainerLow, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Text(title, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(12.dp))
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                options.forEachIndexed { i, (key, label) ->
                    SegmentedButton(selected = selected == key, onClick = { onSelect(key) }, shape = SegmentedButtonDefaults.itemShape(index = i, count = options.size), label = { Text(label, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SliderSettingsItem(title: String, value: Float, valueRange: ClosedFloatingPointRange<Float>, displayValue: String, index: Int, totalItems: Int, onValueChange: (Float) -> Unit) {
    val topRadius    by animateDpAsState(targetValue = if (index == 0 || totalItems == 1) 20.dp else 4.dp, animationSpec = motionScheme.fastSpatialSpec(), label = "top")
    val bottomRadius by animateDpAsState(targetValue = if (index == totalItems - 1 || totalItems == 1) 20.dp else 4.dp, animationSpec = motionScheme.fastSpatialSpec(), label = "bottom")

    Surface(shape = RoundedCornerShape(topStart = topRadius, topEnd = topRadius, bottomStart = bottomRadius, bottomEnd = bottomRadius), color = MaterialTheme.colorScheme.surfaceContainerLow, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(title, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge)
                Text(displayValue, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            }
            Slider(value = value, onValueChange = onValueChange, valueRange = valueRange, modifier = Modifier.fillMaxWidth())
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SwitchSettingsItem(title: String, subtitle: String, checked: Boolean, index: Int, totalItems: Int, onCheckedChange: (Boolean) -> Unit) {
    val topRadius    by animateDpAsState(targetValue = if (index == 0 || totalItems == 1) 20.dp else 4.dp, animationSpec = motionScheme.fastSpatialSpec(), label = "top")
    val bottomRadius by animateDpAsState(targetValue = if (index == totalItems - 1 || totalItems == 1) 20.dp else 4.dp, animationSpec = motionScheme.fastSpatialSpec(), label = "bottom")

    ListItem(
        headlineContent   = { Text(title, fontWeight = FontWeight.SemiBold) },
        supportingContent = { Text(subtitle) },
        trailingContent   = {
            Switch(checked = checked, onCheckedChange = onCheckedChange, colors = SwitchDefaults.colors(uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest, uncheckedThumbColor = MaterialTheme.colorScheme.onSurface, uncheckedBorderColor = MaterialTheme.colorScheme.outline))
        },
        modifier = Modifier.clip(RoundedCornerShape(topStart = topRadius, topEnd = topRadius, bottomStart = bottomRadius, bottomEnd = bottomRadius)),
        colors   = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    )
}
