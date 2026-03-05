package com.deenbase.app.features.quran.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.motionScheme
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue // Explicitly added to fix delegate error
import androidx.compose.runtime.collectAsState // Explicitly added to fix flow error
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.deenbase.app.R
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.deenbase.app.features.quran.data.SurahInfo
import com.deenbase.app.features.quran.viewmodel.QuranViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun QuranScreen(
    viewModel: QuranViewModel = viewModel(),
    onSurahClick: (Int, String) -> Unit
) {
    val surahs by viewModel.surahs.collectAsState()
    // Hoist font creation here — NOT inside each list item
    val surahNamesFont = remember { FontFamily(Font(R.font.surah_names)) }

    // 1. Create the gradient brush from background color to transparent
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(MaterialTheme.colorScheme.background, Color.Transparent)
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                // 2. Apply the gradient brush directly to the TopAppBar modifier
                modifier = Modifier.background(brush = gradientBrush),
                title = {
                    Text(
                        "Quran",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent, // Gradient handles the background now
                    scrolledContainerColor = Color.Transparent
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (surahs.isEmpty()) {
            // Give the loader padding so it doesn't get stuck under the top bar
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularWavyProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    // 3. REMOVED .padding(innerPadding) so the list draws edge-to-edge behind the top bar
                    .padding(horizontal = 16.dp),
                
                // 4. ADDED contentPadding so the first item naturally starts below the top bar
                contentPadding = PaddingValues(
                    top = innerPadding.calculateTopPadding(), 
                    bottom = innerPadding.calculateBottomPadding()
                ),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                itemsIndexed(surahs) { index, surah ->
                    SurahItem(
                        surah = surah,
                        index = index,
                        totalItems = surahs.size,
                        surahNamesFont = surahNamesFont,
                        onClick = { onSurahClick(surah.id, surah.nameTransliteration) }
                    )
                }
                item { Spacer(modifier = Modifier.height(100.dp)) }
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

    val topRadius = if (index == 0) 20.dp else 4.dp
    val bottomRadius = if (index == totalItems - 1) 20.dp else 4.dp

    ListItem(
        headlineContent = { 
            Text(surah.nameTransliteration, fontWeight = FontWeight.SemiBold) 
        },
        supportingContent = { 
            Text("${surah.type.replaceFirstChar { it.uppercase() }} • ${surah.totalVerses} Verses") 
        },
        leadingContent = {
            Box(
                modifier = Modifier.size(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = surah.id.toString(),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        trailingContent = {
            Text(
                text = "surah" + surah.id.toString().padStart(3, '0'),
                fontFamily = surahNamesFont,
                fontWeight = FontWeight.Normal,
                fontSize = 36.sp,
                color = MaterialTheme.colorScheme.primary
            )
        },
        modifier = Modifier
            .clip(
                RoundedCornerShape(
                    topStart = topRadius,
                    topEnd = topRadius,
                    bottomStart = bottomRadius,
                    bottomEnd = bottomRadius
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
