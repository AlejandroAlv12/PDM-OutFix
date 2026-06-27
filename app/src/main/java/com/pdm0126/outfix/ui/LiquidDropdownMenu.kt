package com.pdm0126.outfix.ui

import android.os.Build
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

@Composable
fun LiquidDropdownButton(
    selectedItem: String,
    onClick: () -> Unit,
    onGloballyPositioned: (LayoutCoordinates) -> Unit,
    modifier: Modifier = Modifier,
    alpha: Float = 1f
) {
    Box(
        modifier = modifier
            .graphicsLayer { this.alpha = alpha }
            .clip(RoundedCornerShape(50))
            .background(Color.Black)
            .onGloballyPositioned { onGloballyPositioned(it) }
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = selectedItem, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.width(8.dp))
            androidx.compose.foundation.Canvas(modifier = Modifier.size(width = 12.dp, height = 7.dp)) {
                val strokeWidth = 2.dp.toPx()
                val startX = 1.dp.toPx()
                val centerX = 6.dp.toPx()
                val endX = 11.dp.toPx()
                val topY = 1.dp.toPx()
                val bottomY = 6.dp.toPx()
                drawLine(
                    color = Color.White,
                    start = Offset(startX, topY),
                    end = Offset(centerX, bottomY),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = Color.White,
                    start = Offset(endX, topY),
                    end = Offset(centerX, bottomY),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

@Composable
fun LiquidDropdownOverlay(
    isExpanded: Boolean,
    onDismissRequest: () -> Unit,
    buttonCoords: LayoutCoordinates?,
    targetWidth: Dp,
    backgroundLayer: GraphicsLayer,
    screenCoords: LayoutCoordinates?,
    items: List<String>,
    selectedItem: String,
    onItemSelected: (String) -> Unit
) {
    val menuProgress by animateFloatAsState(
        targetValue = if (isExpanded) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f),
        label = "menuProgress"
    )

    val showOverlay = isExpanded || (menuProgress > 0f)
    if (!showOverlay || buttonCoords?.isAttached != true) return

    if (isExpanded) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismissRequest
                )
        )
    }

    val density = LocalDensity.current
    val buttonWidth = with(density) { buttonCoords.size.width.toDp() }
    val buttonHeight = with(density) { buttonCoords.size.height.toDp() }
    val yOffset = with(density) { buttonCoords.positionInRoot().y.toDp() }
    val startXOffset = with(density) { buttonCoords.positionInRoot().x.toDp() }

    val currentWidth = androidx.compose.ui.unit.lerp(buttonWidth, targetWidth, menuProgress)
    val currentX = androidx.compose.ui.unit.lerp(startXOffset, startXOffset - (targetWidth - buttonWidth) / 2, menuProgress)
    val cornerRadius = androidx.compose.ui.unit.lerp(buttonHeight / 2, 24.dp, menuProgress)

    var isMenuPressed by remember { mutableStateOf(false) }
    val menuScale by animateFloatAsState(
        targetValue = if (isMenuPressed) 0.95f else 1f,
        animationSpec = spring(stiffness = 400f),
        label = "menuScale"
    )

    Box(
        modifier = Modifier
            .absoluteOffset(x = currentX, y = yOffset)
            .graphicsLayer {
                scaleX = menuScale
                scaleY = menuScale
                transformOrigin = TransformOrigin(0.5f, 0f)
            }
            .clip(RoundedCornerShape(cornerRadius))
            .width(currentWidth)
            .layout { measurable, constraints ->
                val placeable = measurable.measure(constraints)
                val startHeightPx = buttonHeight.roundToPx()
                val currentHeightPx = androidx.compose.ui.util.lerp(
                    startHeightPx.toFloat(),
                    placeable.height.toFloat(),
                    menuProgress
                ).roundToInt()
                layout(placeable.width, currentHeightPx) {
                    placeable.placeRelative(0, 0)
                }
            }
    ) {
        if (Build.VERSION.SDK_INT >= 31) {
            androidx.compose.foundation.Canvas(
                modifier = Modifier
                    .matchParentSize()
                    .liquidGlass(
                        blur = 30f,
                        saturation = 1.4f,
                        refraction = 0.5f,
                        curve = 0.05f,
                        dispersion = 0.25f,
                        normalizedRadius = 0.15f,
                        cornerRadius = cornerRadius
                    )
            ) {
                scale(scaleX = 1f / menuScale, scaleY = 1f / menuScale, pivot = Offset(size.width / 2f, 0f)) {
                    if (screenCoords?.isAttached == true) {
                        val screenPos = screenCoords!!.positionInRoot()
                        val unscaledX = currentX.toPx()
                        val unscaledY = yOffset.toPx()
                        translate(left = -(unscaledX - screenPos.x), top = -(unscaledY - screenPos.y)) {
                            drawLayer(backgroundLayer)
                        }
                    }
                }
            }
        } else {
            Box(modifier = Modifier.matchParentSize().background(Color.White))
        }

        val overlayColor = Color.Black.copy(alpha = androidx.compose.ui.util.lerp(0f, 0.4f, menuProgress))
        val baseColor = Color.Black.copy(alpha = 1f - menuProgress)
        Box(modifier = Modifier.matchParentSize().background(baseColor).background(overlayColor))

        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(buttonHeight)
                    .pointerInput(Unit) {
                        awaitEachGesture {
                            awaitFirstDown(requireUnconsumed = false)
                            isMenuPressed = true
                            waitForUpOrCancellation()
                            isMenuPressed = false
                        }
                    }
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDismissRequest
                    )
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val textColor = Color.White
                Text(
                    text = selectedItem,
                    color = textColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.width(8.dp))
                androidx.compose.foundation.Canvas(modifier = Modifier.size(width = 12.dp, height = 7.dp)) {
                    val strokeWidth = 2.dp.toPx()
                    val startX = 1.dp.toPx()
                    val centerX = 6.dp.toPx()
                    val endX = 11.dp.toPx()
                    val topY = 1.dp.toPx()
                    val bottomY = 6.dp.toPx()
                    val outerY = androidx.compose.ui.util.lerp(topY, bottomY, menuProgress)
                    val innerY = androidx.compose.ui.util.lerp(bottomY, topY, menuProgress)
                    drawLine(color = textColor, start = Offset(startX, outerY), end = Offset(centerX, innerY), strokeWidth = strokeWidth, cap = StrokeCap.Round)
                    drawLine(color = textColor, start = Offset(endX, outerY), end = Offset(centerX, innerY), strokeWidth = strokeWidth, cap = StrokeCap.Round)
                }
            }

            HorizontalDivider(color = Color.White.copy(alpha = 0.1f * menuProgress))
            
            val categoriesLayer = rememberGraphicsLayer()
            Box(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                        .drawWithContent {
                            categoriesLayer.record {
                                this@drawWithContent.drawContent()
                            }
                        }
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 8.dp)
                ) {
                    val sortedItems = remember(items) { items.sorted() }
                    sortedItems.forEach { item ->
                        Text(
                            text = item,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onItemSelected(item) }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            color = Color.White.copy(alpha = menuProgress),
                            fontWeight = if (item == selectedItem) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                if (Build.VERSION.SDK_INT >= 31) {
                    VerticalEdgesProgressiveBlurLayer(
                        modifier = Modifier
                            .matchParentSize()
                            .clipToBounds(),
                        contentLayer = categoriesLayer,
                        maxBlur = 20f,
                        edgeHeightFraction = 0.08f
                    )
                }
            }
        }
    }
}
