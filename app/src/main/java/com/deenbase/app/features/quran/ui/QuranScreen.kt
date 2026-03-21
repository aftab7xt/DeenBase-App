package com.deenbase.app.features.quran.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.deenbase.app.R
import com.deenbase.app.features.quran.viewmodel.QuranViewModel
import com.deenbase.app.ui.springOverscroll

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun QuranScreen(
    viewModel: QuranViewModel = viewModel(),
    onBrowseClick: () -> Unit,
    onSearchClick: () -> Unit,
    onFavouritesClick: () -> Unit,
    onBookmarksClick: () -> Unit
) {
    val favouriteCount by viewModel.favouriteCount.collectAsState()
    val bookmarkCount  by viewModel.bookmarkCount.collectAsState()

    val heroColor  = MaterialTheme.colorScheme.primaryContainer
    val background = MaterialTheme.colorScheme.background

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    val alpha by animateFloatAsState(
        targetValue   = if (visible) 1f else 0f,
        animationSpec = tween(400, delayMillis = 60),
        label         = "alpha"
    )
    val scale by animateFloatAsState(
        targetValue   = if (visible) 1f else 0.97f,
        animationSpec = tween(400, delayMillis = 60),
        label         = "scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .springOverscroll()
                .verticalScroll(rememberScrollState())
                .alpha(alpha)
                .scale(scale)
        ) {
            // ── HERO ─────────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
                    .background(heroColor)
            ) {
                // Bottom gradient fade into background
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, background)
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .padding(horizontal = 24.dp)
                        .padding(top = 24.dp, bottom = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Quran calligraphy image
                    androidx.compose.foundation.Image(
                        painter            = androidx.compose.ui.res.painterResource(id = R.drawable.ic_quran_tab),
                        contentDescription = null,
                        contentScale       = androidx.compose.ui.layout.ContentScale.Fit,
                        colorFilter        = androidx.compose.ui.graphics.ColorFilter.tint(MaterialTheme.colorScheme.onPrimaryContainer),
                        modifier           = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp)
                    )

                    // Browse All Surahs button
                    FilledTonalButton(
                        onClick        = onBrowseClick,
                        shape          = RoundedCornerShape(50.dp),
                        colors         = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.12f),
                            contentColor   = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier       = Modifier.fillMaxWidth().height(52.dp)
                    ) {
                        Text(
                            "Browse All Surahs",
                            style      = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // ── CONTENT BELOW HERO ────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Search bar
                Card(
                    onClick  = onSearchClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(50.dp),
                    colors   = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text  = "Search verses...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Icon(
                            imageVector        = Icons.Filled.Search,
                            contentDescription = null,
                            tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier           = Modifier.size(20.dp)
                        )
                    }
                }

                // Favourites + Bookmarks side by side
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Card(
                        onClick  = onFavouritesClick,
                        modifier = Modifier.weight(1f).height(100.dp),
                        shape    = RoundedCornerShape(
                            topStart = 20.dp, topEnd = 4.dp,
                            bottomStart = 20.dp, bottomEnd = 4.dp
                        ),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        )
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Icon(
                                imageVector        = Icons.Filled.Favorite,
                                contentDescription = null,
                                tint               = MaterialTheme.colorScheme.primary,
                                modifier           = Modifier.size(20.dp)
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text       = "Favourites",
                                    style      = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text  = "$favouriteCount saved",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Card(
                        onClick  = onBookmarksClick,
                        modifier = Modifier.weight(1f).height(100.dp),
                        shape    = RoundedCornerShape(
                            topStart = 4.dp, topEnd = 20.dp,
                            bottomStart = 4.dp, bottomEnd = 20.dp
                        ),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        )
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Icon(
                                imageVector        = Icons.Filled.Bookmark,
                                contentDescription = null,
                                tint               = MaterialTheme.colorScheme.primary,
                                modifier           = Modifier.size(20.dp)
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text       = "Bookmarks",
                                    style      = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text  = "$bookmarkCount saved",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}
