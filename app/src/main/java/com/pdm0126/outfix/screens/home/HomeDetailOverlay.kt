package com.pdm0126.outfix.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.draw.scale
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
import com.pdm0126.outfix.screens.closet.ClosetOverlayState
import com.pdm0126.outfix.ui.CharacterWithClothes

@Composable
fun HomeDetailOverlay(
    dayInfo: DayInfo?,
    sourceBounds: androidx.compose.ui.geometry.Rect?,
    appBackgroundLayer: androidx.compose.ui.graphics.layer.GraphicsLayer? = null,
    onDismiss: () -> Unit
) {
    AnimatedVisibility(
        visible = ClosetOverlayState.isHomeOverlayActive,
        enter = fadeIn(animationSpec = tween(durationMillis = 1)),
        exit = fadeOut(animationSpec = tween(durationMillis = 1, delayMillis = 350))
    ) {
        val activeDayInfo = dayInfo ?: return@AnimatedVisibility

        var expandedGarment by remember { mutableStateOf<com.pdm0126.outfix.data.api.dto.GarmentResponse?>(null) }
        
        androidx.activity.compose.BackHandler(enabled = ClosetOverlayState.isHomeOverlayActive) {
            if (expandedGarment != null) {
                expandedGarment = null
            } else {
                onDismiss()
            }
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
            if (it == androidx.compose.animation.EnterExitState.Visible) 24.dp else 16.dp
        }
        
        val cardColor by androidx.compose.animation.animateColorAsState(
            targetValue = if (transition.targetState == androidx.compose.animation.EnterExitState.Visible) Color.White else Color(0xFFF6EEE6),
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
        val charStartW = 150.dp
        val charStartH = 250.dp
        val charStartX = startX + startW - 160.dp
        val charStartY = startY + 38.dp
        
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
            if (it == androidx.compose.animation.EnterExitState.Visible) 20.dp else 16.dp
        }
        
        val charLocalX = charAbsX - x
        val charLocalY = charAbsY - y
        
        val charScale by transition.animateFloat(transitionSpec = { tween(350, easing = FastOutSlowInEasing) }, label = "charScale") {
            if (it == androidx.compose.animation.EnterExitState.Visible) 1f else 1f
        }
        
        val hoyWidthEstimate = 45.dp
        val textAbsStartX = startX + startW - 85.dp - (hoyWidthEstimate / 2)
        val textAbsStartY = startY + 10.dp
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
        
        val textSize by transition.animateFloat(transitionSpec = { tween(350, easing = FastOutSlowInEasing) }, label = "textSize") {
            if (it == androidx.compose.animation.EnterExitState.Visible) 26f else 24f
        }

        val allGarments = listOfNotNull(
            activeDayInfo.topGarment,
            activeDayInfo.bottomGarment,
            activeDayInfo.shoesGarment,
            activeDayInfo.hatGarment
        ) + activeDayInfo.accessories
        
        val styles = allGarments.mapNotNull { it.style }.filter { it.isNotBlank() }
        
        val styleString = if (styles.isEmpty()) {
            "Casual"
        } else {
            val styleCounts = styles.groupingBy { it.lowercase() }.eachCount()
            val maxCount = styleCounts.values.maxOrNull() ?: 0
            val majorityStyles = styleCounts.filterValues { it == maxCount }.keys.toList()
        
            when {
                majorityStyles.size == 1 -> majorityStyles.first()
                maxCount == 1 -> "Exótico"
                else -> "Mixto"
            }
        }.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

        val styleWidthEstimate = (styleString.length * 8).dp
        val styleAbsStartX = startX + startW - 85.dp - (styleWidthEstimate / 2)
        val styleAbsStartY = startY + startH - 28.dp
        
        val styleAbsFinalX = finalX + 24.dp
        val styleAbsFinalY = finalY + 54.dp
        
        val styleAbsX by transition.animateDp(transitionSpec = { transitionSpec() }, label = "styleX") {
            if (it == androidx.compose.animation.EnterExitState.Visible) styleAbsFinalX else styleAbsStartX
        }
        val styleAbsY by transition.animateDp(transitionSpec = { transitionSpec() }, label = "styleY") {
            if (it == androidx.compose.animation.EnterExitState.Visible) styleAbsFinalY else styleAbsStartY
        }
        val styleLocalX = styleAbsX - x
        val styleLocalY = styleAbsY - y
        
        val styleSize by transition.animateFloat(transitionSpec = { tween(350, easing = FastOutSlowInEasing) }, label = "styleSize") {
            if (it == androidx.compose.animation.EnterExitState.Visible) 13f else 16f
        }
        
        val styleColor by androidx.compose.animation.animateColorAsState(
            targetValue = if (transition.targetState == androidx.compose.animation.EnterExitState.Visible) Color.Gray else Color.Black,
            animationSpec = tween(350)
        )

        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier.fillMaxSize()
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
                                    .verticalScroll(rememberScrollState())
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
                                        val totalWidth = maxWidth
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
                                                                .background(Color(0xFFF6EEE6))
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
                        
                        // 1. Progressive Blur
                        if (android.os.Build.VERSION.SDK_INT >= 31 && contentAlpha > 0f) {
                            com.pdm0126.outfix.ui.ProgressiveBlurLayer(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(totalBlurAreaHeight)
                                    .clipToBounds(),
                                contentLayer = scrollLayer,
                                maxBlur = 60f,
                                fadeStartFraction = startFraction,
                                fadeEndFraction = 1f
                            )
                        }

                        // 2. White to Transparent gradient exactly at the gap
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .offset(y = topAreaHeight)
                                .height(blurHeight)
                                .background(
                                    androidx.compose.ui.graphics.Brush.verticalGradient(
                                        colors = listOf(Color.White, Color.White.copy(alpha = 0f))
                                    )
                                )
                        )

                        // 3. Solid White Mask over the top area
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(topAreaHeight)
                                .background(Color.White)
                        )

                        Box(
                            modifier = Modifier
                                .offset(x = charLocalX.coerceAtLeast(0.dp), y = charLocalY.coerceAtLeast(0.dp))
                                .size(width = charW.coerceAtLeast(0.dp), height = charH.coerceAtLeast(0.dp))
                                .clip(RoundedCornerShape(charRadius))
                                .background(Color(0xFFF6EEE6))
                        )
                        
                        Box(
                            modifier = Modifier
                                .offset(x = charLocalX.coerceAtLeast(0.dp), y = charLocalY.coerceAtLeast(0.dp))
                                .size(width = charW.coerceAtLeast(0.dp), height = charH.coerceAtLeast(0.dp))
                        ) {
                            CharacterWithClothes(
                                top = activeDayInfo.topGarment,
                                bottom = activeDayInfo.bottomGarment,
                                shoes = activeDayInfo.shoesGarment,
                                head = activeDayInfo.hatGarment,
                                accessories = activeDayInfo.accessories,
                                modifier = Modifier.fillMaxSize().scale(charScale)
                            )
                        }
                        
                        Text(
                            text = "Hoy",
                            fontWeight = FontWeight.Bold,
                            fontSize = textSize.sp,
                            fontFamily = FontFamily.Serif,
                            color = Color.Black,
                            modifier = Modifier.offset(x = textLocalX, y = textLocalY)
                        )
                        
                        Text(
                            text = styleString,
                            fontSize = styleSize.sp,
                            fontWeight = if (styleSize > 14f) FontWeight.Bold else FontWeight.Normal,
                            fontFamily = FontFamily.Serif,
                            color = styleColor,
                            modifier = Modifier.offset(x = styleLocalX, y = styleLocalY)
                        )
                    }
                }
            }
        }
    }
}
