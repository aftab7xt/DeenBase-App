package com.deenbase.app.features.onboarding.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.motionScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deenbase.app.data.SettingsManager
import kotlinx.coroutines.launch

// ── Page model ────────────────────────────────────────────────────────────────

private data class OnboardingPage(
    val title: String,
    val subtitle: String,
    val centerIcon: ImageVector,
    val topLeftIcon: ImageVector,
    val topRightIcon: ImageVector,
    val bottomLeftIcon: ImageVector,
    val bottomRightIcon: ImageVector,
    val actionLabel: String? = null   // non-null = show action button on this page
)

private val pages = listOf(
    OnboardingPage(
        title        = "DeenBase",
        subtitle     = "Your complete Islamic companion for Quran, Hadith, and daily Dhikr.",
        centerIcon   = Icons.Filled.Mosque,
        topLeftIcon  = Icons.Filled.Stars,
        topRightIcon = Icons.Filled.AutoStories,
        bottomLeftIcon = Icons.Filled.Favorite,
        bottomRightIcon = Icons.Filled.Nightlight,
    ),
    OnboardingPage(
        title        = "Quran",
        subtitle     = "Read every surah with translation. Set a daily goal and track your progress.",
        centerIcon   = Icons.AutoMirrored.Filled.MenuBook,
        topLeftIcon  = Icons.Filled.Bookmark,
        topRightIcon = Icons.Filled.Translate,
        bottomLeftIcon = Icons.Filled.TrackChanges,
        bottomRightIcon = Icons.Filled.BarChart,
    ),
    OnboardingPage(
        title        = "Hadith",
        subtitle     = "Browse 35,000+ authentic hadiths from Bukhari, Muslim, and five other collections.",
        centerIcon   = Icons.Filled.HistoryEdu,
        topLeftIcon  = Icons.Filled.Search,
        topRightIcon = Icons.Filled.Collections,
        bottomLeftIcon = Icons.Filled.FilterList,
        bottomRightIcon = Icons.Filled.Verified,
    ),
    OnboardingPage(
        title        = "Notifications",
        subtitle     = "Get gentle reminders for your Quran goal and daily morning & evening Dhikr.",
        centerIcon   = Icons.Filled.NotificationsActive,
        topLeftIcon  = Icons.Filled.WbSunny,
        topRightIcon = Icons.Filled.Nightlight,
        bottomLeftIcon = Icons.Filled.Schedule,
        bottomRightIcon = Icons.Filled.Circle,
        actionLabel  = "Enable Notifications"
    ),
    OnboardingPage(
        title        = "All Set!",
        subtitle     = "May Allah bless your journey. DeenBase is ready for you.",
        centerIcon   = Icons.Filled.CheckCircle,
        topLeftIcon  = Icons.Filled.Favorite,
        topRightIcon = Icons.Filled.Mosque,
        bottomLeftIcon = Icons.Filled.Stars,
        bottomRightIcon = Icons.Filled.Celebration,
    )
)

// ── Main screen ───────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun OnboardingScreen(
    onFinish: () -> Unit
) {
    val context = LocalContext.current
    val scope   = rememberCoroutineScope()
    var currentPage by remember { mutableIntStateOf(0) }
    val isLastPage = currentPage == pages.lastIndex
    val page = pages[currentPage]

    // ── Notification permission launcher ──────────────────────────────────────
    // Triggered ONLY by the "Enable Notifications" button on the Notifications page.
    // Advances to next page regardless of whether the user grants or denies.
    val notifLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* advance regardless of result */ currentPage++ }

    // ── Next button press animation ───────────────────────────────────────────
    val nextInteraction = remember { MutableInteractionSource() }
    val isNextPressed by nextInteraction.collectIsPressedAsState()

    val nextCorner by animateDpAsState(
        targetValue = if (isNextPressed) 20.dp else 50.dp,
        animationSpec = motionScheme.fastSpatialSpec(),
        label = "NextCorner"
    )
    val nextScale by animateFloatAsState(
        targetValue = if (isNextPressed) 0.92f else 1f,
        animationSpec = motionScheme.fastSpatialSpec(),
        label = "NextScale"
    )
    val nextColor by animateColorAsState(
        targetValue = if (isLastPage)
            MaterialTheme.colorScheme.surfaceContainerHigh
        else
            MaterialTheme.colorScheme.primary,
        animationSpec = tween(300),
        label = "NextColor"
    )
    val nextIconColor by animateColorAsState(
        targetValue = if (isLastPage)
            MaterialTheme.colorScheme.onSurfaceVariant
        else
            MaterialTheme.colorScheme.onPrimary,
        animationSpec = tween(300),
        label = "NextIconColor"
    )

    // ── Page transition: fade illustration on page change ─────────────────────
    val illustrationAlpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(400),
        label = "IllustrationAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(Modifier.height(56.dp))

            // ── Title ─────────────────────────────────────────────────────────
            Text(
                text = page.title,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
            )

            Spacer(Modifier.height(12.dp))

            // ── Subtitle ──────────────────────────────────────────────────────
            Text(
                text = page.subtitle,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 24.sp
            )

            Spacer(Modifier.weight(1f))

            // ── Illustration ──────────────────────────────────────────────────
            IllustrationCluster(
                page = page,
                alpha = illustrationAlpha,
                modifier = Modifier.size(300.dp)
            )

            Spacer(Modifier.weight(1f))

            // ── Action button (notifications page only) ───────────────────────
            page.actionLabel?.let { label ->
                Button(
                    onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            // Pre-13: no runtime permission needed, just advance
                            currentPage++
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text(label, style = MaterialTheme.typography.labelLarge)
                }
                Spacer(Modifier.height(12.dp))
            }

            // ── Bottom spacer — keeps content clear of the floating bottom strip
            Spacer(Modifier.height(100.dp))
        }

        // ── Bottom strip ──────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, MaterialTheme.colorScheme.background)
                    )
                )
                .navigationBarsPadding()
                .padding(horizontal = 28.dp, vertical = 20.dp)
        ) {
            // Step counter
            Text(
                text = "Step ${currentPage + 1} of ${pages.size}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterStart)
            )

            // Morphing next / finish button
            // On the Notifications page (actionLabel != null) the arrow is hidden —
            // the "Enable Notifications" button is the only way to advance.
            if (page.actionLabel == null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(64.dp)
                        .scale(nextScale)
                        .background(
                            color = nextColor,
                            shape = RoundedCornerShape(nextCorner)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = {
                            if (isLastPage) {
                                scope.launch {
                                    SettingsManager(context).setOnboardingDone(true)
                                    onFinish()
                                }
                            } else {
                                currentPage++
                            }
                        },
                        interactionSource = nextInteraction,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = if (isLastPage) Icons.Filled.Close else Icons.Filled.ArrowForward,
                            contentDescription = if (isLastPage) "Finish" else "Next",
                            tint = nextIconColor,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }
        }
    }
}

// ── Floating illustration cluster ─────────────────────────────────────────────

@Composable
private fun IllustrationCluster(
    page: OnboardingPage,
    alpha: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // ── Main rotated card ─────────────────────────────────────────────────
        Card(
            modifier = Modifier
                .size(180.dp)
                .rotate(-12f),
            shape = RoundedCornerShape(40.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = page.centerIcon,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                )
            }
        }

        // ── Top-left: circle ──────────────────────────────────────────────────
        SatelliteIcon(
            icon = page.topLeftIcon,
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            iconTint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            size = 60.dp,
            modifier = Modifier.offset(x = (-80).dp, y = (-80).dp)
        )

        // ── Top-right: rounded rect ───────────────────────────────────────────
        SatelliteIcon(
            icon = page.topRightIcon,
            shape = RoundedCornerShape(20.dp),
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            iconTint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
            size = 72.dp,
            modifier = Modifier
                .offset(x = 85.dp, y = (-65).dp)
                .rotate(8f)
        )

        // ── Bottom-left: badge/squircle ───────────────────────────────────────
        SatelliteIcon(
            icon = page.bottomLeftIcon,
            shape = RoundedCornerShape(16.dp),
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            iconTint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
            size = 52.dp,
            modifier = Modifier
                .offset(x = (-95).dp, y = 75.dp)
                .rotate(-6f)
        )

        // ── Bottom-right: circle (accent colored) ─────────────────────────────
        SatelliteIcon(
            icon = page.bottomRightIcon,
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            iconTint = MaterialTheme.colorScheme.onPrimaryContainer,
            size = 60.dp,
            modifier = Modifier.offset(x = 90.dp, y = 80.dp)
        )
    }
}

@Composable
private fun SatelliteIcon(
    icon: ImageVector,
    shape: androidx.compose.ui.graphics.Shape,
    containerColor: Color,
    iconTint: Color,
    size: Dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .background(color = containerColor, shape = shape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(size * 0.48f)
        )
    }
}
