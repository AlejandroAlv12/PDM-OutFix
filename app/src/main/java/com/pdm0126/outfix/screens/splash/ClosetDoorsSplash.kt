package com.pdm0126.outfix.screens.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun ClosetDoorsOverlay(
    onFinished: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }

    var isOpening by remember { mutableStateOf(false) }

    val leftDoorOffset by animateFloatAsState(
        targetValue = if (isOpening) -screenWidthPx / 2f else 0f,
        animationSpec = tween(
            durationMillis = 800,
            easing = FastOutSlowInEasing
        ),
        label = "LeftDoor"
    )
    
    val rightDoorOffset by animateFloatAsState(
        targetValue = if (isOpening) screenWidthPx / 2f else 0f,
        animationSpec = tween(
            durationMillis = 800,
            easing = FastOutSlowInEasing
        ),
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

    Box(
        modifier = Modifier
            .fillMaxSize()

            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                if (!isOpening) isOpening = true
            }
    ) {

        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.5f)
                .offset { IntOffset(leftDoorOffset.roundToInt(), 0) }
                .background(Color(0xFF8B6C57))
                .border(
                    width = 1.dp,
                    color = Color.Black.copy(alpha = 0.3f)
                )
        ) {

            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp)
                    .width(12.dp)
                    .height(60.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFD2B48C))
            )
        }
        

        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .fillMaxWidth(0.5f)
                .offset { IntOffset(rightDoorOffset.roundToInt(), 0) }
                .background(Color(0xFF8B6C57))
                .border(
                    width = 1.dp,
                    color = Color.Black.copy(alpha = 0.3f)
                )
        ) {

            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 16.dp)
                    .width(12.dp)
                    .height(60.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFD2B48C))
            )
        }
        

        if (pulseAlpha > 0f) {
            Icon(
                imageVector = Icons.Rounded.TouchApp,
                contentDescription = "Tap to open",
                tint = Color.White.copy(alpha = 0.8f),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 120.dp)
                    .size(60.dp)
                    .graphicsLayer {
                        scaleX = pulseScale
                        scaleY = pulseScale
                        alpha = pulseAlpha
                    }
            )
        }
    }
}
