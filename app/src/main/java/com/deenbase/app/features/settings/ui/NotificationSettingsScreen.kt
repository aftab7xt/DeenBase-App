package com.deenbase.app.features.settings.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.deenbase.app.ui.springOverscroll
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.deenbase.app.features.settings.viewmodel.NotificationSettingsViewModel
import java.util.Locale

@Composable
private fun NotifSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    shape: RoundedCornerShape,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(shape = shape, color = MaterialTheme.colorScheme.surfaceContainerLow, modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange, colors = SwitchDefaults.colors(uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest, uncheckedThumbColor = MaterialTheme.colorScheme.onSurface, uncheckedBorderColor = MaterialTheme.colorScheme.outline))
        }
    }
}

@Composable
private fun NotifTimeRow(title: String, time: String, shape: RoundedCornerShape, onClick: () -> Unit) {
    Surface(shape = shape, color = MaterialTheme.colorScheme.surfaceContainerLow, modifier = Modifier.fillMaxWidth().clip(shape).clickable(onClick = onClick)) {
        Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                Text(time, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun NotifSectionLabel(text: String) {
    Text(text = text, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(start = 4.dp, bottom = 6.dp, top = 24.dp))
}

private fun formatTime(hour: Int, minute: Int): String {
    val amPm = if (hour < 12) "AM" else "PM"
    val h = when { hour == 0 -> 12; hour > 12 -> hour - 12; else -> hour }
    return String.format(Locale.getDefault(), "%d:%02d %s", h, minute, amPm)
}

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

    var showPicker       by remember { mutableStateOf(false) }
    var pickerTitle      by remember { mutableStateOf("") }
    var pickerState      by remember { mutableStateOf<TimePickerState?>(null) }
    var onPickerConfirm  by remember { mutableStateOf<(Int, Int) -> Unit>({ _, _ -> }) }

    val context = LocalContext.current
    var pendingToggle by remember { mutableStateOf<(() -> Unit)?>(null) }

    // ── Entrance animation ────────────────────────────────────────────────────
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    val contentAlpha by animateFloatAsState(targetValue = if (visible) 1f else 0f, animationSpec = tween(400, 60), label = "alpha")
    val contentScale by animateFloatAsState(targetValue = if (visible) 1f else 0.97f, animationSpec = tween(400, 60), label = "scale")

    fun hasNotifPermission(): Boolean = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    else true

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) pendingToggle?.invoke()
        pendingToggle = null
    }

    fun requestOrRun(onGranted: () -> Unit) {
        if (hasNotifPermission()) { onGranted() }
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pendingToggle = onGranted
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    fun openPicker(title: String, hour: Int, minute: Int, onConfirm: (Int, Int) -> Unit) {
        pickerTitle    = title
        pickerState    = TimePickerState(initialHour = hour, initialMinute = minute, is24Hour = false)
        onPickerConfirm = onConfirm
        showPicker     = true
    }

    if (showPicker && pickerState != null) {
        val state = pickerState!!
        AlertDialog(
            onDismissRequest = { showPicker = false },
            title   = { Text(pickerTitle) },
            text    = { Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { TimePicker(state = state) } },
            confirmButton = { TextButton(onClick = { onPickerConfirm(state.hour, state.minute); showPicker = false }) { Text("Set") } },
            dismissButton = { TextButton(onClick = { showPicker = false }) { Text("Cancel") } }
        )
    }

    val gradientBrush = Brush.verticalGradient(colors = listOf(MaterialTheme.colorScheme.background, Color.Transparent))

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                modifier = Modifier.background(brush = gradientBrush),
                title    = { Text("Notification Settings", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors   = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent, scrolledContainerColor = Color.Transparent)
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

                NotifSectionLabel("Quran Reminder")
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    NotifSwitchRow(
                        title    = "Daily Reminder",
                        subtitle = "Get reminded to read your daily Quran goal",
                        checked  = quranEnabled,
                        shape    = if (!quranEnabled) RoundedCornerShape(20.dp) else RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 4.dp, bottomEnd = 4.dp),
                        onCheckedChange = { if (it) requestOrRun { viewModel.setQuranEnabled(true) } else viewModel.setQuranEnabled(false) }
                    )
                    AnimatedVisibility(visible = quranEnabled, enter = expandVertically(), exit = shrinkVertically()) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            NotifTimeRow(title = "Reminder Time", time = formatTime(quranHour1, quranMinute1), shape = RoundedCornerShape(4.dp)) {
                                openPicker("Quran Reminder Time", quranHour1, quranMinute1) { h, m -> viewModel.setQuranTime1(h, m) }
                            }
                            NotifSwitchRow(
                                title    = "Second Reminder", subtitle = "Missed it? Get a follow-up alert",
                                checked  = quranSecondEnabled,
                                shape    = if (!quranSecondEnabled) RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 20.dp, bottomEnd = 20.dp) else RoundedCornerShape(4.dp),
                                onCheckedChange = { viewModel.setQuranSecondEnabled(it) }
                            )
                            AnimatedVisibility(visible = quranSecondEnabled, enter = expandVertically(), exit = shrinkVertically()) {
                                NotifTimeRow(title = "Second Reminder Time", time = formatTime(quranHour2, quranMinute2), shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 20.dp, bottomEnd = 20.dp)) {
                                    openPicker("Second Quran Reminder", quranHour2, quranMinute2) { h, m -> viewModel.setQuranTime2(h, m) }
                                }
                            }
                        }
                    }
                }

                NotifSectionLabel("Dhikr Reminder")
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    NotifSwitchRow(
                        title    = "Daily Reminder",
                        subtitle = "Reminder for Subhanallahi wa bihamdihi ×100",
                        checked  = dhikrEnabled,
                        shape    = if (!dhikrEnabled) RoundedCornerShape(20.dp) else RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 4.dp, bottomEnd = 4.dp),
                        onCheckedChange = { if (it) requestOrRun { viewModel.setDhikrEnabled(true) } else viewModel.setDhikrEnabled(false) }
                    )
                    AnimatedVisibility(visible = dhikrEnabled, enter = expandVertically(), exit = shrinkVertically()) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            NotifTimeRow(title = "Reminder Time", time = formatTime(dhikrHour1, dhikrMinute1), shape = RoundedCornerShape(4.dp)) {
                                openPicker("Dhikr Reminder Time", dhikrHour1, dhikrMinute1) { h, m -> viewModel.setDhikrTime1(h, m) }
                            }
                            NotifSwitchRow(
                                title    = "Second Reminder", subtitle = "Missed it? Get a follow-up alert",
                                checked  = dhikrSecondEnabled,
                                shape    = if (!dhikrSecondEnabled) RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 20.dp, bottomEnd = 20.dp) else RoundedCornerShape(4.dp),
                                onCheckedChange = { viewModel.setDhikrSecondEnabled(it) }
                            )
                            AnimatedVisibility(visible = dhikrSecondEnabled, enter = expandVertically(), exit = shrinkVertically()) {
                                NotifTimeRow(title = "Second Reminder Time", time = formatTime(dhikrHour2, dhikrMinute2), shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 20.dp, bottomEnd = 20.dp)) {
                                    openPicker("Second Dhikr Reminder", dhikrHour2, dhikrMinute2) { h, m -> viewModel.setDhikrTime2(h, m) }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}
