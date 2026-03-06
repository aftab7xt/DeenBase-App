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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.deenbase.app.BuildConfig

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onQuranSettingsClick: () -> Unit,
    onQuranGoalClick: () -> Unit,
    onAppPreferencesClick: () -> Unit,
    onNotificationSettingsClick: () -> Unit,
    onCheckForUpdatesClick: () -> Unit = {},
    isCheckingForUpdates: Boolean = false
) {
    // ── Entrance animation ────────────────────────────────────────────────────
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    val quranAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 400, delayMillis = 80),
        label = "quranAlpha"
    )
    val quranScale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.96f,
        animationSpec = tween(durationMillis = 400, delayMillis = 80),
        label = "quranScale"
    )
    val appAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 400, delayMillis = 180),
        label = "appAlpha"
    )
    val appScale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.96f,
        animationSpec = tween(durationMillis = 400, delayMillis = 180),
        label = "appScale"
    )
    val aboutAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 400, delayMillis = 280),
        label = "aboutAlpha"
    )
    val aboutScale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.96f,
        animationSpec = tween(durationMillis = 400, delayMillis = 280),
        label = "aboutScale"
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Settings",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
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
                .verticalScroll(rememberScrollState())
        ) {
            // ── QURAN ────────────────────────────────────────────────────
            Column(modifier = Modifier.alpha(quranAlpha).scale(quranScale)) {
                Text(
                    text = "Quran",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 8.dp, bottom = 8.dp, top = 8.dp)
                )
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    SettingsItem(
                        title = "Quran Settings",
                        subtitle = "Font, translation, display",
                        leadingIcon = Icons.AutoMirrored.Filled.MenuBook,
                        index = 0,
                        totalItems = 2,
                        onClick = onQuranSettingsClick
                    )
                    SettingsItem(
                        title = "Reading Goal",
                        subtitle = "Daily verse target",
                        leadingIcon = Icons.Filled.TrackChanges,
                        index = 1,
                        totalItems = 2,
                        onClick = onQuranGoalClick
                    )
                }
            }

            // ── APP ──────────────────────────────────────────────────────
            Column(modifier = Modifier.alpha(appAlpha).scale(appScale)) {
                Text(
                    text = "App",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 8.dp, bottom = 8.dp, top = 24.dp)
                )
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    SettingsItem(
                        title = "App Preferences",
                        subtitle = "Theme, haptics",
                        leadingIcon = Icons.Filled.Settings,
                        index = 0,
                        totalItems = 2,
                        onClick = onAppPreferencesClick
                    )
                    SettingsItem(
                        title = "Notification Settings",
                        subtitle = "Quran and Dhikr reminders",
                        leadingIcon = Icons.Filled.NotificationsNone,
                        index = 1,
                        totalItems = 2,
                        onClick = onNotificationSettingsClick
                    )
                }
            }

            // ── ABOUT ────────────────────────────────────────────────────
            Column(modifier = Modifier.alpha(aboutAlpha).scale(aboutScale)) {
                Text(
                    text = "About",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 8.dp, bottom = 8.dp, top = 24.dp)
                )
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    SettingsItem(
                        title = "DeenBase Version",
                        subtitle = BuildConfig.VERSION_NAME,
                        leadingIcon = Icons.Filled.Info,
                        index = 0,
                        totalItems = 2
                    )
                    SettingsItem(
                        title = if (isCheckingForUpdates) "Checking..." else "Check for Updates",
                        subtitle = "Tap to check for the latest version",
                        leadingIcon = Icons.Filled.SystemUpdateAlt,
                        index = 1,
                        totalItems = 2,
                        onClick = { if (!isCheckingForUpdates) onCheckForUpdatesClick() }
                    )
                }
            }
        }
    }
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
        animationSpec = motionScheme.fastSpatialSpec(),
        label = "topRounding"
    )
    val bottomRadius by animateDpAsState(
        targetValue = if (isPressed || index == totalItems - 1 || totalItems == 1) 20.dp else 4.dp,
        animationSpec = motionScheme.fastSpatialSpec(),
        label = "bottomRounding"
    )

    val shape = RoundedCornerShape(
        topStart = topRadius, topEnd = topRadius,
        bottomStart = bottomRadius, bottomEnd = bottomRadius
    )

    ListItem(
        headlineContent = {
            Text(title, fontWeight = FontWeight.Medium)
        },
        supportingContent = {
            Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
        },
        leadingContent = {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        trailingContent = {
            if (onClick != {}) {
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    )
}
