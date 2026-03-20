package com.deenbase.app.features.settings.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.Image
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Gavel
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.toPath
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import com.deenbase.app.ui.springOverscroll
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deenbase.app.BuildConfig
import com.deenbase.app.R
import kotlin.math.max
private class PolygonShape(private val polygon: RoundedPolygon) : Shape {
    private val matrix = Matrix()
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val path = polygon.toPath().asComposePath()
        matrix.reset()
        val bounds = polygon.calculateBounds()
        val maxDim = max(bounds[2] - bounds[0], bounds[3] - bounds[1])
        matrix.scale(size.width / maxDim, size.height / maxDim)
        matrix.translate(-bounds[0], -bounds[1])
        path.transform(matrix)
        return Outline.Generic(path)
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AboutScreen(
    onNavigateBack: () -> Unit
) {
    val uriHandler = LocalUriHandler.current
    val cookieShape = remember { PolygonShape(MaterialShapes.Cookie7Sided) }

    val gradientBrush = Brush.verticalGradient(
        colors = listOf(MaterialTheme.colorScheme.background, Color.Transparent)
    )

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    val appAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 400, delayMillis = 80),
        label = "appAlpha"
    )
    val appScale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.96f,
        animationSpec = tween(durationMillis = 400, delayMillis = 80),
        label = "appScale"
    )
    val devAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 400, delayMillis = 180),
        label = "devAlpha"
    )
    val devScale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.96f,
        animationSpec = tween(durationMillis = 400, delayMillis = 180),
        label = "devScale"
    )
    val linksAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 400, delayMillis = 280),
        label = "linksAlpha"
    )
    val linksScale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.96f,
        animationSpec = tween(durationMillis = 400, delayMillis = 280),
        label = "linksScale"
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "About",
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
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().springOverscroll()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Spacer(modifier = Modifier.height(innerPadding.calculateTopPadding() + 8.dp))

                // ── APP CARD ──────────────────────────────────────────────────
                Box(
                    modifier = Modifier
                        .alpha(appAlpha)
                        .scale(appScale)
                        .fillMaxWidth()
                        .clip(
                            RoundedCornerShape(
                                topStart = 20.dp, topEnd = 20.dp,
                                bottomStart = 4.dp, bottomEnd = 4.dp
                            )
                        )
                        .background(MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        // App icon
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(cookieShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_db_logo),
                                contentDescription = null,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "DeenBase",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        // GitHub + Instagram icon buttons
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            FilledTonalIconButton(
                                onClick = { uriHandler.openUri("https://github.com/aftab7xt/DeenBase-App") }
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_github),
                                    contentDescription = "GitHub",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            FilledTonalIconButton(
                                onClick = { uriHandler.openUri("https://instagram.com/deen_base") }
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_instagram),
                                    contentDescription = "Instagram",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                // ── DEVELOPER CARD ────────────────────────────────────────────
                Box(
                    modifier = Modifier
                        .alpha(devAlpha)
                        .scale(devScale)
                        .fillMaxWidth()
                        .clip(
                            RoundedCornerShape(
                                topStart = 4.dp, topEnd = 4.dp,
                                bottomStart = 20.dp, bottomEnd = 20.dp
                            )
                        )
                        .background(MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        // Developer avatar placeholder
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "SA",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                fontSize = 20.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Shaikh Aftab Alli",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Developer",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // ── LEGAL ─────────────────────────────────────────────────────
                Box(modifier = Modifier.alpha(linksAlpha).scale(linksScale)) {
                    SettingsItem(
                        title = "License",
                        subtitle = "GNU General Public License Version 3",
                        leadingIcon = Icons.Filled.Gavel,
                        index = 0,
                        totalItems = 1,
                        onClick = { uriHandler.openUri("https://www.gnu.org/licenses/gpl-3.0.html") }
                    )
                }

                Spacer(modifier = Modifier.height(100.dp))
            }

            // Top gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(innerPadding.calculateTopPadding() + 32.dp)
                    .background(brush = gradientBrush)
                    .align(Alignment.TopCenter)
            )
        }
    }
}


