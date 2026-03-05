package com.deenbase.app.features.home.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.deenbase.app.features.home.viewmodel.GoalViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HomeScreen(
    onReadQuranClick: (Int, Int) -> Unit,
    onHadithClick: () -> Unit,
    goalViewModel: GoalViewModel = viewModel()
) {
    val surahs by goalViewModel.surahs.collectAsState()
    val goalSurahId by goalViewModel.goalSurahId.collectAsState()
    val goalVerse by goalViewModel.goalVerse.collectAsState()
    val dailyTarget by goalViewModel.dailyTarget.collectAsState()
    val todayCount by goalViewModel.todayCount.collectAsState()

    var showPositionDialog by remember { mutableStateOf(false) }
    var selectedSurahId by remember { mutableIntStateOf(goalSurahId) }
    var verseInput by remember { mutableStateOf(goalVerse.toString()) }

    val currentSurah = surahs.find { it.id == goalSurahId }
    val progress = if (dailyTarget > 0) (todayCount.toFloat() / dailyTarget).coerceIn(0f, 1f) else 0f
    val percent = (progress * 100).toInt()

    // Entrance animation state — triggers once on first composition
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    val goalAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 400, delayMillis = 80),
        label = "goalAlpha"
    )
    val goalOffset by animateFloatAsState(
        targetValue = if (visible) 1f else 0.96f,
        animationSpec = tween(durationMillis = 400, delayMillis = 80),
        label = "goalOffset"
    )
    val tasbihAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 400, delayMillis = 180),
        label = "tasbihAlpha"
    )
    val tasbihOffset by animateFloatAsState(
        targetValue = if (visible) 1f else 0.96f,
        animationSpec = tween(durationMillis = 400, delayMillis = 180),
        label = "tasbihOffset"
    )

    // Position picker dialog
    if (showPositionDialog) {
        val surahList = surahs
        val selectedSurah = surahList.find { it.id == selectedSurahId }
        LaunchedEffect(goalSurahId, goalVerse) {
            selectedSurahId = goalSurahId
            verseInput = goalVerse.toString()
        }
        var surahDropdownExpanded by remember { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = { showPositionDialog = false },
            title = { Text("Set Reading Position") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    ExposedDropdownMenuBox(
                        expanded = surahDropdownExpanded,
                        onExpandedChange = { surahDropdownExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = if (selectedSurah != null) "${selectedSurah.id}. ${selectedSurah.nameTransliteration}" else "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Surah") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = surahDropdownExpanded) },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        )
                        ExposedDropdownMenu(
                            expanded = surahDropdownExpanded,
                            onDismissRequest = { surahDropdownExpanded = false }
                        ) {
                            if (surahList.isEmpty()) {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    LinearWavyProgressIndicator(modifier = Modifier.fillMaxWidth())
                                }
                            } else {
                                Box(modifier = Modifier.heightIn(max = 300.dp).verticalScroll(rememberScrollState())) {
                                    Column {
                                        surahList.forEach { surah ->
                                            DropdownMenuItem(
                                                text = { Text("${surah.id}. ${surah.nameTransliteration}") },
                                                onClick = {
                                                    selectedSurahId = surah.id
                                                    verseInput = "1"
                                                    surahDropdownExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    OutlinedTextField(
                        value = verseInput,
                        onValueChange = { verseInput = it.filter { c -> c.isDigit() }.take(3) },
                        label = { Text("Verse") },
                        supportingText = { if (selectedSurah != null) Text("1 – ${selectedSurah.totalVerses}") },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val verse = verseInput.toIntOrNull() ?: 1
                    val maxVerse = surahs.find { it.id == selectedSurahId }?.totalVerses ?: 1
                    goalViewModel.setGoalPosition(selectedSurahId, verse.coerceIn(1, maxVerse))
                    showPositionDialog = false
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showPositionDialog = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "DeenBase",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // ── GOAL CARD ────────────────────────────────────────────────
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(goalAlpha)
                    .scale(goalOffset),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Goal",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$percent%",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (currentSurah != null)
                                "${currentSurah.id} ${currentSurah.nameTransliteration} | $goalVerse/${currentSurah.totalVerses}"
                            else "Loading...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        IconButton(
                            onClick = { showPositionDialog = true },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = "Edit goal",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(8.dp),
                        trackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = "$todayCount/$dailyTarget verses today",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Button(
                        onClick = { onReadQuranClick(goalSurahId, goalVerse) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Read Quran", fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // ── HADITH ───────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(tasbihAlpha)
                    .scale(tasbihOffset),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Card(
                    onClick = onHadithClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "Explore Hadiths",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Browse authentic hadiths",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = Icons.Filled.Loop,
                            contentDescription = "Hadith",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}
