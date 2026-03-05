package com.deenbase.app.features.tasbih.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.deenbase.app.R
import com.deenbase.app.features.tasbih.viewmodel.SubhanallahViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TARGET = 100
private val CIRCLE_SIZE = 220.dp

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SubhanallahScreen(
    onNavigateBack: () -> Unit,
    viewModel: SubhanallahViewModel = viewModel()
) {
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    val count by viewModel.count.collectAsState()
    val isComplete by viewModel.isComplete.collectAsState()

    var showCompletionDialog by remember { mutableStateOf(false) }
    var hasShownDialog by remember { mutableStateOf(false) }

    // Read arabicFontStyle from settings to match reading screen font
    val arabicFont = remember {
        FontFamily(Font(R.font.indopak_nastaleeq))
    }

    LaunchedEffect(isComplete) {
        if (isComplete && !hasShownDialog) {
            showCompletionDialog = true
            hasShownDialog = true
        }
    }

    // --- TAP BUTTON ---
    val tapInteractionSource = remember { MutableInteractionSource() }
    val isTapPhysicallyPressed by tapInteractionSource.collectIsPressedAsState()
    var forceSquishTap by remember { mutableStateOf(false) }
    val isTapSquished = isTapPhysicallyPressed || forceSquishTap

    val tapScale by animateFloatAsState(
        targetValue = if (isTapSquished) 0.95f else 1.0f,
        animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
        label = "TapScale"
    )
    val tapCorner by animateDpAsState(
        targetValue = if (isTapSquished) 16.dp else 50.dp,
        animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
        label = "TapCorner"
    )

    val animatedProgress by animateFloatAsState(
        targetValue = count / TARGET.toFloat(),
        animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
        label = "Progress"
    )

    // ── Completion dialog ────────────────────────────────────────────────────
    if (showCompletionDialog) {
        AlertDialog(
            onDismissRequest = {},
            shape = RoundedCornerShape(28.dp),
            title = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "🤲", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Your sins are forgiven",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "إِنْ شَاءَ ٱللَّٰهُ",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                }
            },
            text = {
                Text(
                    text = "You've completed 100x Subhanallahi wa bihamdihi today. May Allah accept it from you. Come back tomorrow to do it again.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { showCompletionDialog = false; onNavigateBack() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Come Back Tomorrow", fontWeight = FontWeight.SemiBold)
                    }
                    TextButton(
                        onClick = { showCompletionDialog = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Stay on this page")
                    }
                }
            }
        )
    }

    // ── Main layout ──────────────────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Back button — top-left, properly scoped inside Box
        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(8.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }

        // Main content column
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(top = 48.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {

            // ── Progress ring + count ────────────────────────────────────
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.size(CIRCLE_SIZE),
                    strokeWidth = 6.dp,
                    strokeCap = StrokeCap.Round,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    color = if (isComplete) MaterialTheme.colorScheme.tertiary
                    else MaterialTheme.colorScheme.primary
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = count.toString(),
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 80.sp,
                            lineHeight = 80.sp,
                            fontWeight = FontWeight.Black,
                            fontFeatureSettings = "tnum"
                        ),
                        color = if (isComplete) MaterialTheme.colorScheme.tertiary
                        else MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "/ $TARGET",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // ── Arabic dhikr text ────────────────────────────────────────
            Text(
                text = "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ",
                fontFamily = arabicFont,
                fontSize = 32.sp,
                lineHeight = 48.sp,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            // ── TAP button — pill, same width as circle ──────────────────
            FilledTonalButton(
                onClick = {
                    if (!isComplete) {
                        viewModel.increment()
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        scope.launch {
                            forceSquishTap = true
                            delay(90)
                            forceSquishTap = false
                        }
                    }
                },
                interactionSource = tapInteractionSource,
                shape = RoundedCornerShape(tapCorner),
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = if (isComplete)
                        MaterialTheme.colorScheme.tertiaryContainer
                    else
                        MaterialTheme.colorScheme.primaryContainer,
                    contentColor = if (isComplete)
                        MaterialTheme.colorScheme.onTertiaryContainer
                    else
                        MaterialTheme.colorScheme.onPrimaryContainer
                ),
                modifier = Modifier
                    .width(CIRCLE_SIZE)
                    .height(64.dp)
                    .scale(tapScale)
            ) {
                Text(
                    text = if (isComplete) "✓" else "TAP",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                )
            }

            // ── Hadith — two cards with 2dp gap ──────────────────────────
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Card(
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 4.dp, bottomEnd = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                        Text(
                            text = "Narrated Abu Huraira: Allah's Messenger (ﷺ) said,",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "\"Whoever says, 'Subhan Allah wa bihamdihi,' one hundred times a day, will be forgiven all his sins even if they were as much as the foam of the sea.\"",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontStyle = FontStyle.Italic
                        )
                    }
                }
                Card(
                    shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 20.dp, bottomEnd = 20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Sahih al-Bukhari",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "6405",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
