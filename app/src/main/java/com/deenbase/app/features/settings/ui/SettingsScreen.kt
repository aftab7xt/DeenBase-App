package com.deenbase.app.features.settings.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SystemUpdateAlt
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.motionScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deenbase.app.BuildConfig
import com.deenbase.app.ui.springOverscroll

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onQuranSettingsClick: () -> Unit,
    onQuranGoalClick: () -> Unit,
    onAppPreferencesClick: () -> Unit,
    onNotificationSettingsClick: () -> Unit,
    onAboutClick: () -> Unit = {},
    onCheckForUpdatesClick: () -> Unit = {},
    isCheckingForUpdates: Boolean = false
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    val quranAlpha by animateFloatAsState(targetValue = if (visible) 1f else 0f, animationSpec = tween(400, delayMillis = 80),  label = "quranAlpha")
    val quranScale by animateFloatAsState(targetValue = if (visible) 1f else 0.96f, animationSpec = tween(400, delayMillis = 80),  label = "quranScale")
    val appAlpha   by animateFloatAsState(targetValue = if (visible) 1f else 0f, animationSpec = tween(400, delayMillis = 180), label = "appAlpha")
    val appScale   by animateFloatAsState(targetValue = if (visible) 1f else 0.96f, animationSpec = tween(400, delayMillis = 180), label = "appScale")
    val aboutAlpha by animateFloatAsState(targetValue = if (visible) 1f else 0f, animationSpec = tween(400, delayMillis = 280), label = "aboutAlpha")
    val aboutScale by animateFloatAsState(targetValue = if (visible) 1f else 0.96f, animationSpec = tween(400, delayMillis = 280), label = "aboutScale")

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Settings", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().springOverscroll()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // ── QURAN ────────────────────────────────────────────────────
            Column(modifier = Modifier.alpha(quranAlpha).scale(quranScale)) {
                SectionLabel("Quran", topPadding = 8.dp)
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    SettingsItem("Quran Settings",  "Font, translation, display", Icons.AutoMirrored.Filled.MenuBook, 0, 2, onQuranSettingsClick)
                    SettingsItem("Reading Goal",    "Daily verse target",         Icons.Filled.TrackChanges,          1, 2, onQuranGoalClick)
                }
            }

            // ── APP ──────────────────────────────────────────────────────
            Column(modifier = Modifier.alpha(appAlpha).scale(appScale)) {
                SectionLabel("App")
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    SettingsItem("App Preferences",       "Theme, haptics",            Icons.Filled.Settings,          0, 2, onAppPreferencesClick)
                    SettingsItem("Notification Settings", "Quran and Dhikr reminders", Icons.Filled.NotificationsNone, 1, 2, onNotificationSettingsClick)
                }
            }

            // ── ABOUT ────────────────────────────────────────────────────
            Column(modifier = Modifier.alpha(aboutAlpha).scale(aboutScale)) {
                SectionLabel("About")
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    SettingsItem(
                        title = "About DeenBase",
                        subtitle = "App info, developer, license",
                        leadingIcon = Icons.Filled.Info,
                        index = 0, totalItems = 2,
                        onClick = onAboutClick
                    )
                    SettingsItem(
                        title = if (isCheckingForUpdates) "Checking..." else "Check for Updates",
                        subtitle = "v${BuildConfig.VERSION_NAME} installed",
                        leadingIcon = Icons.Filled.SystemUpdateAlt,
                        index = 1, totalItems = 2,
                        onClick = { if (!isCheckingForUpdates) onCheckForUpdatesClick() }
                    )
                }
            }
        }
    }
        } // end springOverscroll
}

@Composable
fun SectionLabel(text: String, topPadding: androidx.compose.ui.unit.Dp = 24.dp) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 4.dp, bottom = 6.dp, top = topPadding)
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsItem(
    title: String,
    subtitle: String,
    leadingIcon: ImageVector,
    index: Int,
    totalItems: Int,
    onClick: () -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val topRadius by animateDpAsState(
        targetValue = if (isPressed || index == 0 || totalItems == 1) 20.dp else 4.dp,
        animationSpec = motionScheme.fastSpatialSpec(), label = "top"
    )
    val bottomRadius by animateDpAsState(
        targetValue = if (isPressed || index == totalItems - 1 || totalItems == 1) 20.dp else 4.dp,
        animationSpec = motionScheme.fastSpatialSpec(), label = "bottom"
    )

    Surface(
        shape = RoundedCornerShape(topStart = topRadius, topEnd = topRadius, bottomStart = bottomRadius, bottomEnd = bottomRadius),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = topRadius, topEnd = topRadius, bottomStart = bottomRadius, bottomEnd = bottomRadius))
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (onClick != {}) {
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

