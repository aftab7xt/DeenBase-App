package com.deenbase.app.features.dhikr.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.deenbase.app.data.SettingsManager
import java.time.LocalDate
import kotlinx.coroutines.flow.first

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DhikrScreen(
    onDhikrClick: (dhikrId: String, period: String) -> Unit,
    onSubhanallahClick: () -> Unit,
    onTasbihClick: () -> Unit
) {
    val context = LocalContext.current
    val sm = remember { SettingsManager(context) }
    val today = LocalDate.now().toString()

    var bismillahMorningDone by remember { mutableStateOf(false) }
    var bismillahEveningDone by remember { mutableStateOf(false) }
    var radituEveningDone by remember { mutableStateOf(false) }
    var subhanallahDone by remember { mutableStateOf(false) }

    // All completion states read from DataStore — consistent, no cross-ViewModel scope issues
    LaunchedEffect(Unit) {
        bismillahMorningDone = sm.getDhikrCompletionDate("bismillah", "morning") == today
        bismillahEveningDone = sm.getDhikrCompletionDate("bismillah", "evening") == today
        radituEveningDone   = sm.getDhikrCompletionDate("raditu", "evening") == today
        subhanallahDone     = sm.subhanallahDate.first() == today && sm.subhanallahCount.first() >= 100
    }

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    val morningAlpha by animateFloatAsState(targetValue = if (visible) 1f else 0f, animationSpec = tween(400, delayMillis = 80),  label = "morningAlpha")
    val morningScale by animateFloatAsState(targetValue = if (visible) 1f else 0.96f, animationSpec = tween(400, delayMillis = 80),  label = "morningScale")
    val eveningAlpha by animateFloatAsState(targetValue = if (visible) 1f else 0f, animationSpec = tween(400, delayMillis = 180), label = "eveningAlpha")
    val eveningScale by animateFloatAsState(targetValue = if (visible) 1f else 0.96f, animationSpec = tween(400, delayMillis = 180), label = "eveningScale")
    val tasbihAlpha  by animateFloatAsState(targetValue = if (visible) 1f else 0f, animationSpec = tween(400, delayMillis = 280), label = "tasbihAlpha")
    val tasbihScale  by animateFloatAsState(targetValue = if (visible) 1f else 0.96f, animationSpec = tween(400, delayMillis = 280), label = "tasbihScale")

    val gradientBrush = Brush.verticalGradient(
        colors = listOf(MaterialTheme.colorScheme.background, Color.Transparent)
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Dhikr", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Spacer(modifier = Modifier.height(innerPadding.calculateTopPadding() + 4.dp))

                Column(modifier = Modifier.alpha(morningAlpha).scale(morningScale), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    DhikrSectionLabel("Morning Adhkar")
                    DhikrCard("Protection from All Harm", "Bismillahil-ladhi la yadurru... x3", bismillahMorningDone, topShape = true, bottomShape = true) { onDhikrClick("bismillah", "morning") }
                }

                Column(modifier = Modifier.alpha(eveningAlpha).scale(eveningScale), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    DhikrSectionLabel("Evening Adhkar")
                    DhikrCard("Protection from All Harm", "Bismillahil-ladhi la yadurru... x3", bismillahEveningDone, topShape = true,  bottomShape = false) { onDhikrClick("bismillah", "evening") }
                    DhikrCard("Contentment with Allah",   "Raditu billahi Rabban, wa bil-Islami... x3",  radituEveningDone,   topShape = false, bottomShape = true)  { onDhikrClick("raditu",    "evening") }
                }

                Column(modifier = Modifier.alpha(tasbihAlpha).scale(tasbihScale), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    DhikrSectionLabel("Daily Adhkar")
                    DhikrCard("Forgiveness of All Sins", "Subhanallahi wa bihamdihi. x100", subhanallahDone, topShape = true, bottomShape = true, onClick = onSubhanallahClick)
                }

                Column(modifier = Modifier.alpha(tasbihAlpha).scale(tasbihScale), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    DhikrSectionLabel("Tasbih")
                    Card(
                        onClick = onTasbihClick,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("Tasbih Counter", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(100.dp))
            }

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
private fun DhikrSectionLabel(title: String) {
    Text(text = title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 8.dp, top = 8.dp, bottom = 8.dp))
}

@Composable
private fun DhikrCard(title: String, subtitle: String, isDone: Boolean, topShape: Boolean, bottomShape: Boolean, onClick: () -> Unit) {
    ListItem(
        headlineContent = {
            Text(title, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis,
                color = if (isDone) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
        },
        supportingContent = {
            Text(subtitle, maxLines = 1, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurfaceVariant)
        },
        trailingContent = {
            if (isDone) Icon(Icons.Filled.CheckCircle, contentDescription = "Done", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
        },
        modifier = Modifier
            .clip(RoundedCornerShape(
                topStart = if (topShape) 20.dp else 4.dp, topEnd = if (topShape) 20.dp else 4.dp,
                bottomStart = if (bottomShape) 20.dp else 4.dp, bottomEnd = if (bottomShape) 20.dp else 4.dp
            ))
            .clickable(onClick = onClick),
        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    )
}

