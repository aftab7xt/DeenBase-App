package com.deenbase.app.features.settings.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.motionScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onQuranSettingsClick: () -> Unit,
    onQuranGoalClick: () -> Unit,
    onAppPreferencesClick: () -> Unit,
    onNotificationSettingsClick: () -> Unit
) {
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
        ) {
            // ── QURAN ───────────────────────────────────────────────────
            Text(
                text = "Quran",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 8.dp, bottom = 8.dp, top = 16.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                SettingsItem(
                    title = "Quran Settings",
                    subtitle = "Font, translation, and display",
                    leadingIcon = Icons.AutoMirrored.Filled.MenuBook,
                    index = 0,
                    totalItems = 2,
                    onClick = onQuranSettingsClick
                )
                SettingsItem(
                    title = "Daily Quran Goal",
                    subtitle = "Set your daily verse target",
                    leadingIcon = Icons.Filled.TrackChanges,
                    index = 1,
                    totalItems = 2,
                    onClick = onQuranGoalClick
                )
            }

            // ── GENERAL ─────────────────────────────────────────────────
            Text(
                text = "General",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 8.dp, bottom = 8.dp, top = 24.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                SettingsItem(
                    title = "App Preferences",
                    subtitle = "Theme, haptics, and display",
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

            // ── ABOUT ────────────────────────────────────────────────────
            Text(
                text = "About",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 8.dp, bottom = 8.dp, top = 24.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                SettingsItem(
                    title = "DeenBase Version",
                    subtitle = "1.0.0 (Beta)",
                    leadingIcon = Icons.Filled.Info,
                    index = 0,
                    totalItems = 1
                )
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

    ListItem(
        headlineContent = { Text(title, fontWeight = FontWeight.SemiBold) },
        supportingContent = { Text(subtitle) },
        leadingContent = {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
        },
        trailingContent = {
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.outline
            )
        },
        modifier = Modifier
            .clip(RoundedCornerShape(topStart = topRadius, topEnd = topRadius, bottomStart = bottomRadius, bottomEnd = bottomRadius))
            .clickable(interactionSource = interactionSource, indication = ripple(), onClick = onClick),
        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    )
}
