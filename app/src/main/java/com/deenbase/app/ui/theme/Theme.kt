package com.deenbase.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    // 30% blue as the action color
    primary                = DarkSecondary,
    onPrimary              = Color.White,
    primaryContainer       = Color(0xFF0D3F5C),
    onPrimaryContainer     = DarkAccent,

    secondary              = DarkSecondary,
    onSecondary            = Color.White,
    secondaryContainer     = Color(0xFF0A2D42),
    onSecondaryContainer   = DarkAccent,

    tertiary               = DarkSecondary,
    onTertiary             = Color.White,
    tertiaryContainer      = Color(0xFF0D3F5C),
    onTertiaryContainer    = DarkAccent,

    // 60% deep navy as all surfaces
    background             = DarkBackground,
    onBackground           = DarkAccent,

    surface                = DarkSurface,
    onSurface              = DarkAccent,
    onSurfaceVariant       = Color(0xFF7A9BAD),

    surfaceContainer       = DarkSurfaceContainer,
    surfaceContainerLow    = DarkSurfaceContainerLow,
    surfaceContainerHigh   = DarkSurfaceContainerHigh,
    surfaceContainerHighest= DarkSurfaceContainerHighest,

    outline                = DarkOutline,
    outlineVariant         = DarkOutlineVariant,

    error                  = Color(0xFFCF6679),
    onError                = Color.White,
)

private val LightColorScheme = lightColorScheme(
    // 30% blue as the action color
    primary                = LightSecondary,
    onPrimary              = Color.White,
    primaryContainer       = Color(0xFFB3E4FF),
    onPrimaryContainer     = LightAccent,

    secondary              = LightSecondary,
    onSecondary            = Color.White,
    secondaryContainer     = Color(0xFFCCEAFA),
    onSecondaryContainer   = LightAccent,

    tertiary               = LightSecondary,
    onTertiary             = Color.White,
    tertiaryContainer      = Color(0xFFB3E4FF),
    onTertiaryContainer    = LightAccent,

    // 60% light blue-gray as all surfaces
    background             = LightBackground,
    onBackground           = LightAccent,

    surface                = LightSurface,
    onSurface              = LightAccent,
    onSurfaceVariant       = Color(0xFF3D5166),

    surfaceContainer       = LightSurfaceContainer,
    surfaceContainerLow    = LightSurfaceContainerLow,
    surfaceContainerHigh   = LightSurfaceContainerHigh,
    surfaceContainerHighest= LightSurfaceContainerHighest,

    outline                = LightOutline,
    outlineVariant         = LightOutlineVariant,

    error                  = Color(0xFFB3261E),
    onError                = Color.White,
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DeenBaseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialExpressiveTheme(
        colorScheme = colorScheme,
        typography = Typography,
        motionScheme = MotionScheme.expressive(),
        content = content
    )
}
