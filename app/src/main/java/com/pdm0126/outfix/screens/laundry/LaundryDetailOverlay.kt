package com.pdm0126.outfix.screens.laundry

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocalLaundryService
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.pdm0126.outfix.data.api.dto.GarmentResponse
import com.pdm0126.outfix.ui.bouncyClickable

@Composable
fun LaundryDetailOverlay(
    isActive: Boolean,
    titleText: String,
    subtitleText: String,
    buttonBounds: androidx.compose.ui.geometry.Rect?,
    garment: GarmentResponse?,
    sourceBounds: androidx.compose.ui.geometry.Rect?,
    appBackgroundLayer: androidx.compose.ui.graphics.layer.GraphicsLayer? = null,
    onDismiss: () -> Unit,
    onWash: (String) -> Unit
) {
    AnimatedVisibility(
        visible = isActive,
        enter = fadeIn(animationSpec = tween(durationMillis = 1)),
        exit = fadeOut(animationSpec = tween(durationMillis = 1, delayMillis = 350))
    ) {
        val activeGarment = garment ?: return@AnimatedVisibility

        androidx.activity.compose.BackHandler(enabled = isActive) {
            onDismiss()
        }

        val transition = this.transition
        
        val configuration = LocalConfiguration.current
        val density = LocalDensity.current

        val startW = with(density) { (sourceBounds?.width ?: 300f).toDp() }
        val startH = with(density) { (sourceBounds?.height ?: 120f).toDp() }
        val startX = with(density) { (sourceBounds?.left ?: 0f).toDp() }
        val startY = with(density) { (sourceBounds?.top ?: 0f).toDp() }

        val finalW = configuration.screenWidthDp.dp * 0.9f
        val finalH = configuration.screenHeightDp.dp * 0.5f
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
            if (it == androidx.compose.animation.EnterExitState.Visible) 24.dp else 24.dp
        }
        
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

        val backgroundLayer = androidx.compose.ui.graphics.rememberGraphicsLayer()

        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawWithContent {
                        backgroundLayer.record {
                            appBackgroundLayer?.let { drawLayer(it) }
                            this@drawWithContent.drawContent()
                        }
                        drawLayer(backgroundLayer)
                    }
            ) {
                // Dim background
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

                // Expanding white card
                Box(
                    modifier = Modifier
                        .offset(x = x, y = y)
                        .size(width = w, height = h)
                        .clip(RoundedCornerShape(radius))
                        .background(Color.White)
                ) {
                    // We animate the image box inside the white card
                    // In the list, the image box is 96.dp by 96.dp, left padded 16.dp, vertically centered.
                    // The original card height is approx 128.dp, so vertically centered is ~16.dp top.
                    
                    val imgStartSize = 96.dp
                    val imgStartPaddingX = 16.dp
                    val imgStartPaddingY = (startH - imgStartSize) / 2f
                    
                    val imgFinalSizeW = finalW - 32.dp
                    val imgFinalSizeH = finalH - 32.dp
                    val imgFinalPaddingX = 16.dp
                    val imgFinalPaddingY = 16.dp

                    val imgSizeW by transition.animateDp(transitionSpec = { transitionSpec() }, label = "imgSizeW") {
                        if (it == androidx.compose.animation.EnterExitState.Visible) imgFinalSizeW else imgStartSize
                    }
                    val imgSizeH by transition.animateDp(transitionSpec = { transitionSpec() }, label = "imgSizeH") {
                        if (it == androidx.compose.animation.EnterExitState.Visible) imgFinalSizeH else imgStartSize
                    }
                    val imgPaddingX by transition.animateDp(transitionSpec = { transitionSpec() }, label = "imgPadX") {
                        if (it == androidx.compose.animation.EnterExitState.Visible) imgFinalPaddingX else imgStartPaddingX
                    }
                    val imgPaddingY by transition.animateDp(transitionSpec = { transitionSpec() }, label = "imgPadY") {
                        if (it == androidx.compose.animation.EnterExitState.Visible) imgFinalPaddingY else imgStartPaddingY
                    }

                    Column(
                        modifier = Modifier
                            .padding(start = 132.dp, top = (startH - 50.dp) / 2f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.Center
                    ) {
                        androidx.compose.material3.Text(
                            text = titleText,
                            fontSize = 22.sp,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        androidx.compose.material3.Text(
                            text = subtitleText,
                            fontSize = 14.sp,
                            color = Color.DarkGray
                        )
                    }

                    Box(
                        modifier = Modifier
                            .padding(start = imgPaddingX, top = imgPaddingY)
                            .size(width = imgSizeW, height = imgSizeH)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFF6EEE6)),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                                .data(activeGarment.imageUrl)
                                .size(coil.size.Size.ORIGINAL)
                                .build(),
                            contentDescription = activeGarment.name,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp)
                        )
                    }
                }
                
                if (buttonBounds != null) {
                    val btnSize = 40.dp
                    val btnStartX = with(density) { buttonBounds.left.toDp() }
                    val btnStartY = with(density) { buttonBounds.top.toDp() }
                    
                    val btnFinalX = finalX + 16.dp + (finalW - 32.dp) - 40.dp - 8.dp
                    val btnFinalY = finalY + 16.dp + 8.dp
                    
                    val btnX by transition.animateDp(transitionSpec = { transitionSpec() }, label = "btnX") {
                        if (it == androidx.compose.animation.EnterExitState.Visible) btnFinalX else btnStartX
                    }
                    val btnY by transition.animateDp(transitionSpec = { transitionSpec() }, label = "btnY") {
                        if (it == androidx.compose.animation.EnterExitState.Visible) btnFinalY else btnStartY
                    }
                    var isConfirming by remember { mutableStateOf(false) }
                    
                    LaunchedEffect(transition.targetState) {
                        if (transition.targetState == androidx.compose.animation.EnterExitState.Visible) {
                            isConfirming = false
                        }
                    }
                    
                    val btnColor by androidx.compose.animation.animateColorAsState(
                        targetValue = if (isConfirming) Color.DarkGray else Color(0xFFBDBDBD),
                        animationSpec = tween(300),
                        label = "btnColor"
                    )

                    Box(
                        modifier = Modifier
                            .offset(x = btnX, y = btnY)
                            .size(btnSize)
                            .bouncyClickable {
                                if (isConfirming) {
                                    onWash(activeGarment.id)
                                } else {
                                    isConfirming = true
                                }
                            }
                            .clip(CircleShape)
                            .background(btnColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.LocalLaundryService,
                            contentDescription = "Lavar",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
    }
}
