package com.pdm0126.outfix.screens.closet

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pdm0126.outfix.data.api.dto.GarmentResponse
import com.pdm0126.outfix.ui.theme.LimeGreen
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateDp
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.layout
import androidx.compose.ui.zIndex
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import com.pdm0126.outfix.data.GARMENT_CATEGORIES
import com.pdm0126.outfix.ui.bouncyClickable
import androidx.compose.material.icons.rounded.ChevronLeft

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GarmentDetailOverlay(
    garment: GarmentResponse?,
    sourceBounds: androidx.compose.ui.geometry.Rect?,
    onDismiss: () -> Unit,
    onUpdate: (GarmentResponse) -> Unit,
    onDelete: (String) -> Unit,
    appBackgroundLayer: androidx.compose.ui.graphics.layer.GraphicsLayer? = null
) {
    AnimatedVisibility(
        visible = com.pdm0126.outfix.screens.closet.ClosetOverlayState.isOverlayActive,
        enter = fadeIn(animationSpec = tween(durationMillis = 1)),
        exit = fadeOut(animationSpec = tween(durationMillis = 1, delayMillis = 350))
    ) {
        val activeGarment = garment ?: return@AnimatedVisibility

        var confirmDelete by remember { mutableStateOf(false) }

        var editName by remember(activeGarment) { mutableStateOf(activeGarment.name) }
        var selectedCategory by remember(activeGarment) { mutableStateOf(activeGarment.category) }
        var selectedColorHex by remember(activeGarment) { mutableStateOf(activeGarment.colorHex ?: "#000000") }
        var editBrand by remember(activeGarment) { mutableStateOf(activeGarment.brand ?: "") }
        var selectedStyle by remember(activeGarment) { mutableStateOf(activeGarment.style ?: "casual") }
        
        val currentSizes = if (selectedCategory in com.pdm0126.outfix.data.SHOE_CATEGORIES) com.pdm0126.outfix.data.SHOE_SIZES else com.pdm0126.outfix.data.GARMENT_SIZES
        var selectedSizeIndex by remember(activeGarment, selectedCategory) { 
            mutableStateOf(currentSizes.indexOf(activeGarment.size).takeIf { it >= 0 } ?: 2) 
        }

        var showCategoryMenu by remember { mutableStateOf(false) }
        var isColorPickingMode by remember { mutableStateOf(false) }

        val hasChanges = editName != activeGarment.name ||
                         selectedCategory != activeGarment.category ||
                         selectedColorHex != (activeGarment.colorHex ?: "#000000") ||
                         editBrand != (activeGarment.brand ?: "") ||
                         selectedStyle != (activeGarment.style ?: "casual") ||
                         currentSizes.getOrNull(selectedSizeIndex) != (activeGarment.size ?: "")

        val transition = this.transition
        
        LaunchedEffect(transition.currentState) {
            if (transition.currentState == androidx.compose.animation.EnterExitState.PostExit && !com.pdm0126.outfix.screens.closet.ClosetOverlayState.isOverlayActive) {
                com.pdm0126.outfix.screens.closet.ClosetOverlayState.detailGarment = null
            }
        }
        
        val configuration = androidx.compose.ui.platform.LocalConfiguration.current
        val density = androidx.compose.ui.platform.LocalDensity.current
        
        val startW = with(density) { (sourceBounds?.width ?: 200f).toDp() }
        val startH = with(density) { (sourceBounds?.height ?: 200f).toDp() }
        val startX = with(density) { (sourceBounds?.left ?: 0f).toDp() }
        val startY = with(density) { (sourceBounds?.top ?: 0f).toDp() }

        val finalW = configuration.screenWidthDp.dp * 0.9f
        val finalH = configuration.screenHeightDp.dp * 0.85f
        
        val finalX = (configuration.screenWidthDp.dp - finalW) / 2f
        val finalY = (configuration.screenHeightDp.dp - finalH) / 2f
        
        val topPadding = (finalW - 200.dp) / 2f
        
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

        val focusManager = androidx.compose.ui.platform.LocalFocusManager.current

        val contentAlpha by transition.animateFloat(
            transitionSpec = { tween(300, delayMillis = 100) },
            label = "contentAlpha"
        ) { if (it == androidx.compose.animation.EnterExitState.Visible) 1f else 0f }

        val backgroundLayer = androidx.compose.ui.graphics.rememberGraphicsLayer()
        var buttonCoords: LayoutCoordinates? by remember { mutableStateOf(null) }
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
                        onClick = { onDismiss() }
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
                        onClick = { 
                            focusManager.clearFocus()
                            confirmDelete = false
                        }
                    )
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    
                    if (contentAlpha > 0f) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp)
                                .graphicsLayer { alpha = contentAlpha }
                                .pointerInput(Unit) {
                                    detectTapGestures(onTap = {
                                        focusManager.clearFocus()
                                        confirmDelete = false
                                    })
                                }
                        ) {
                            
                            Spacer(modifier = Modifier.height((topPadding + 200.dp) - 24.dp))
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .verticalScroll(rememberScrollState()),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    EditableDetailRow("Nombre", editName) { editName = it }
                                
                                // Category
                                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                                    Text(text = "Categoría", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    com.pdm0126.outfix.ui.LiquidDropdownButton(
                                        selectedItem = selectedCategory,
                                        isExpanded = showCategoryMenu,
                                        onClick = { showCategoryMenu = true },
                                        onGloballyPositioned = { buttonCoords = it },
                                        modifier = Modifier.align(Alignment.CenterHorizontally).zIndex(10f)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                                }

                                ColorDetailRow("Color", selectedColorHex, isColorPickingMode) {
                                    isColorPickingMode = !isColorPickingMode
                                }

                                EditableDetailRow("Marca", editBrand) { editBrand = it }
                                
                                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                                    Text(text = "Estilo", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    com.pdm0126.outfix.ui.StyleSlider(
                                        selectedStyle = selectedStyle,
                                        onStyleSelected = { selectedStyle = it },
                                        modifier = Modifier.extendHorizontally(24.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                                }
                                
                                // Size Slider
                                if (selectedCategory !in listOf("Bolso", "Mochila", "Gorra", "Sombrero", "Gorro", "Gafas", "Reloj", "Cinturón", "Corbata", "Bufanda", "Joyería", "Otro")) {
                                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                                        Text(text = "Talla", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                                        com.pdm0126.outfix.ui.CustomSizeSlider(
                                            sizes = currentSizes,
                                            selectedIndex = selectedSizeIndex.coerceIn(0, currentSizes.size - 1),
                                            onIndexChanged = { selectedSizeIndex = it }
                                        )
                                    }
                                }
                            }
                            
                            // Removed VerticalEdgesProgressiveBlurLayer to prevent interference with child blurs
                            
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .extendHorizontally(24.dp)
                                    .background(
                                        androidx.compose.ui.graphics.Brush.verticalGradient(
                                            0.0f to cardColor,
                                            0.05f to Color.Transparent,
                                            0.95f to Color.Transparent,
                                            1.0f to cardColor
                                        )
                                    )
                            )
                        }
                            
                        Spacer(modifier = Modifier.height(16.dp))
                            
                            val deleteColor by androidx.compose.animation.animateColorAsState(
                                targetValue = Color(0xFFFF0000).copy(alpha = if (confirmDelete) 0.9f else 0.5f),
                                animationSpec = androidx.compose.animation.core.tween(300),
                                label = "deleteColor"
                            )
                            val deleteWidth by androidx.compose.animation.core.animateFloatAsState(
                                targetValue = if (confirmDelete) 0.7f else 0.5f,
                                animationSpec = androidx.compose.animation.core.spring(
                                    dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
                                    stiffness = androidx.compose.animation.core.Spring.StiffnessLow
                                ),
                                label = "deleteWidth"
                            )

                            Box(
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .fillMaxWidth(deleteWidth)
                                    .height(56.dp)
                                    .bouncyClickable { 
                                        if (confirmDelete) {
                                            onDelete(activeGarment.id)
                                        } else {
                                            confirmDelete = true
                                        }
                                    }
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(deleteColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (confirmDelete) "¿Borrar?" else "Borrar",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Floating back button
                        Box(
                            modifier = Modifier
                                .padding(start = 24.dp, top = 24.dp)
                                .size(40.dp)
                                .bouncyClickable { onDismiss() }
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(Color(0xFFBDBDBD)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ChevronLeft,
                                contentDescription = "Regresar",
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        // Floating save button
                        androidx.compose.animation.AnimatedVisibility(
                            visible = hasChanges,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(end = 24.dp, top = 24.dp),
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .bouncyClickable {
                                        val updated = activeGarment.copy(
                                            name = editName.takeIf { it.isNotBlank() } ?: selectedCategory,
                                            category = selectedCategory,
                                            colorHex = selectedColorHex,
                                            style = selectedStyle,
                                            brand = editBrand.takeIf { it.isNotBlank() },
                                            size = currentSizes.getOrNull(selectedSizeIndex)
                                        )
                                        onUpdate(updated)
                                    }
                                    .clip(androidx.compose.foundation.shape.CircleShape)
                                    .background(com.pdm0126.outfix.ui.theme.LimeGreen.copy(alpha = 0.5f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = "Guardar",
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }

                    // La Imagen se dibuja siempre y escala
                    val imgW by transition.animateDp(transitionSpec = { transitionSpec() }, label = "imgW") {
                        if (it == androidx.compose.animation.EnterExitState.Visible) 200.dp else startW
                    }
                    val imgH by transition.animateDp(transitionSpec = { transitionSpec() }, label = "imgH") {
                        if (it == androidx.compose.animation.EnterExitState.Visible) 200.dp else startH
                    }
                    val imgY by transition.animateDp(transitionSpec = { transitionSpec() }, label = "imgY") {
                        if (it == androidx.compose.animation.EnterExitState.Visible) topPadding else 0.dp
                    }

                    val context = androidx.compose.ui.platform.LocalContext.current
                    var imageBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
                    val imageRequest = remember(activeGarment.imageUrl) {
                        coil.request.ImageRequest.Builder(context)
                            .data(activeGarment.imageUrl)
                            .allowHardware(false)
                            .build()
                    }
                    val painter = coil.compose.rememberAsyncImagePainter(
                        model = imageRequest,
                        onSuccess = { state ->
                            val drawable = state.result.drawable
                            imageBitmap = (drawable as? android.graphics.drawable.BitmapDrawable)?.bitmap
                        }
                    )

                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = imgY.coerceAtLeast(0.dp))
                            .size(width = imgW.coerceAtLeast(0.dp), height = imgH.coerceAtLeast(0.dp))
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFF6EEE6)), // Always match GarmentCard background for the image itself!
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.foundation.Image(
                            painter = painter,
                            contentDescription = activeGarment.name,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(isColorPickingMode) {
                                    if (!isColorPickingMode) return@pointerInput
                                    
                                    val handleTouch = { offset: androidx.compose.ui.geometry.Offset ->
                                        val bmp = imageBitmap
                                        if (bmp != null) {
                                            val boxW = size.width.toFloat()
                                            val boxH = size.height.toFloat()
                                            
                                            val imgRatio = bmp.width.toFloat() / bmp.height.toFloat()
                                            val boxRatio = boxW / boxH
                                            
                                            var drawW = boxW
                                            var drawH = boxH
                                            if (imgRatio > boxRatio) {
                                                drawH = boxW / imgRatio
                                            } else {
                                                drawW = boxH * imgRatio
                                            }
                                            
                                            val left = (boxW - drawW) / 2f
                                            val top = (boxH - drawH) / 2f
                                            
                                            val normX = (offset.x - left) / drawW
                                            val normY = (offset.y - top) / drawH
                                            
                                            if (normX in 0f..1f && normY in 0f..1f) {
                                                val bmpX = (normX * bmp.width).toInt().coerceIn(0, bmp.width - 1)
                                                val bmpY = (normY * bmp.height).toInt().coerceIn(0, bmp.height - 1)
                                                try {
                                                    val pixel = bmp.getPixel(bmpX, bmpY)
                                                    selectedColorHex = String.format("#%06X", 0xFFFFFF and pixel)
                                                } catch(e: Exception) {}
                                            }
                                        }
                                    }

                                    awaitEachGesture {
                                        val down = awaitFirstDown()
                                        handleTouch(down.position)
                                        do {
                                            val event = awaitPointerEvent()
                                            event.changes.forEach { 
                                                if (it.pressed) {
                                                    handleTouch(it.position)
                                                    it.consume()
                                                }
                                            }
                                        } while (event.changes.any { it.pressed })
                                        isColorPickingMode = false
                                    }
                                }
                        )
                    } // Close Image Box
                } // Close Inner Card Box
            } // Close Card
            } // Close inner recording Box

            com.pdm0126.outfix.ui.LiquidDropdownOverlay(
                isExpanded = showCategoryMenu,
                onDismissRequest = { showCategoryMenu = false },
                buttonCoords = buttonCoords,
                targetWidth = 200.dp,
                backgroundLayer = backgroundLayer,
                screenCoords = screenCoords,
                items = GARMENT_CATEGORIES,
                selectedItem = selectedCategory,
                onItemSelected = {
                    selectedCategory = it
                    showCategoryMenu = false
                }
            )
        } // Close outer Box


    }
}

@Composable
fun EditableDetailRow(label: String, value: String, onValueChange: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(text = label, fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
        androidx.compose.foundation.text.BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = androidx.compose.ui.text.TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            ),
            cursorBrush = androidx.compose.ui.graphics.SolidColor(Color.Black),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            singleLine = true
        )
        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
    }
}

@Composable
fun ColorDetailRow(label: String, colorHex: String, isPicking: Boolean, onTogglePick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(text = label, fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            val color = remember(colorHex) {
                try { Color(android.graphics.Color.parseColor(colorHex)) } catch(e: Exception) { Color.Black }
            }
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(color)
                    .border(2.dp, if (isPicking) Color.Black else Color.LightGray, androidx.compose.foundation.shape.CircleShape)
                    .clickable { onTogglePick() }
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = if (isPicking) "Toca y arrastra la imagen arriba para elegir un color" else "Toca el círculo para extraer color de la imagen",
                fontSize = 12.sp,
                color = if (isPicking) LimeGreen else Color.Gray,
                fontWeight = if (isPicking) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
    }
}

fun Modifier.extendHorizontally(padding: androidx.compose.ui.unit.Dp) = layout { measurable, constraints ->
    val paddingPx = padding.roundToPx()
    val placeable = measurable.measure(
        constraints.copy(
            maxWidth = constraints.maxWidth + paddingPx * 2,
            minWidth = constraints.minWidth + paddingPx * 2
        )
    )
    layout(constraints.maxWidth, placeable.height) {
        placeable.place(-paddingPx, 0)
    }
}
