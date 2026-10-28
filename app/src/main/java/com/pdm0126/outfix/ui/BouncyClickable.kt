package com.pdm0126.outfix.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation

import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.bouncyClickable(
    enabled: Boolean = true,
    onClick: () -> Unit
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    var isPressedInstant by remember { mutableStateOf(false) }
    val hapticFeedback = LocalHapticFeedback.current

    val scale by animateFloatAsState(
        targetValue = if (isPressedInstant && enabled) 1.08f else 1f,
        animationSpec = spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
            stiffness = 400f
        ),
        label = "bouncyClickScale"
    )

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .pointerInput(enabled) {
            awaitEachGesture {
                awaitFirstDown(requireUnconsumed = false)
                if (enabled) {
                    isPressedInstant = true
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    waitForUpOrCancellation()
                    isPressedInstant = false
                }
            }
        }
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            enabled = enabled,
            onClick = onClick
        )
}

/**
 * Expande visual y físicamente el tamaño del elemento simétricamente en todos sus lados
 * respecto a su centro, mientras reporta al contenedor padre las dimensiones en reposo.
 * Esto evita que empuje elementos vecinos en Rows/Columns y conserva coordenadas reales
 * 1:1 para Canvas con shader liquidGlass sin requerir escalado de GPU.
 */
fun Modifier.growCentered(
    extraX: Dp,
    extraY: Dp = extraX
): Modifier = this.layout { measurable, constraints ->
    val extraXPx = (extraX * 2).roundToPx()
    val extraYPx = (extraY * 2).roundToPx()
    val childConstraints = constraints.copy(
        minWidth = 0,
        minHeight = 0
    )
    val placeable = measurable.measure(childConstraints)
    val layoutWidth = (placeable.width - extraXPx).coerceIn(constraints.minWidth, constraints.maxWidth)
    val layoutHeight = (placeable.height - extraYPx).coerceIn(constraints.minHeight, constraints.maxHeight)
    layout(layoutWidth, layoutHeight) {
        placeable.place(-extraXPx / 2, -extraYPx / 2)
    }
}
