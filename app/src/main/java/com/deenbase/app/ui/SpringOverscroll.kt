package com.deenbase.app.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.sign

class SpringOverscrollConnection(private val scope: CoroutineScope) : NestedScrollConnection {

    val overscrollY = Animatable(0f)
    private var animJob: Job? = null

    private fun snapTo(target: Float) {
        animJob?.cancel()
        animJob = scope.launch {
            try {
                overscrollY.snapTo(target)
            } catch (_: Exception) {}
        }
    }

    private fun springBackTo(target: Float) {
        animJob?.cancel()
        animJob = scope.launch {
            try {
                overscrollY.animateTo(
                    target,
                    spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness    = Spring.StiffnessMedium
                    )
                )
            } catch (_: Exception) {}
        }
    }

    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
        val current = overscrollY.value
        if (abs(current) > 0.5f && source == NestedScrollSource.UserInput) {
            val isSameDirection = sign(available.y) == sign(current)
            if (!isSameDirection) {
                val next = current + available.y
                return if (sign(next) != sign(current)) {
                    val consumed = -current
                    snapTo(0f)
                    Offset(0f, consumed)
                } else {
                    snapTo(next)
                    Offset(0f, available.y)
                }
            }
        }
        return Offset.Zero
    }

    override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource
    ): Offset {
        if (source == NestedScrollSource.UserInput && abs(available.y) > 0f) {
            val resistance = 0.38f
            val target = (overscrollY.value + available.y * resistance).coerceIn(-260f, 260f)
            snapTo(target)
        }
        return Offset.Zero
    }

    override suspend fun onPreFling(available: Velocity): Velocity = Velocity.Zero

    override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
        springBackTo(0f)
        return Velocity.Zero
    }
}

@Composable
fun Modifier.springOverscroll(): Modifier {
    val scope = rememberCoroutineScope()
    val connection = remember(scope) { SpringOverscrollConnection(scope) }
    return this
        .nestedScroll(connection)
        .graphicsLayer { translationY = connection.overscrollY.value }
}
