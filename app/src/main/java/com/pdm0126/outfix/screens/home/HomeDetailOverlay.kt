package com.pdm0126.outfix.screens.home

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
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
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

        // Character Box Dimensions
        // Original character Box is bounded by padding(16.dp), plus some spaces for texts.
        // It has weight(1f), so its height varies, but let's estimate it based on its constraints.
        // In HomeScreen Hoy widget: padding = 16.dp on all sides.
        // Top text "Hoy" (24.sp) ~ 34.dp height, plus 16.dp spacer = 50.dp offset from top padding -> 66.dp from top edge
        // Bottom text "Style" (16.sp) ~ 24.dp height, plus 16.dp spacer = 40.dp offset from bottom padding -> 56.dp from bottom edge
        val charStartW = startW - 32.dp
        val charStartH = startH - 122.dp
        val charStartX = startX + 16.dp
        val charStartY = startY + 66.dp
        
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
            if (it == androidx.compose.animation.EnterExitState.Visible) 1f else 1.5f
        }
        
        val hoyWidthEstimate = 45.dp
        val textAbsStartX = startX + (startW / 2) - (hoyWidthEstimate / 2)
        val textAbsStartY = startY + 16.dp
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
        val styleAbsStartX = startX + (startW / 2) - (styleWidthEstimate / 2)
        val styleAbsStartY = startY + startH - 40.dp
        
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

        val backgroundLayer = rememberGraphicsLayer()
        var screenCoords: LayoutCoordinates? by remember { mutableStateOf(null) }

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
                    Box(modifier = Modifier.fillMaxSize()) {
                        
                        if (contentAlpha > 0f) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                                    .graphicsLayer { alpha = contentAlpha }
                                    .padding(horizontal = 24.dp)
                            ) {
                                Spacer(modifier = Modifier.height(charLocalY + charH))
                                
                                Spacer(modifier = Modifier.height(20.dp))
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
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        slots.chunked(2).forEach { row ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                row.forEach { (label, garment) ->
                                                    Column(
                                                        modifier = Modifier.weight(1f),
                                                        horizontalAlignment = Alignment.CenterHorizontally
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .aspectRatio(1f)
                                                            .clip(RoundedCornerShape(16.dp))
                                                            .background(Color(0xFFF6EEE6))
                                                            .border(1.dp, Color.LightGray.copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            AsyncImage(
                                                                model = garment.imageUrl,
                                                                contentDescription = garment.name,
                                                                contentScale = ContentScale.Fit,
                                                                modifier = Modifier.fillMaxSize().padding(8.dp)
                                                            )
                                                        }
                                                        Spacer(modifier = Modifier.height(4.dp))
                                                        Text(
                                                            text = garment.name.take(18),
                                                            fontSize = 11.sp,
                                                            color = Color.DarkGray,
                                                            fontWeight = FontWeight.Medium
                                                        )
                                                    }
                                                }
                                                if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
                                            }
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(32.dp))
                            }
                        }
                        
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
