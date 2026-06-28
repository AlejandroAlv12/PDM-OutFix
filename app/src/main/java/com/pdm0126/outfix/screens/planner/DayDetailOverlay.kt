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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
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
import com.pdm0126.outfix.data.mock.DayInfo
import com.pdm0126.outfix.screens.closet.ClosetOverlayState
import com.pdm0126.outfix.ui.CharacterWithClothes
import com.pdm0126.outfix.ui.GlobalNavigationState
import com.pdm0126.outfix.ui.OutFixScreen

@Composable
fun DayDetailOverlay(
    dayInfo: DayInfo?,
    sourceBounds: androidx.compose.ui.geometry.Rect?,
    appBackgroundLayer: androidx.compose.ui.graphics.layer.GraphicsLayer? = null,
    onDismiss: () -> Unit
) {
    AnimatedVisibility(
        visible = ClosetOverlayState.isDayOverlayActive,
        enter = fadeIn(animationSpec = tween(durationMillis = 1)),
        exit = fadeOut(animationSpec = tween(durationMillis = 1, delayMillis = 350))
    ) {
        val activeDayInfo = dayInfo ?: return@AnimatedVisibility

        val transition = this.transition

        val configuration = androidx.compose.ui.platform.LocalConfiguration.current
        val density = androidx.compose.ui.platform.LocalDensity.current

        val startW = with(density) { (sourceBounds?.width ?: 120f).toDp() }
        val startH = with(density) { (sourceBounds?.height ?: 200f).toDp() }
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

        val bgAlpha by transition.animateFloat(
            transitionSpec = { tween(300) },
            label = "bgAlpha"
        ) { if (it == androidx.compose.animation.EnterExitState.Visible) 0.4f else 0f }

        val contentAlpha by transition.animateFloat(
            transitionSpec = { tween(300, delayMillis = 100) },
            label = "contentAlpha"
        ) { if (it == androidx.compose.animation.EnterExitState.Visible) 1f else 0f }

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
                // Dim background — tapping it dismisses
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

                // Expanding card
                Box(
                    modifier = Modifier
                        .offset(x = x, y = y)
                        .size(width = w, height = h)
                        .clip(RoundedCornerShape(radius))
                        .background(Color.White)
                        .clickable(
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                            indication = null,
                            onClick = {}
                        )
                ) {
                    if (contentAlpha > 0f) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .graphicsLayer { alpha = contentAlpha }
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = activeDayInfo.day,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 26.sp,
                                        fontFamily = FontFamily.Serif,
                                        color = Color.Black
                                    )
                                    val isEmpty = activeDayInfo.topGarment == null &&
                                            activeDayInfo.bottomGarment == null &&
                                            activeDayInfo.shoesGarment == null
                                    Text(
                                        text = if (isEmpty) "Sin outfit guardado" else "Outfit del día",
                                        fontSize = 13.sp,
                                        color = Color.Gray
                                    )
                                }
                                // Edit button
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(androidx.compose.foundation.shape.CircleShape)
                                        .background(Color.Black.copy(alpha = 0.08f))
                                        .clickable {
                                            onDismiss()
                                            ClosetOverlayState.plannerEditDay = activeDayInfo.day
                                            ClosetOverlayState.hasLoadedPlannerDay = false
                                            GlobalNavigationState.requestedTab = OutFixScreen.Closet
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Edit,
                                        contentDescription = "Editar día",
                                        tint = Color.Black,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Character preview
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color(0xFFF6EEE6))
                                    .border(1.dp, Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
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

                            Spacer(modifier = Modifier.height(20.dp))

                            // Garment slots grid
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
                                                    Text(
                                                        text = label,
                                                        fontSize = 10.sp,
                                                        color = Color.Gray
                                                    )
                                                }
                                            }
                                            // Fill remaining slot if odd number
                                            if (row.size == 1) {
                                                Spacer(modifier = Modifier.weight(1f))
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            } // End drawWithContent Box
        }
    }
}
