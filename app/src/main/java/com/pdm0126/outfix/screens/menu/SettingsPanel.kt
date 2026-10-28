package com.pdm0126.outfix.screens.menu

import android.os.Build
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BlurOn
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pdm0126.outfix.ui.bouncyClickable
import com.pdm0126.outfix.ui.liquidGlass
import com.pdm0126.outfix.ui.theme.LimeGreen
import kotlin.math.roundToInt

@Composable
fun SettingsPanel(
    currentBlur: Float,
    onBlurChange: (Float) -> Unit,
    onBack: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // ── Header (En armonía con los demás paneles del menú) ────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(40.dp)
                    .bouncyClickable(onClick = onBack)
                    .clip(CircleShape)
                    .background(Color(0xFFBDBDBD)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.ChevronLeft,
                    contentDescription = "Regresar",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            Text(
                text = "Ajustes",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                color = Color.Black,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.4f))

        // ── Contenido ────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            // ── Sección: Apariencia ──────────────────────────────────
            Text(
                text = "Apariencia",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // ── Tarjeta de Liquid Glass Blur ─────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFFF7F7FA))
                    .padding(vertical = 18.dp, horizontal = 16.dp)
            ) {
                Column {
                    // Header de la tarjeta con icono de acento LimeGreen
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF007AFF).copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.BlurOn,
                                contentDescription = null,
                                tint = Color(0xFF007AFF),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Intensidad Liquid Glass",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black
                            )
                            Text(
                                text = "Ajusta el blur del efecto de vidrio",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Slider idéntico a CustomSizeSlider con imán en 0f y 50f (sin mostrar valor)
                    SettingsBlurSlider(
                        currentValue = currentBlur.coerceIn(0f, 50f),
                        onValueChange = onBlurChange
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Etiquetas descriptivas "Suave" e "Intenso"
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Suave",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Gray
                        )
                        Text(
                            text = "Intenso",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Gray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Vista previa del efecto ──────────────────────────────
            Text(
                text = "Vista previa",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(84.dp)
                    .clip(RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (Build.VERSION.SDK_INT >= 31) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .liquidGlass(
                                blur = currentBlur,
                                saturation = 1.4f,
                                refraction = 0.55f,
                                curve = 0.45f,
                                dispersion = 0.12f,
                                cornerRadius = 18.dp
                            )
                    ) {
                        drawRect(
                            brush = Brush.linearGradient(
                                listOf(
                                    LimeGreen.copy(alpha = 0.85f),
                                    Color(0xFF2CB67D),
                                    Color(0xFF2F80ED)
                                )
                            )
                        )
                        drawCircle(
                            color = Color.White.copy(alpha = 0.5f),
                            radius = size.height * 0.4f,
                            center = Offset(size.width * 0.25f, size.height * 0.5f)
                        )
                        drawCircle(
                            color = Color.Black.copy(alpha = 0.25f),
                            radius = size.height * 0.35f,
                            center = Offset(size.width * 0.75f, size.height * 0.5f)
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        LimeGreen.copy(alpha = 0.85f),
                                        Color(0xFF2CB67D),
                                        Color(0xFF2F80ED)
                                    )
                                )
                            )
                    )
                }
                Text(
                    text = "Liquid Glass",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

/**
 * Slider idéntico a CustomSizeSlider con thumb de liquidGlass y efecto imán en 0f y 50f.
 */
@Composable
private fun SettingsBlurSlider(
    currentValue: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current
    val trackLayer = rememberGraphicsLayer()

    var isDragging by remember { mutableStateOf(false) }
    var dragX by remember { mutableStateOf(0f) }

    val maxValue = 50f

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
            .padding(horizontal = 24.dp)
    ) {
        val widthPx = constraints.maxWidth.toFloat()
        val currentFraction = (currentValue / maxValue).coerceIn(0f, 1f)
        val targetX = if (isDragging) dragX else (currentFraction * widthPx)

        val animatedX by animateFloatAsState(
            targetValue = targetX,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMedium
            ),
            label = "settingsSliderHandle"
        )

        val thumbHalfWidthPx = with(density) { 24.dp.toPx() }
        val thumbOffsetYPx = with(density) { 24.dp.toPx() }
        val snapMarginPx = with(density) { 22.dp.toPx() }

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
                    .pointerInput(widthPx) {
                        var lastVibratedZone = -1
                        detectHorizontalDragGestures(
                            onDragStart = { offset ->
                                isDragging = true
                                val x = offset.x.coerceIn(0f, widthPx)
                                when {
                                    x <= snapMarginPx -> {
                                        dragX = 0f
                                        lastVibratedZone = 0
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        onValueChange(0f)
                                    }
                                    x >= widthPx - snapMarginPx -> {
                                        dragX = widthPx
                                        lastVibratedZone = 50
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        onValueChange(maxValue)
                                    }
                                    else -> {
                                        dragX = x
                                        lastVibratedZone = -1
                                        val v = (x / widthPx) * maxValue
                                        onValueChange(v)
                                    }
                                }
                            },
                            onDragEnd = {
                                isDragging = false
                            },
                            onDragCancel = {
                                isDragging = false
                            }
                        ) { change, _ ->
                            change.consume()
                            val x = change.position.x.coerceIn(0f, widthPx)
                            when {
                                x <= snapMarginPx -> {
                                    dragX = 0f
                                    if (lastVibratedZone != 0) {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        lastVibratedZone = 0
                                    }
                                    onValueChange(0f)
                                }
                                x >= widthPx - snapMarginPx -> {
                                    dragX = widthPx
                                    if (lastVibratedZone != 50) {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        lastVibratedZone = 50
                                    }
                                    onValueChange(maxValue)
                                }
                                else -> {
                                    dragX = x
                                    lastVibratedZone = -1
                                    val v = (x / widthPx) * maxValue
                                    onValueChange(v)
                                }
                            }
                        }
                    }
                    .pointerInput(widthPx) {
                        detectTapGestures(
                            onTap = { offset ->
                                val x = offset.x.coerceIn(0f, widthPx)
                                when {
                                    x <= snapMarginPx -> {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        onValueChange(0f)
                                    }
                                    x >= widthPx - snapMarginPx -> {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        onValueChange(maxValue)
                                    }
                                    else -> {
                                        val v = (x / widthPx) * maxValue
                                        onValueChange(v)
                                    }
                                }
                            }
                        )
                    }
            ) {
                val centerY = size.height / 2f
                val activeColor = Color(0xFF007AFF)
                val inactiveColor = Color(0xFFE0E0E0)

                // Línea activa
                drawLine(
                    color = activeColor,
                    start = Offset(0f, centerY),
                    end = Offset(animatedX, centerY),
                    strokeWidth = 4.dp.toPx()
                )

                // Línea inactiva
                drawLine(
                    color = inactiveColor,
                    start = Offset(animatedX, centerY),
                    end = Offset(widthPx, centerY),
                    strokeWidth = 4.dp.toPx()
                )

                // Puntos imán en 0f y 50f
                listOf(0f, widthPx).forEach { cx ->
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

        // Thumb de Liquid Glass idéntico a CustomSizeSlider
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
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(50))
                )
            }
        }
    }
}
