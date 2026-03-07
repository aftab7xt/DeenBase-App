package com.deenbase.app.features.settings.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.motionScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.deenbase.app.features.settings.viewmodel.AppPreferencesViewModel

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SliderSettingsItem(
    title: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    displayValue: String,
    index: Int,
    totalItems: Int,
    onValueChange: (Float) -> Unit
) {
    val topRadius by animateDpAsState(
        targetValue = if (index == 0 || totalItems == 1) 20.dp else 4.dp,
        animationSpec = motionScheme.fastSpatialSpec(), label = "top"
    )
    val bottomRadius by animateDpAsState(
        targetValue = if (index == totalItems - 1 || totalItems == 1) 20.dp else 4.dp,
        animationSpec = motionScheme.fastSpatialSpec(), label = "bottom"
    )

    Surface(
        shape = RoundedCornerShape(topStart = topRadius, topEnd = topRadius, bottomStart = bottomRadius, bottomEnd = bottomRadius),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyMedium)
                Text(displayValue, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            }
            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = valueRange,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SwitchSettingsItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    index: Int,
    totalItems: Int,
    onCheckedChange: (Boolean) -> Unit
) {
    val topRadius by animateDpAsState(
        targetValue = if (index == 0 || totalItems == 1) 20.dp else 4.dp,
        animationSpec = motionScheme.fastSpatialSpec(), label = "top"
    )
    val bottomRadius by animateDpAsState(
        targetValue = if (index == totalItems - 1 || totalItems == 1) 20.dp else 4.dp,
        animationSpec = motionScheme.fastSpatialSpec(), label = "bottom"
    )

    Surface(
        shape = RoundedCornerShape(topStart = topRadius, topEnd = topRadius, bottomStart = bottomRadius, bottomEnd = bottomRadius),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    uncheckedThumbColor = MaterialTheme.colorScheme.onSurface,
                    uncheckedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppPreferencesScreen(
    onNavigateBack: () -> Unit,
    viewModel: AppPreferencesViewModel = viewModel()
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val hapticsEnabled by viewModel.hapticsEnabled.collectAsState()
    val oledMode by viewModel.oledMode.collectAsState()
    var showResetDialog by remember { mutableStateOf(false) }

    val gradientBrush = Brush.verticalGradient(
        colors = listOf(MaterialTheme.colorScheme.background, Color.Transparent)
    )

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset All Data?") },
            text = { Text("This will clear all your progress, goals, and tasbih counts. This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = { viewModel.resetAllData(); showResetDialog = false }) {
                    Text("Reset", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                modifier = Modifier.background(brush = gradientBrush),
                title = { Text("App Preferences", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold) },
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
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(innerPadding.calculateTopPadding()))

            // ── APPEARANCE ────────────────────────────────────────────────────
            SectionLabel("Appearance", topPadding = 8.dp)

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                // Theme picker (index 0)
                Surface(
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 4.dp, bottomEnd = 4.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                        Text("Theme", fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(10.dp))
                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            listOf("system" to "System", "light" to "Light", "dark" to "Dark")
                                .forEachIndexed { i, (key, label) ->
                                    SegmentedButton(
                                        selected = themeMode == key,
                                        onClick = { viewModel.setThemeMode(key) },
                                        shape = SegmentedButtonDefaults.itemShape(index = i, count = 3),
                                        label = { Text(label) }
                                    )
                                }
                        }
                    }
                }

                // OLED Mode (index 1)
                SwitchSettingsItem(
                    title = "OLED Mode",
                    subtitle = "Pure black background in dark theme",
                    checked = oledMode,
                    index = 1, totalItems = 3,
                    onCheckedChange = { viewModel.setOledMode(it) }
                )

                // Haptics (index 2 — last)
                SwitchSettingsItem(
                    title = "Haptics",
                    subtitle = "Vibration feedback on button taps",
                    checked = hapticsEnabled,
                    index = 2, totalItems = 3,
                    onCheckedChange = { viewModel.setHapticsEnabled(it) }
                )
            }

            // ── DATA ──────────────────────────────────────────────────────────
            SectionLabel("Data")

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { showResetDialog = true }
            ) {
                Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Reset All Data", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.error)
                        Text("Clear all progress, goals, and tasbih counts", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}
