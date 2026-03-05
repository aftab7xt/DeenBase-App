package com.deenbase.app.features.settings.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.deenbase.app.features.settings.viewmodel.AppPreferencesViewModel

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 8.dp, bottom = 8.dp, top = 24.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppPreferencesScreen(
    onNavigateBack: () -> Unit,
    viewModel: AppPreferencesViewModel = viewModel()
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val hapticsEnabled by viewModel.hapticsEnabled.collectAsState()
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
                TextButton(onClick = {
                    viewModel.resetAllData()
                    showResetDialog = false
                }) {
                    Text("Reset", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                modifier = Modifier.background(brush = gradientBrush),
                title = {
                    Text(
                        "App Preferences",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
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

            // ── APPEARANCE ───────────────────────────────────────────────
            SectionLabel("Appearance")

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {

                // Theme picker
                Surface(
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 4.dp, bottomEnd = 4.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                        Text("Theme", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge)
                        Spacer(modifier = Modifier.height(12.dp))
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

                // Haptics toggle
                ListItem(
                    headlineContent = { Text("Haptics", fontWeight = FontWeight.SemiBold) },
                    supportingContent = { Text("Vibration feedback on button taps") },
                    trailingContent = {
                        Switch(
                            checked = hapticsEnabled,
                            onCheckedChange = { viewModel.setHapticsEnabled(it) },
                            colors = SwitchDefaults.colors(
                                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                uncheckedThumbColor = MaterialTheme.colorScheme.onSurface,
                                uncheckedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )
                    },
                    modifier = Modifier.clip(
                        RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 20.dp, bottomEnd = 20.dp)
                    ),
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                )
            }

            // ── DATA ─────────────────────────────────────────────────────
            SectionLabel("Data")

            ListItem(
                headlineContent = {
                    Text("Reset All Data", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.error)
                },
                supportingContent = { Text("Clear all progress, goals, and tasbih counts") },
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { showResetDialog = true },
                colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
            )

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}
