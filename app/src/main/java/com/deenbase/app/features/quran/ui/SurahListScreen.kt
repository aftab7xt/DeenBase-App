package com.deenbase.app.features.quran.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.deenbase.app.R
import com.deenbase.app.features.quran.data.SurahInfo
import com.deenbase.app.features.quran.viewmodel.QuranViewModel
import com.deenbase.app.ui.springOverscroll

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SurahListScreen(
    onNavigateBack: () -> Unit,
    onSurahClick: (Int, String) -> Unit,
    viewModel: QuranViewModel = viewModel()
) {
    val surahs         by viewModel.surahs.collectAsState()
    val surahNamesFont  = remember { FontFamily(Font(R.font.surah_names)) }

    val gradientBrush = Brush.verticalGradient(
        colors = listOf(MaterialTheme.colorScheme.background, Color.Transparent)
    )

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(surahs) { if (surahs.isNotEmpty()) visible = true }

    val listAlpha by animateFloatAsState(
        targetValue   = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 400, delayMillis = 60),
        label         = "listAlpha"
    )
    val listScale by animateFloatAsState(
        targetValue   = if (visible) 1f else 0.97f,
        animationSpec = tween(durationMillis = 400, delayMillis = 60),
        label         = "listScale"
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                modifier = Modifier.background(brush = gradientBrush),
                title = {
                    Text(
                        "All Surahs",
                        style      = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor        = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().springOverscroll()) {
            if (surahs.isEmpty()) {
                Box(
                    modifier         = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularWavyProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                        .alpha(listAlpha)
                        .scale(listScale),
                    contentPadding = PaddingValues(
                        top    = innerPadding.calculateTopPadding(),
                        bottom = 100.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    itemsIndexed(surahs) { index, surah ->
                        SurahItem(
                            surah          = surah,
                            index          = index,
                            totalItems     = surahs.size,
                            surahNamesFont = surahNamesFont,
                            onClick        = { onSurahClick(surah.id, surah.nameTransliteration) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SurahItem(
    surah: SurahInfo,
    index: Int,
    totalItems: Int,
    surahNamesFont: FontFamily,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val topRadius    = if (index == 0) 20.dp else 4.dp
    val bottomRadius = if (index == totalItems - 1) 20.dp else 4.dp

    ListItem(
        headlineContent = {
            Text(surah.nameTransliteration, fontWeight = FontWeight.SemiBold)
        },
        supportingContent = {
            Text("${surah.type.replaceFirstChar { it.uppercase() }} • ${surah.totalVerses} Verses")
        },
        leadingContent = {
            Box(modifier = Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                Text(
                    text  = surah.id.toString(),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        trailingContent = {
            Text(
                text       = "surah" + surah.id.toString().padStart(3, '0'),
                fontFamily = surahNamesFont,
                fontWeight = FontWeight.Normal,
                fontSize   = 36.sp,
                color      = MaterialTheme.colorScheme.primary
            )
        },
        modifier = Modifier
            .clip(RoundedCornerShape(
                topStart    = topRadius,    topEnd    = topRadius,
                bottomStart = bottomRadius, bottomEnd = bottomRadius
            ))
            .clickable(
                interactionSource = interactionSource,
                indication        = ripple(),
                onClick           = onClick
            ),
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    )
}
