package com.deenbase.app.features.tasbih.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deenbase.app.features.tasbih.viewmodel.TasbihViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TasbihScreen(viewModel: TasbihViewModel) {
    val haptic = LocalHapticFeedback.current
    val count = viewModel.count.value
    val scope = rememberCoroutineScope()

    // --- TAP BUTTON STATE ---
    val tapInteractionSource = remember { MutableInteractionSource() }
    val isTapPhysicallyPressed by tapInteractionSource.collectIsPressedAsState()
    var forceSquishTap by remember { mutableStateOf(false) }
    val isTapSquished = isTapPhysicallyPressed || forceSquishTap

    val tapScale by animateFloatAsState(
        targetValue = if (isTapSquished) 0.95f else 1.0f,
        animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
        label = "TapButtonScale"
    )

    val tapCornerRadius by animateDpAsState(
        targetValue = if (isTapSquished) 24.dp else 75.dp,
        animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
        label = "TapButtonShape"
    )

    // --- RESET BUTTON STATE ---
    val resetInteractionSource = remember { MutableInteractionSource() }
    val isResetPhysicallyPressed by resetInteractionSource.collectIsPressedAsState()
    var forceSquishReset by remember { mutableStateOf(false) }
    val isResetSquished = isResetPhysicallyPressed || forceSquishReset

    val resetScale by animateFloatAsState(
        targetValue = if (isResetSquished) 0.95f else 1.0f,
        animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
        label = "ResetButtonScale"
    )

    val resetCornerRadius by animateDpAsState(
        // CHANGED: 24.dp is exactly half of the 48.dp height, making a perfect pill without rendering glitches.
        targetValue = if (isResetSquished) 12.dp else 24.dp,
        animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
        label = "ResetButtonShape"
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 160.sp, 
                    lineHeight = 160.sp,
                    fontWeight = FontWeight.Black,
                    fontFeatureSettings = "tnum" 
                ),
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(50.dp))

            // THE TAP BUTTON
            LargeFloatingActionButton(
                onClick = {
                    viewModel.increment()
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    
                    scope.launch {
                        forceSquishTap = true
                        delay(90)
                        forceSquishTap = false
                    }
                },
                interactionSource = tapInteractionSource,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier
                    .size(150.dp) 
                    .scale(tapScale), 
                shape = RoundedCornerShape(tapCornerRadius)
            ) {
                Text(
                    "TAP", 
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // THE RESET BUTTON
            FilledTonalButton(
                onClick = { 
                    viewModel.reset()
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    
                    scope.launch {
                        forceSquishReset = true
                        delay(90)
                        forceSquishReset = false
                    }
                },
                interactionSource = resetInteractionSource,
                shape = RoundedCornerShape(resetCornerRadius),
                contentPadding = PaddingValues(0.dp), // CHANGED: Removes internal shifting padding
                modifier = Modifier
                    .width(120.dp) // CHANGED: Fixed width
                    .height(48.dp) // CHANGED: Fixed height
                    .scale(resetScale)
            ) {
                Text(
                    "RESET", 
                    style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 1.sp)
                    // CHANGED: Removed the padding modifier on the text since the button size is locked
                )
            }
            
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}
