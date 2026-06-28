package com.pdm0126.outfix.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.drawWithContent
import kotlin.math.roundToInt
import android.os.Build

@Composable
fun CustomSizeSlider(
    sizes: List<String>,
    selectedIndex: Int,
    onIndexChanged: (Int) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val currentSelectedIndex by rememberUpdatedState(selectedIndex)

    var isDragging by remember { mutableStateOf(false) }
    var dragX by remember { mutableStateOf(0f) }
    
    val trackLayer = androidx.compose.ui.graphics.rememberGraphicsLayer()
    val density = LocalDensity.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(top = 24.dp)
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth(0.75f).height(60.dp)) {
            val widthPx = constraints.maxWidth.toFloat()
            val segmentPx = if (sizes.size > 1) widthPx / (sizes.size - 1) else 0f
            
            val targetX = if (isDragging) dragX else (segmentPx * selectedIndex)
            
            val animatedX by animateFloatAsState(
                targetValue = targetX,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMedium
                ),
                label = "sliderHandle"
            )

            val textToDisplay = if (segmentPx > 0) {
                val idx = (animatedX / segmentPx).roundToInt().coerceIn(0, sizes.size - 1)
                sizes[idx]
            } else sizes[selectedIndex]
            
            val thumbHalfWidthPx = with(density) { 24.dp.toPx() }
            val thumbOffsetYPx = with(density) { 24.dp.toPx() }

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .drawWithContent {
                        trackLayer.record {
                            this@drawWithContent.drawContent()
                        }
                        drawLayer(trackLayer)
                    }
            ) {
                Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .align(Alignment.BottomCenter)
                    .pointerInput(segmentPx) {
                        var lastVibratedIndex = -1
                        detectHorizontalDragGestures(
                            onDragStart = { offset ->
                                isDragging = true
                                dragX = offset.x.coerceIn(0f, widthPx)
                                lastVibratedIndex = (dragX / segmentPx).roundToInt()
                            },
                            onDragEnd = {
                                isDragging = false
                            },
                            onDragCancel = {
                                isDragging = false
                            }
                        ) { change, _ ->
                            change.consume()
                            dragX = change.position.x.coerceIn(0f, widthPx)
                            
                            for (i in sizes.indices) {
                                val cx = i * segmentPx
                                if (Math.abs(dragX - cx) < (segmentPx * 0.15f)) {
                                    if (lastVibratedIndex != i) {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        lastVibratedIndex = i
                                    }
                                    break
                                }
                            }

                            val newIndex = (dragX / segmentPx).roundToInt().coerceIn(0, sizes.size - 1)
                            if (newIndex != currentSelectedIndex) {
                                onIndexChanged(newIndex)
                            }
                        }
                    }
                    .pointerInput(segmentPx) {
                        detectTapGestures(
                            onTap = { offset ->
                                val newIndex = (offset.x / segmentPx).roundToInt().coerceIn(0, sizes.size - 1)
                                if (newIndex != currentSelectedIndex) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onIndexChanged(newIndex)
                                }
                            }
                        )
                    }
            ) {
                val centerY = size.height / 2f
                val activeColor = Color(0xFF007AFF)
                val inactiveColor = Color(0xFFE0E0E0)
                
                drawLine(
                    color = activeColor,
                    start = Offset(0f, centerY),
                    end = Offset(animatedX, centerY),
                    strokeWidth = 4.dp.toPx()
                )
                
                drawLine(
                    color = inactiveColor,
                    start = Offset(animatedX, centerY),
                    end = Offset(widthPx, centerY),
                    strokeWidth = 4.dp.toPx()
                )
                
                for (i in sizes.indices) {
                    val cx = i * segmentPx
                    val isActive = cx <= animatedX + 1f
                    
                    drawCircle(
                        color = if (isActive) activeColor else inactiveColor,
                        radius = 5.dp.toPx(),
                        center = Offset(cx, centerY)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 3.dp.toPx(),
                        center = Offset(cx, centerY)
                    )
                }
                
            }
            }

            Box(
                modifier = Modifier
                    .offset { 
                        IntOffset(
                            (animatedX - thumbHalfWidthPx).roundToInt(), 
                            thumbOffsetYPx.roundToInt()
                        ) 
                    }
                    .size(48.dp, 32.dp)
                    .liquidGlass(
                        blur = 0f,
                        saturation = 1.2f,
                        refraction = 0.4f,
                        curve = 0.6f,
                        dispersion = 0.25f,
                        normalizedRadius = 0.5f
                    )
            ) {
                if (Build.VERSION.SDK_INT >= 31) {
                    Canvas(modifier = Modifier.matchParentSize()) {
                        translate(left = -(animatedX - thumbHalfWidthPx), top = -thumbOffsetYPx) {
                            drawLayer(trackLayer)
                        }
                        drawRect(Color.White.copy(alpha = 0.5f))
                    }
                } else {
                    Box(modifier = Modifier.matchParentSize().background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(50)))
                }
            }

            Box(
                modifier = Modifier
                    .offset { IntOffset(animatedX.roundToInt(), 0) }
            ) {
                Text(
                    text = textToDisplay,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.Black,
                    modifier = Modifier.offset(x = (-10).dp)
                )
            }
        }
    }
}
