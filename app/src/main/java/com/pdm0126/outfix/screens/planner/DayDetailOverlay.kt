package com.pdm0126.outfix.screens.planner

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.pdm0126.outfix.data.model.DayInfo
import com.pdm0126.outfix.ui.CharacterWithClothes
import com.pdm0126.outfix.ui.OutFixScreen
import com.pdm0126.outfix.ui.bouncyClickable

@Composable
fun DayDetailOverlay(
    isActive: Boolean,
    dayInfo: DayInfo?,
    sourceBounds: androidx.compose.ui.geometry.Rect?,
    textBounds: androidx.compose.ui.geometry.Rect? = null,
    charBounds: androidx.compose.ui.geometry.Rect? = null,
    appBackgroundLayer: androidx.compose.ui.graphics.layer.GraphicsLayer? = null,
    onDismiss: () -> Unit,
    onEditDay: (String) -> Unit
) {
    AnimatedVisibility(
        visible = isActive,
        enter = fadeIn(animationSpec = tween(durationMillis = 1)),
        exit = fadeOut(animationSpec = tween(durationMillis = 1, delayMillis = 350))
    ) {
        val activeDayInfo = dayInfo ?: return@AnimatedVisibility

        androidx.activity.compose.BackHandler(enabled = isActive) {
            onDismiss()
        }

        val transition = this.transition

        val configuration = androidx.compose.ui.platform.LocalConfiguration.current
        val density = androidx.compose.ui.platform.LocalDensity.current

        val startW = with(density) { (sourceBounds?.width ?: 120f).toDp() }
        val startH = with(density) { (sourceBounds?.height ?: 300f).toDp() }
        val startX = with(density) { (sourceBounds?.left ?: 0f).toDp() }
        val startY = with(density) { (sourceBounds?.top ?: 0f).toDp() }

        val finalW = configuration.screenWidthDp.dp * 0.9f
        val finalH = configuration.screenHeightDp.dp * 0.85f
        val finalX = (configuration.screenWidthDp.dp - finalW) / 2f
        val finalY = (configuration.screenHeightDp.dp - finalH) / 2f

        val transitionSpec = { tween<androidx.compose.ui.unit.Dp>(durationMillis = 350, easing = FastOutSlowInEasing) }

        val x by transition.animateDp(transitionSpec = { transitionSpec() }, label = "x") {
            if (it == androidx.compose.animation.EnterExitState.Visible) finalX else startX
        }
        val y by transition.animateDp(transitionSpec = { transitionSpec() }, label = "y") {
            if (it == androidx.compose.animation.EnterExitState.Visible) finalY else startY
        }
        val w by transition.animateDp(transitionSpec = { transitionSpec() }, label = "w") {
            if (it == androidx.compose.animation.EnterExitState.Visible) finalW else startW
        }
        val h by transition.animateDp(transitionSpec = { transitionSpec() }, label = "h") {
            if (it == androidx.compose.animation.EnterExitState.Visible) finalH else startH
        }
        val radius by transition.animateDp(transitionSpec = { transitionSpec() }, label = "radius") {
            if (it == androidx.compose.animation.EnterExitState.Visible) 24.dp else 12.dp
        }
        
        val cardColor by androidx.compose.animation.animateColorAsState(
            targetValue = if (transition.targetState == androidx.compose.animation.EnterExitState.Visible) Color(0xFFF6EEE6) else Color(0xFFF6EEE6),
            animationSpec = tween(300)
        )

        val bgAlpha by transition.animateFloat(
            transitionSpec = { tween(300) },
            label = "bgAlpha"
        ) { if (it == androidx.compose.animation.EnterExitState.Visible) 0.4f else 0f }

        val contentAlpha by transition.animateFloat(
            transitionSpec = { 
                if (transition.targetState == androidx.compose.animation.EnterExitState.Visible) {
                    tween(300, delayMillis = 100)
                } else {
                    tween(100)
                }
            },
            label = "contentAlpha"
        ) { if (it == androidx.compose.animation.EnterExitState.Visible) 1f else 0f }

        val charStartW = with(density) { (charBounds?.width ?: (startW - 16.dp).value).toDp() }
        val charStartH = with(density) { (charBounds?.height ?: (startH - 60.dp).value).toDp() }
        val charStartX = with(density) { (charBounds?.left ?: (startX + 8.dp).value).toDp() }
        val charStartY = with(density) { (charBounds?.top ?: (startY + 48.dp).value).toDp() }
        
        val charFinalW = finalW - 48.dp
        val charFinalH = 220.dp
        val charFinalX = finalX + 24.dp
        val charFinalY = finalY + 84.dp
        
        val charAbsX by transition.animateDp(transitionSpec = { transitionSpec() }, label = "charX") {
            if (it == androidx.compose.animation.EnterExitState.Visible) charFinalX else charStartX
        }
        val charAbsY by transition.animateDp(transitionSpec = { transitionSpec() }, label = "charY") {
            if (it == androidx.compose.animation.EnterExitState.Visible) charFinalY else charStartY
        }
        val charW by transition.animateDp(transitionSpec = { transitionSpec() }, label = "charW") {
            if (it == androidx.compose.animation.EnterExitState.Visible) charFinalW else charStartW
        }
        val charH by transition.animateDp(transitionSpec = { transitionSpec() }, label = "charH") {
            if (it == androidx.compose.animation.EnterExitState.Visible) charFinalH else charStartH
        }
        val charRadius by transition.animateDp(transitionSpec = { transitionSpec() }, label = "charRadius") {
            if (it == androidx.compose.animation.EnterExitState.Visible) 20.dp else 12.dp
        }
        
        val charLocalX = charAbsX - x
        val charLocalY = charAbsY - y
        
        val textAbsStartX = with(density) { (textBounds?.left ?: (startX + (startW / 2) - 30.dp).value).toDp() }
        val textAbsStartY = with(density) { (textBounds?.top ?: (startY + 12.dp).value).toDp() }
        val textAbsFinalX = finalX + 24.dp
        val textAbsFinalY = finalY + 24.dp
        
        val textAbsX by transition.animateDp(transitionSpec = { transitionSpec() }, label = "textX") {
            if (it == androidx.compose.animation.EnterExitState.Visible) textAbsFinalX else textAbsStartX
        }
        val textAbsY by transition.animateDp(transitionSpec = { transitionSpec() }, label = "textY") {
            if (it == androidx.compose.animation.EnterExitState.Visible) textAbsFinalY else textAbsStartY
        }
        val textLocalX = textAbsX - x
        val textLocalY = textAbsY - y
        
        val textScale by transition.animateFloat(transitionSpec = { tween(350, easing = FastOutSlowInEasing) }, label = "textScale") {
            if (it == androidx.compose.animation.EnterExitState.Visible) 1f else (15f / 26f)
        }

        val backgroundLayer = rememberGraphicsLayer()
        var screenCoords: LayoutCoordinates? by remember { mutableStateOf(null) }
        var expandedGarment by remember { mutableStateOf<com.pdm0126.outfix.data.api.dto.GarmentResponse?>(null) }

        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .onGloballyPositioned { screenCoords = it }
                    .drawWithContent {
                        backgroundLayer.record {
                            appBackgroundLayer?.let { drawLayer(it) }
                            this@drawWithContent.drawContent()
                        }
                        drawLayer(backgroundLayer)
                    }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = bgAlpha))
                        .clickable(
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                            indication = null,
                            onClick = onDismiss
                        )
                )

                Box(
                    modifier = Modifier
                        .offset(x = x, y = y)
                        .size(width = w, height = h)
                        .clip(RoundedCornerShape(radius))
                        .background(cardColor)
                        .clickable(
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                            indication = null,
                            onClick = {}
                        )
                ) {
                    val scrollLayer = rememberGraphicsLayer()
                    val scrollState = rememberScrollState()
                    val blurHeight = 36.dp

                    Box(modifier = Modifier.fillMaxSize()) {
                        val topAreaHeight = (charLocalY + charH).coerceAtLeast(0.dp)
                        val totalBlurAreaHeight = topAreaHeight + blurHeight
                        val startFraction = if (totalBlurAreaHeight > 0.dp) (topAreaHeight / totalBlurAreaHeight) else 0f
                        
                        if (contentAlpha > 0f) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer { alpha = contentAlpha }
                                    .drawWithContent {
                                        scrollLayer.record { this@drawWithContent.drawContent() }
                                        clipRect(top = totalBlurAreaHeight.toPx()) {
                                            drawLayer(scrollLayer)
                                        }
                                    }
                                    .verticalScroll(scrollState)
                                    .padding(horizontal = 24.dp)
                            ) {
                                Spacer(modifier = Modifier.height(totalBlurAreaHeight))
                                val slots = listOfNotNull(
                                    activeDayInfo.topGarment?.let { "Superior" to it },
                                    activeDayInfo.bottomGarment?.let { "Inferior" to it },
                                    activeDayInfo.shoesGarment?.let { "Calzado" to it },
                                    activeDayInfo.hatGarment?.let { "Cabeza" to it }
                                ) + activeDayInfo.accessories.map { "Accesorio" to it }

                                if (slots.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(100.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(Color(0xFFF6EEE6))
                                            .border(1.dp, Color.LightGray.copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Toca Editar para crear un outfit",
                                            fontSize = 14.sp,
                                            color = Color.Gray
                                        )
                                    }
                                } else {
                                    BoxWithConstraints(
                                        modifier = Modifier.fillMaxWidth().animateContentSize(
                                            animationSpec = tween(400, easing = FastOutSlowInEasing)
                                        )
                                    ) {
                                        val totalWidth = finalW - 48.dp
                                        val itemSpacing = 10.dp
                                        
                                        data class SlotBounds(val x: androidx.compose.ui.unit.Dp, val y: androidx.compose.ui.unit.Dp, val w: androidx.compose.ui.unit.Dp, val h: androidx.compose.ui.unit.Dp)
                                        
                                        val slotBoundsList = remember(slots, expandedGarment, totalWidth) {
                                            val bounds = mutableListOf<SlotBounds>()
                                            var currentY = 0.dp
                                            var currentX = 0.dp
                                            var rowMaxH = 0.dp

                                            for (i in slots.indices) {
                                                val garment = slots[i].second
                                                val isExpanded = expandedGarment?.id == garment.id
                                                
                                                val w = if (isExpanded) totalWidth else (totalWidth - itemSpacing) / 2f
                                                val h = w + 28.dp 

                                                if (currentX + w > totalWidth + 1.dp) { 
                                                    currentX = 0.dp
                                                    currentY += rowMaxH + itemSpacing
                                                    rowMaxH = 0.dp
                                                }

                                                bounds.add(SlotBounds(currentX, currentY, w, h))

                                                currentX += w + itemSpacing
                                                rowMaxH = maxOf(rowMaxH, h)
                                            }
                                            bounds
                                        }

                                        val totalHeight = if (slotBoundsList.isEmpty()) 0.dp else slotBoundsList.maxOf { it.y + it.h }

                                        LaunchedEffect(expandedGarment) {
                                            if (expandedGarment != null) {
                                                val index = slots.indexOfFirst { it.second.id == expandedGarment?.id }
                                                if (index >= 0) {
                                                    val bounds = slotBoundsList[index]
                                                    val visibleH = finalH - totalBlurAreaHeight
                                                    val targetScrollY = bounds.y + (bounds.h / 2) - (visibleH / 2)
                                                    
                                                    val targetPx = with(density) { targetScrollY.toPx().toInt().coerceAtLeast(0) }
                                                    scrollState.animateScrollTo(targetPx, animationSpec = tween(400, easing = FastOutSlowInEasing))
                                                }
                                            }
                                        }

                                        Box(modifier = Modifier.fillMaxWidth().height(totalHeight)) {
                                            slots.forEachIndexed { index, (label, garment) ->
                                                val isExpanded = expandedGarment?.id == garment.id
                                                val targetBounds = slotBoundsList[index]

                                                val animX by androidx.compose.animation.core.animateDpAsState(targetValue = targetBounds.x, animationSpec = tween(400, easing = FastOutSlowInEasing), label = "x")
                                                val animY by androidx.compose.animation.core.animateDpAsState(targetValue = targetBounds.y, animationSpec = tween(400, easing = FastOutSlowInEasing), label = "y")
                                                val animW by androidx.compose.animation.core.animateDpAsState(targetValue = targetBounds.w, animationSpec = tween(400, easing = FastOutSlowInEasing), label = "w")

                                                val animFontSize by androidx.compose.animation.core.animateFloatAsState(targetValue = if (isExpanded) 13f else 11f, animationSpec = tween(400, easing = FastOutSlowInEasing), label = "font")
                                                val animImagePadding by androidx.compose.animation.core.animateDpAsState(targetValue = if (isExpanded) 16.dp else 8.dp, animationSpec = tween(400, easing = FastOutSlowInEasing), label = "pad")

                                                Box(
                                                    modifier = Modifier
                                                        .offset(x = animX, y = animY)
                                                        .width(animW)
                                                ) {
                                                    Column(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalAlignment = Alignment.CenterHorizontally
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .aspectRatio(1f)
                                                                .clip(RoundedCornerShape(16.dp))
                                                                .background(Color.White)
                                                                .border(1.dp, Color.LightGray.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                                                                .clickable(
                                                                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                                                    indication = null
                                                                ) {
                                                                    expandedGarment = if (isExpanded) null else garment
                                                                },
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            AsyncImage(
                                                                model = garment.imageUrl,
                                                                contentDescription = garment.name,
                                                                contentScale = ContentScale.Fit,
                                                                modifier = Modifier.fillMaxSize().padding(animImagePadding)
                                                            )
                                                        }
                                                        Spacer(modifier = Modifier.height(4.dp))
                                                        Text(
                                                            text = garment.name.take(18),
                                                            fontSize = animFontSize.sp,
                                                            color = Color.DarkGray,
                                                            fontWeight = FontWeight.Medium
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(32.dp))
                            }
                        }
                        
                        if (android.os.Build.VERSION.SDK_INT >= 31 && contentAlpha > 0f) {
                            com.pdm0126.outfix.ui.ProgressiveBlurLayer(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(totalBlurAreaHeight)
                                    .clipToBounds(),
                                contentLayer = scrollLayer,
                                maxBlur = 0f,
                                fadeStartFraction = startFraction,
                                fadeEndFraction = 1f
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .offset(y = topAreaHeight)
                                .height(blurHeight)
                                .background(
                                    androidx.compose.ui.graphics.Brush.verticalGradient(
                                        colors = listOf(Color(0xFFF6EEE6), Color(0xFFF6EEE6).copy(alpha = 0f))
                                    )
                                )
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(topAreaHeight)
                                .background(Color(0xFFF6EEE6))
                        )

                        Box(
                            modifier = Modifier
                                .offset(x = charLocalX.coerceAtLeast(0.dp), y = charLocalY.coerceAtLeast(0.dp))
                                .size(width = charW.coerceAtLeast(0.dp), height = charH.coerceAtLeast(0.dp))
                                .clip(RoundedCornerShape(charRadius))
                                .background(Color(0xFFF6EEE6))
                        ) {
                            CharacterWithClothes(
                                top = activeDayInfo.topGarment,
                                bottom = activeDayInfo.bottomGarment,
                                shoes = activeDayInfo.shoesGarment,
                                head = activeDayInfo.hatGarment,
                                accessories = activeDayInfo.accessories,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        
                        if (contentAlpha > 0f) {
                            val isEmpty = activeDayInfo.topGarment == null &&
                                    activeDayInfo.bottomGarment == null &&
                                    activeDayInfo.shoesGarment == null
                            Text(
                                text = if (isEmpty) "Sin outfit guardado" else "Outfit del día",
                                fontSize = 13.sp,
                                color = Color.Gray,
                                modifier = Modifier
                                    .offset(x = 24.dp, y = 54.dp)
                                    .graphicsLayer { alpha = contentAlpha }
                            )
                            
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(top = 24.dp, end = 24.dp)
                                    .size(40.dp)
                                    .graphicsLayer { alpha = contentAlpha }
                                    .bouncyClickable {
                                        onDismiss()
                                        onEditDay(activeDayInfo.day)
                                    }
                                    .clip(androidx.compose.foundation.shape.CircleShape)
                                    .background(Color(0xFFBDBDBD)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Edit,
                                    contentDescription = "Editar día",
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        
                        Text(
                            text = activeDayInfo.day,
                            fontWeight = FontWeight.Bold,
                            fontSize = 26.sp,
                            fontFamily = FontFamily.Serif,
                            color = Color.Black,
                            modifier = Modifier
                                .offset(x = textLocalX, y = textLocalY)
                                .graphicsLayer {
                                    scaleX = textScale
                                    scaleY = textScale
                                    transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0f, 0f)
                                }
                        )
                    }
                }
            }
        }
    }
}
