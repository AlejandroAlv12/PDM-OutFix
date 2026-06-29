package com.pdm0126.outfix.screens.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowInsetsControllerCompat
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
fun Modifier.innerShadow(
    color: Color = Color.Black.copy(alpha = 0.4f),
    blur: Dp = 4.dp,
    offsetX: Dp = 0.dp,
    offsetY: Dp = 4.dp,
    cornerRadius: Dp = 6.dp
) = this.drawWithCache {
    val blurPx = blur.toPx()
    val offsetXpx = offsetX.toPx()
    val offsetYpx = offsetY.toPx()
    val radiusPx = cornerRadius.toPx()
    val drawWidth = size.width
    val drawHeight = size.height

    val paint = Paint().apply {
        this.color = color
        isAntiAlias = true
        style = PaintingStyle.Fill
        asFrameworkPaint().apply {
            if (blurPx > 0) {
                maskFilter = android.graphics.BlurMaskFilter(blurPx, android.graphics.BlurMaskFilter.Blur.NORMAL)
            }
        }
    }

    val largeRectPath = Path().apply {
        addRect(Rect(-drawWidth, -drawHeight, drawWidth * 2f, drawHeight * 2f))
    }

    val shiftedHolePath = Path().apply {
        addRoundRect(
            RoundRect(
                left = offsetXpx,
                top = offsetYpx,
                right = drawWidth + offsetXpx,
                bottom = drawHeight + offsetYpx,
                cornerRadius = CornerRadius(radiusPx, radiusPx)
            )
        )
    }

    val shadowPath = Path().apply {
        op(largeRectPath, shiftedHolePath, PathOperation.Difference)
    }

    onDrawWithContent {
        drawContent()
        drawIntoCanvas { canvas ->
            canvas.drawPath(shadowPath, paint)
        }
    }
}

@Composable
fun ClosetDoorsOverlay(onFinished: () -> Unit) {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val screenWidthDp = configuration.screenWidthDp.dp
    val screenHeightDp = configuration.screenHeightDp.dp
    val view = LocalView.current
    val hapticFeedback = LocalHapticFeedback.current

    DisposableEffect(Unit) {
        val window = (view.context as? android.app.Activity)?.window
        if (window != null) {
            val controller = WindowInsetsControllerCompat(window, view)
            controller.isAppearanceLightStatusBars = false
            controller.isAppearanceLightNavigationBars = false
        }
        onDispose {
            val window2 = (view.context as? android.app.Activity)?.window
            if (window2 != null) {
                val controller = WindowInsetsControllerCompat(window2, view)
                controller.isAppearanceLightStatusBars = true
                controller.isAppearanceLightNavigationBars = false
            }
        }
    }

    var isOpening by remember { mutableStateOf(false) }
    var isShaking by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val infiniteShake = rememberInfiniteTransition(label = "vibrate")
    val doorShakeX by infiniteShake.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(55, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "doorShakeX"
    )
    
    val doorShakeY by infiniteShake.animateFloat(
        initialValue = -1.5f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(75, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "doorShakeY"
    )

    val shakeX by animateFloatAsState(
        targetValue = if (isShaking) doorShakeX else 0f,
        animationSpec = tween(35),
        label = "shakeX"
    )
    
    val shakeY by animateFloatAsState(
        targetValue = if (isShaking) doorShakeY * 0.35f else 0f,
        animationSpec = tween(35),
        label = "shakeY"
    )

    val leftDoorOffset by animateFloatAsState(
        targetValue = if (isOpening) -screenWidthPx / 2f else 0f,
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "LeftDoor"
    )
    
    val rightDoorOffset by animateFloatAsState(
        targetValue = if (isOpening) screenWidthPx / 2f else 0f,
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "RightDoor"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    
    val pulseAlpha by animateFloatAsState(
        targetValue = if (isOpening) 0f else 1f,
        animationSpec = tween(300),
        label = "pulseAlpha"
    )

    LaunchedEffect(leftDoorOffset) {
        if (isOpening && leftDoorOffset <= -screenWidthPx / 2f + 10f) {
            delay(100)
            onFinished()
        }
    }

    val extraWidthDp = 16.dp
    val extraWidthPx = with(density) { extraWidthDp.toPx() }
    val doorWidth = (configuration.screenWidthDp / 2).dp + extraWidthDp

    val drawDoors: @Composable BoxScope.() -> Unit = {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(doorWidth)
                .offset { IntOffset((leftDoorOffset + shakeX - extraWidthPx).roundToInt(), shakeY.roundToInt()) }
                .background(Color(0xFF8B6C57))
                .drawBehind {
                    val borderWidth = 2.5.dp.toPx()
                    drawRect(
                        color = Color.Black.copy(alpha = 0.40f),
                        topLeft = Offset(size.width - borderWidth, 0f),
                        size = Size(borderWidth, size.height)
                    )
                }
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp)
                    .width(12.dp)
                    .height(60.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFD2B48C))
                    .innerShadow(
                        color = Color.Black.copy(alpha = 0.4f),
                        blur = 4.dp,
                        offsetX = 0.dp,
                        offsetY = 4.dp,
                        cornerRadius = 6.dp
                    )
            )

            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(id = com.pdm0126.outfix.R.drawable.outfix_logo_engraved),
                contentDescription = "OutFix Logo",
                contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 40.dp)
                    .height(30.dp)
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .width(doorWidth)
                .offset { IntOffset((rightDoorOffset + shakeX + extraWidthPx).roundToInt(), shakeY.roundToInt()) }
                .background(Color(0xFF8B6C57))
                .drawBehind {
                    val borderWidth = 2.5.dp.toPx()
                    drawRect(
                        color = Color.Black.copy(alpha = 0.40f),
                        topLeft = Offset(0f, 0f),
                        size = Size(borderWidth, size.height)
                    )
                }
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 16.dp)
                    .width(12.dp)
                    .height(60.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFD2B48C))
                    .innerShadow(
                        color = Color.Black.copy(alpha = 0.4f),
                        blur = 4.dp,
                        offsetX = 0.dp,
                        offsetY = 4.dp,
                        cornerRadius = 6.dp
                    )
            )
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                if (!isOpening && !isShaking) {
                    coroutineScope.launch {
                        isShaking = true
                        repeat(6) {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            delay(55)
                        }
                        isShaking = false
                        delay(60)
                        isOpening = true
                    }
                }
            }
    ) {
        val parentWidth = maxWidth
        val parentHeight = maxHeight

        drawDoors()

        if (pulseAlpha > 0f) {
            val iconSize = 60.dp
            val bottomPad = 120.dp

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = bottomPad)
                    .size(iconSize)
                    .graphicsLayer {
                        scaleX = pulseScale
                        scaleY = pulseScale
                        alpha = pulseAlpha
                    }
            ) {
                Icon(
                    imageVector = Icons.Rounded.TouchApp,
                    contentDescription = "Tap to open",
                    tint = Color(0xFFC7BAB0),
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
