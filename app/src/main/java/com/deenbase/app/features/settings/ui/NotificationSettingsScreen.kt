package com.deenbase.app.features.settings.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.deenbase.app.features.settings.viewmodel.NotificationSettingsViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: NotificationSettingsViewModel = viewModel()
) {
    val quranEnabled       by viewModel.quranEnabled.collectAsState()
    val quranHour1         by viewModel.quranHour1.collectAsState()
    val quranMinute1       by viewModel.quranMinute1.collectAsState()
    val quranSecondEnabled by viewModel.quranSecondEnabled.collectAsState()
    val quranHour2         by viewModel.quranHour2.collectAsState()
    val quranMinute2       by viewModel.quranMinute2.collectAsState()

    val dhikrEnabled       by viewModel.dhikrEnabled.collectAsState()
    val dhikrHour1         by viewModel.dhikrHour1.collectAsState()
    val dhikrMinute1       by viewModel.dhikrMinute1.collectAsState()
    val dhikrSecondEnabled by viewModel.dhikrSecondEnabled.collectAsState()
    val dhikrHour2         by viewModel.dhikrHour2.collectAsState()
    val dhikrMinute2       by viewModel.dhikrMinute2.collectAsState()

    // Time picker dialog state
    var showPicker by remember { mutableStateOf(false) }
    var pickerTitle by remember { mutableStateOf("") }
    var pickerState by remember { mutableStateOf<TimePickerState?>(null) }
    var onPickerConfirm by remember { mutableStateOf<(Int, Int) -> Unit>({ _, _ -> }) }

    // Permission handling
    val context = LocalContext.current
    var pendingToggle by remember { mutableStateOf<(() -> Unit)?>(null) }

    fun hasNotifPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else true
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) pendingToggle?.invoke()
        pendingToggle = null
    }

    fun requestOrRun(onGranted: () -> Unit) {
        if (hasNotifPermission()) {
            onGranted()
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pendingToggle = onGranted
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    fun openPicker(title: String, hour: Int, minute: Int, onConfirm: (Int, Int) -> Unit) {
        pickerTitle = title
        pickerState = TimePickerState(initialHour = hour, initialMinute = minute, is24Hour = false)
        onPickerConfirm = onConfirm
        showPicker = true
    }

    if (showPicker && pickerState != null) {
        val state = pickerState!!
        AlertDialog(
            onDismissRequest = { showPicker = false },
            title = { Text(pickerTitle) },
            text = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TimePicker(state = state)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onPickerConfirm(state.hour, state.minute)
                    showPicker = false
                }) { Text("Set") }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text("Cancel") }
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
                    Text(
                        "Notification Settings",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
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

            // ── QURAN REMINDER ────────────────────────────────────────────
            NotifSectionLabel("Quran Reminder")

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {

                // Enable toggle
                ListItem(
                    headlineContent = { Text("Daily Reminder", fontWeight = FontWeight.SemiBold) },
                    supportingContent = { Text("Get reminded to read your daily Quran goal") },
                    trailingContent = {
                        Switch(
                            checked = quranEnabled,
                            onCheckedChange = { enabled ->
                                if (enabled) requestOrRun { viewModel.setQuranEnabled(true) }
                                else viewModel.setQuranEnabled(false)
                            },
                            colors = SwitchDefaults.colors(
                                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                uncheckedThumbColor = MaterialTheme.colorScheme.onSurface,
                                uncheckedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )
                    },
                    modifier = Modifier.clip(
                        if (!quranEnabled) RoundedCornerShape(20.dp)
                        else RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 4.dp, bottomEnd = 4.dp)
                    ),
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                )

                AnimatedVisibility(
                    visible = quranEnabled,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {

                        // Time picker 1
                        ListItem(
                            headlineContent = { Text("Reminder Time", fontWeight = FontWeight.SemiBold) },
                            supportingContent = { Text(formatTime(quranHour1, quranMinute1)) },
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable {
                                    openPicker("Quran Reminder Time", quranHour1, quranMinute1) { h, m ->
                                        viewModel.setQuranTime1(h, m)
                                    }
                                },
                            colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                        )

                        // Second reminder toggle
                        ListItem(
                            headlineContent = { Text("Second Reminder", fontWeight = FontWeight.SemiBold) },
                            supportingContent = { Text("Missed it? Get a follow-up alert") },
                            trailingContent = {
                                Switch(
                                    checked = quranSecondEnabled,
                                    onCheckedChange = { viewModel.setQuranSecondEnabled(it) },
                                    colors = SwitchDefaults.colors(
                                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                        uncheckedThumbColor = MaterialTheme.colorScheme.onSurface,
                                        uncheckedBorderColor = MaterialTheme.colorScheme.outline
                                    )
                                )
                            },
                            modifier = Modifier.clip(
                                if (!quranSecondEnabled) RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 20.dp, bottomEnd = 20.dp)
                                else RoundedCornerShape(4.dp)
                            ),
                            colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                        )

                        AnimatedVisibility(
                            visible = quranSecondEnabled,
                            enter = expandVertically(),
                            exit = shrinkVertically()
                        ) {
                            ListItem(
                                headlineContent = { Text("Second Reminder Time", fontWeight = FontWeight.SemiBold) },
                                supportingContent = { Text(formatTime(quranHour2, quranMinute2)) },
                                modifier = Modifier
                                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 20.dp, bottomEnd = 20.dp))
                                    .clickable {
                                        openPicker("Second Quran Reminder", quranHour2, quranMinute2) { h, m ->
                                            viewModel.setQuranTime2(h, m)
                                        }
                                    },
                                colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                            )
                        }
                    }
                }
            }

            // ── DHIKR REMINDER ────────────────────────────────────────────
            NotifSectionLabel("Dhikr Reminder")

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {

                ListItem(
                    headlineContent = { Text("Daily Reminder", fontWeight = FontWeight.SemiBold) },
                    supportingContent = { Text("Reminder for Subhanallahi wa bihamdihi ×100") },
                    trailingContent = {
                        Switch(
                            checked = dhikrEnabled,
                            onCheckedChange = { enabled ->
                                if (enabled) requestOrRun { viewModel.setDhikrEnabled(true) }
                                else viewModel.setDhikrEnabled(false)
                            },
                            colors = SwitchDefaults.colors(
                                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                uncheckedThumbColor = MaterialTheme.colorScheme.onSurface,
                                uncheckedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )
                    },
                    modifier = Modifier.clip(
                        if (!dhikrEnabled) RoundedCornerShape(20.dp)
                        else RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 4.dp, bottomEnd = 4.dp)
                    ),
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                )

                AnimatedVisibility(
                    visible = dhikrEnabled,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {

                        ListItem(
                            headlineContent = { Text("Reminder Time", fontWeight = FontWeight.SemiBold) },
                            supportingContent = { Text(formatTime(dhikrHour1, dhikrMinute1)) },
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable {
                                    openPicker("Dhikr Reminder Time", dhikrHour1, dhikrMinute1) { h, m ->
                                        viewModel.setDhikrTime1(h, m)
                                    }
                                },
                            colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                        )

                        ListItem(
                            headlineContent = { Text("Second Reminder", fontWeight = FontWeight.SemiBold) },
                            supportingContent = { Text("Missed it? Get a follow-up alert") },
                            trailingContent = {
                                Switch(
                                    checked = dhikrSecondEnabled,
                                    onCheckedChange = { viewModel.setDhikrSecondEnabled(it) },
                                    colors = SwitchDefaults.colors(
                                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                        uncheckedThumbColor = MaterialTheme.colorScheme.onSurface,
                                        uncheckedBorderColor = MaterialTheme.colorScheme.outline
                                    )
                                )
                            },
                            modifier = Modifier.clip(
                                if (!dhikrSecondEnabled) RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 20.dp, bottomEnd = 20.dp)
                                else RoundedCornerShape(4.dp)
                            ),
                            colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                        )

                        AnimatedVisibility(
                            visible = dhikrSecondEnabled,
                            enter = expandVertically(),
                            exit = shrinkVertically()
                        ) {
                            ListItem(
                                headlineContent = { Text("Second Reminder Time", fontWeight = FontWeight.SemiBold) },
                                supportingContent = { Text(formatTime(dhikrHour2, dhikrMinute2)) },
                                modifier = Modifier
                                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 20.dp, bottomEnd = 20.dp))
                                    .clickable {
                                        openPicker("Second Dhikr Reminder", dhikrHour2, dhikrMinute2) { h, m ->
                                            viewModel.setDhikrTime2(h, m)
                                        }
                                    },
                                colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
private fun NotifSectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 8.dp, bottom = 8.dp, top = 24.dp)
    )
}

private fun formatTime(hour: Int, minute: Int): String {
    val amPm = if (hour < 12) "AM" else "PM"
    val h = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    return String.format(Locale.getDefault(), "%d:%02d %s", h, minute, amPm)
}
