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
