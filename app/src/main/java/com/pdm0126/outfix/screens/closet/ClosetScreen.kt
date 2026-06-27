package com.pdm0126.outfix.screens.closet

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.pdm0126.outfix.data.api.dto.GarmentResponse
import com.pdm0126.outfix.data.mock.MockDatabase
import com.pdm0126.outfix.ui.theme.LimeGreen
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.graphics.drawscope.scale
import com.pdm0126.outfix.ui.liquidGlass
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.clipToBounds

@Composable
fun ClosetScreen(viewModel: ClosetViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val coroutineScope = rememberCoroutineScope()
    var garments by remember { mutableStateOf<List<GarmentResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    var selectedTop by viewModel::selectedTop
    var selectedBottom by viewModel::selectedBottom
    var selectedShoes by viewModel::selectedShoes
    var selectedHead by viewModel::selectedHead
    var selectedAccessories by viewModel::selectedAccessories

    LaunchedEffect(MockDatabase.updateTrigger.value) {
        isLoading = true
        garments = MockDatabase.getGarments()
        isLoading = false
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = LimeGreen)
        }
    } else {
        val slidersLayer = rememberGraphicsLayer()
        var rootCoords by remember { mutableStateOf<androidx.compose.ui.layout.LayoutCoordinates?>(null) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned { rootCoords = it }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .drawWithContent {
                        slidersLayer.record {
                            drawRect(Color(0xFFEDDDCC))
                            this@drawWithContent.drawContent()
                        }
                        drawLayer(slidersLayer)
                    }
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(402.dp))

                val tops = remember(garments) { garments.filter { it.category in listOf("Camiseta", "Camisa", "Blusa", "Top", "Suéter", "Chaqueta", "Abrigo", "Vestido") } }
                val bottoms = remember(garments) { garments.filter { it.category in listOf("Jeans", "Pantalón", "Short", "Falda", "Vestido") } }
                val shoes = remember(garments) { garments.filter { it.category in listOf("Zapatillas", "Botas", "Zapatos") } }
                val headwear = remember(garments) { garments.filter { it.category in listOf("Gorra", "Sombrero", "Gorro") } }
                val accessories = remember(garments) { garments.filter { it.category in listOf("Bolso", "Mochila", "Reloj", "Gafas", "Cinturón", "Corbata", "Bufanda", "Joyería", "Accesorio", "Otros", "Otro") } }

                val isDressSelected = selectedTop?.category?.equals("Vestido", ignoreCase = true) == true
                
                CategorySlider(
                    title = "superior",
                    items = tops,
                    selectedItem = selectedTop,
                    onItemSelected = { 
                        selectedTop = if (selectedTop == it) null else it 
                        if (selectedTop?.category?.equals("Vestido", ignoreCase = true) == true) {
                            selectedBottom = null
                        }
                    }
                )

                androidx.compose.animation.AnimatedVisibility(
                    visible = !isDressSelected,
                    enter = androidx.compose.animation.expandVertically() + androidx.compose.animation.fadeIn(),
                    exit = androidx.compose.animation.shrinkVertically() + androidx.compose.animation.fadeOut()
                ) {
                    CategorySlider(
                        title = "inferior",
                        items = bottoms,
                        selectedItem = selectedBottom,
                        onItemSelected = { selectedBottom = if (selectedBottom == it) null else it }
                    )
                }

                CategorySlider(
                    title = "calzado",
                    items = shoes,
                    selectedItem = selectedShoes,
                    onItemSelected = { selectedShoes = if (selectedShoes == it) null else it }
                )

                if (headwear.isNotEmpty()) {
                    CategorySlider(
                        title = "cabeza",
                        items = headwear,
                        selectedItem = selectedHead,
                        onItemSelected = { selectedHead = if (selectedHead == it) null else it }
                    )
                }

                if (accessories.isNotEmpty()) {
                    CategorySlider(
                        title = "accesorios",
                        items = accessories,
                        selectedItems = selectedAccessories,
                        onItemSelected = { clickedItem ->
                            val currentCategory = clickedItem.category
                            val isAlreadySelected = selectedAccessories.contains(clickedItem)
                            
                            if (isAlreadySelected) {
                                selectedAccessories = selectedAccessories.filter { it.id != clickedItem.id }
                            } else {
                                val exclusiveBags = listOf("bolso", "mochila")
                                val isBag = currentCategory?.lowercase() in exclusiveBags
                                
                                val filtered = selectedAccessories.filter { existing ->
                                    val sameCategory = existing.category.equals(currentCategory, ignoreCase = true)
                                    val bothAreBags = isBag && existing.category?.lowercase() in exclusiveBags
                                    !(sameCategory || bothAreBags)
                                }
                                selectedAccessories = filtered + clickedItem
                            }
                        }
                    )
                }
                
                Spacer(modifier = Modifier.height(120.dp))
            }

            if (android.os.Build.VERSION.SDK_INT >= 31) {
                com.pdm0126.outfix.ui.ProgressiveBlurLayer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp)
                        .clipToBounds(),
                    contentLayer = slidersLayer,
                    maxBlur = 60f,
                    fadeStartFraction = 0.4f,
                    fadeEndFraction = 1.0f
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                OutfitPreview(
                    top = selectedTop,
                    bottom = selectedBottom,
                    shoes = selectedShoes,
                    head = selectedHead,
                    accessories = selectedAccessories
                )

                Spacer(modifier = Modifier.height(24.dp))

                var buttonOffset by remember { mutableStateOf(Offset.Zero) }
                var isPressedInstant by remember { mutableStateOf(false) }
                val hapticFeedback = androidx.compose.ui.platform.LocalHapticFeedback.current
                val buttonScale by androidx.compose.animation.core.animateFloatAsState(
                    targetValue = if (isPressedInstant) 1.08f else 1f,
                    animationSpec = androidx.compose.animation.core.spring(
                        dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
                        stiffness = 400f
                    ),
                    label = "glassButtonScale"
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .height(50.dp)
                        .width(220.dp)
                        .onGloballyPositioned { coords -> 
                            if (rootCoords != null && rootCoords!!.isAttached && coords.isAttached) {
                                try {
                                    buttonOffset = rootCoords!!.localPositionOf(coords, Offset.Zero)
                                } catch (e: Exception) {
                                }
                            }
                        }
                        .graphicsLayer {
                            scaleX = buttonScale
                            scaleY = buttonScale
                        }
                        .pointerInput(Unit) {
                            awaitEachGesture {
                                awaitFirstDown(requireUnconsumed = false)
                                isPressedInstant = true
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                waitForUpOrCancellation()
                                isPressedInstant = false
                            }
                        }
                        .clickable(
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                            indication = null
                        ) { /* TODO: Show success message or save outfit */ }
                        .clip(RoundedCornerShape(percent = 50)),
                    contentAlignment = Alignment.Center
                ) {
                    if (android.os.Build.VERSION.SDK_INT >= 31) {
                        androidx.compose.foundation.Canvas(
                            modifier = Modifier.fillMaxSize().liquidGlass(
                                blur = 12f,
                                saturation = 1.2f,
                                refraction = 0.5f,
                                curve = 0.5f,
                                dispersion = 0.15f,
                                normalizedRadius = 0.5f
                            )
                        ) {
                            scale(
                                scaleX = 1f / buttonScale,
                                scaleY = 1f / buttonScale,
                                pivot = center
                            ) {
                                translate(left = -buttonOffset.x, top = -buttonOffset.y) {
                                    drawLayer(slidersLayer)
                                }
                            }
                        }
                    } else {
                        Box(modifier = Modifier.fillMaxSize().background(LimeGreen))
                    }
                    Box(modifier = Modifier.fillMaxSize().background(LimeGreen.copy(alpha = 0.40f)))
                    
                    Row(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Guardar outfit", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(imageVector = Icons.Rounded.Add, contentDescription = "Add", tint = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun OutfitPreview(
    top: GarmentResponse?,
    bottom: GarmentResponse?,
    shoes: GarmentResponse?,
    head: GarmentResponse?,
    accessories: List<GarmentResponse> = emptyList()
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .height(290.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFFF6EEE6))
            .border(1.dp, Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val defaultSkin = Color(0xFFEAC3AB)
        
        val topColor = remember(top?.colorHex) { top?.colorHex?.let { parseColorHex(it) } ?: defaultSkin }
        val hasBottom = bottom != null
        val bottomColor = remember(bottom?.colorHex) { bottom?.colorHex?.let { parseColorHex(it) } ?: defaultSkin }
        val shoesColor = remember(shoes?.colorHex) { shoes?.colorHex?.let { parseColorHex(it) } }
        val hatColor = remember(head?.colorHex) { head?.colorHex?.let { parseColorHex(it) } ?: defaultSkin }

        val headCategories = remember { listOf("Gafas", "Joyería") }
        val topCategories = remember { listOf("Bufanda", "Corbata") }
        val bottomCategories = remember { listOf("Reloj", "Cinturón") }
        val shoesCategories = remember { listOf("Bolso", "Mochila", "Otro") }
        
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val isDress = top?.category?.equals("Vestido", ignoreCase = true) == true
            
            Column(
                modifier = Modifier
                    .width(76.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .border(1.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            ) {
                UnifiedMainSlot("Cabeza", head, Modifier.weight(1f))
                
                if (isDress) {
                    UnifiedMainSlot("Vestido", top, Modifier.weight(2f))
                } else {
                    UnifiedMainSlot("Superior", top, Modifier.weight(1f))
                    UnifiedMainSlot("Inferior", bottom, Modifier.weight(1f))
                }
                
                UnifiedMainSlot("Calzado", shoes, Modifier.weight(1f))
            }
            
            Column(modifier = Modifier.fillMaxHeight()) {
                val headAccs = remember(accessories) { accessories.filter { it.category in headCategories } }
                val topAccs = remember(accessories) { accessories.filter { it.category in topCategories } }
                val bottomAccs = remember(accessories) { accessories.filter { it.category in bottomCategories } }
                val shoesAccs = remember(accessories) { accessories.filter { it.category in shoesCategories } }

                UnifiedAccessoryRow(headAccs, Modifier.weight(1f))
                
                if (isDress) {
                    Column(modifier = Modifier.weight(2f)) {
                        UnifiedAccessoryRow(topAccs, Modifier.weight(1f))
                        UnifiedAccessoryRow(bottomAccs, Modifier.weight(1f))
                    }
                } else {
                    UnifiedAccessoryRow(topAccs, Modifier.weight(1f))
                    UnifiedAccessoryRow(bottomAccs, Modifier.weight(1f))
                }
                
                UnifiedAccessoryRow(shoesAccs, Modifier.weight(1f))
            }
        }

        Box(modifier = Modifier.fillMaxHeight().aspectRatio(0.5f)) {
            Canvas(modifier = Modifier.fillMaxSize().clipToBounds()) {
            val u = size.height / 340f
            val cx = size.width - 73f * u
            val cy = size.height / 2
            
            val headRadius = 26f * u
            val headCenter = Offset(cx, cy - 90f * u)
            
            val torsoTop = headCenter.y + headRadius + 8f * u
            val torsoWidth = 56f * u
            val torsoHeight = 75f * u
            val shoulderRadius = 18f * u
            
            val armWidth = 22f * u
            val armHeight = 85f * u
            val armRadius = armWidth / 2
            
            val upperWidth = torsoWidth + 2 * armWidth
            
            val legWidth = torsoWidth / 2
            val legHeight = 100f * u
            val legRadius = legWidth / 2
            
            drawCircle(
                color = defaultSkin,
                radius = headRadius,
                center = headCenter
            )
            
            val gafas = accessories.find { it.category.equals("Gafas", ignoreCase = true) }
            if (gafas != null) {
                val accColor = gafas.colorHex?.let { parseColorHex(it) } ?: Color.Black
                val glassWidth = 14f * u
                val glassHeight = 10f * u
                val glassY = headCenter.y - 4f * u
                drawRoundRect(
                    color = accColor,
                    topLeft = Offset(headCenter.x - glassWidth - 2f * u, glassY),
                    size = Size(glassWidth, glassHeight),
                    cornerRadius = CornerRadius(3f * u, 3f * u)
                )
                drawRoundRect(
                    color = accColor,
                    topLeft = Offset(headCenter.x + 2f * u, glassY),
                    size = Size(glassWidth, glassHeight),
                    cornerRadius = CornerRadius(3f * u, 3f * u)
                )
                drawRect(
                    color = accColor,
                    topLeft = Offset(headCenter.x - 2f * u, glassY + glassHeight / 2 - 1f * u),
                    size = Size(4f * u, 2f * u)
                )
            }
            
            drawArc(
                color = hatColor,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(headCenter.x - headRadius, headCenter.y - headRadius),
                size = Size(headRadius * 2, headRadius * 2)
            )
            
            val isGorra = head?.category?.equals("Gorra", ignoreCase = true) == true
            val isSombrero = head?.category?.equals("Sombrero", ignoreCase = true) == true
            
            if (isGorra || isSombrero) {
                drawRoundRect(
                    color = hatColor,
                    topLeft = Offset(headCenter.x, headCenter.y - 8f * u),
                    size = Size(headRadius + 15f * u, 8f * u),
                    cornerRadius = CornerRadius(4f * u, 4f * u)
                )
            }
            if (isSombrero) {
                drawRoundRect(
                    color = hatColor,
                    topLeft = Offset(headCenter.x - headRadius - 15f * u, headCenter.y - 8f * u),
                    size = Size(headRadius + 15f * u, 8f * u),
                    cornerRadius = CornerRadius(4f * u, 4f * u)
                )
            }
            
            val upperPath = androidx.compose.ui.graphics.Path().apply {
                moveTo(cx - upperWidth / 2 + shoulderRadius, torsoTop)
                lineTo(cx + upperWidth / 2 - shoulderRadius, torsoTop)
                
                arcTo(
                    rect = androidx.compose.ui.geometry.Rect(
                        left = cx + upperWidth / 2 - 2 * shoulderRadius,
                        top = torsoTop,
                        right = cx + upperWidth / 2,
                        bottom = torsoTop + 2 * shoulderRadius
                    ),
                    startAngleDegrees = 270f,
                    sweepAngleDegrees = 90f,
                    forceMoveTo = false
                )
                
                lineTo(cx + upperWidth / 2, torsoTop + armHeight - armRadius)
                
                arcTo(
                    rect = androidx.compose.ui.geometry.Rect(
                        left = cx + torsoWidth / 2,
                        top = torsoTop + armHeight - 2 * armRadius,
                        right = cx + upperWidth / 2,
                        bottom = torsoTop + armHeight
                    ),
                    startAngleDegrees = 0f,
                    sweepAngleDegrees = 180f,
                    forceMoveTo = false
                )
                
                lineTo(cx + torsoWidth / 2, torsoTop + torsoHeight)
                
                lineTo(cx - torsoWidth / 2, torsoTop + torsoHeight)
                
                lineTo(cx - torsoWidth / 2, torsoTop + armHeight - armRadius)
                
                arcTo(
                    rect = androidx.compose.ui.geometry.Rect(
                        left = cx - upperWidth / 2,
                        top = torsoTop + armHeight - 2 * armRadius,
                        right = cx - torsoWidth / 2,
                        bottom = torsoTop + armHeight
                    ),
                    startAngleDegrees = 0f,
                    sweepAngleDegrees = 180f,
                    forceMoveTo = false
                )
                
                lineTo(cx - upperWidth / 2, torsoTop + shoulderRadius)
                
                arcTo(
                    rect = androidx.compose.ui.geometry.Rect(
                        left = cx - upperWidth / 2,
                        top = torsoTop,
                        right = cx - upperWidth / 2 + 2 * shoulderRadius,
                        bottom = torsoTop + 2 * shoulderRadius
                    ),
                    startAngleDegrees = 180f,
                    sweepAngleDegrees = 90f,
                    forceMoveTo = false
                )
                
                close()
            }
            drawPath(path = upperPath, color = topColor)
            
            val isFormalTop = top?.style?.equals("Formal", ignoreCase = true) == true
            val isLongSleeveCategory = top?.category in listOf("Chaqueta", "Abrigo", "Suéter", "Camisa")
            val isLongSleeve = isFormalTop || isLongSleeveCategory
            val isDressTop = top?.category?.equals("Vestido", ignoreCase = true) == true
            
            val handHeight = if (isDressTop) armHeight else if (isLongSleeve) 16f * u else armHeight - 30f * u
            val handTop = torsoTop + armHeight - handHeight
            val topCorner = if (isDressTop) CornerRadius(shoulderRadius, shoulderRadius) else CornerRadius.Zero
            
            val leftHandPath = androidx.compose.ui.graphics.Path().apply {
                addRoundRect(
                    androidx.compose.ui.geometry.RoundRect(
                        left = cx - upperWidth / 2,
                        top = handTop,
                        right = cx - torsoWidth / 2,
                        bottom = handTop + handHeight,
                        topLeftCornerRadius = topCorner,
                        topRightCornerRadius = CornerRadius.Zero,
                        bottomLeftCornerRadius = CornerRadius(armRadius, armRadius),
                        bottomRightCornerRadius = CornerRadius(armRadius, armRadius)
                    )
                )
            }
            drawPath(path = leftHandPath, color = defaultSkin)
            
            val rightHandPath = androidx.compose.ui.graphics.Path().apply {
                addRoundRect(
                    androidx.compose.ui.geometry.RoundRect(
                        left = cx + torsoWidth / 2,
                        top = handTop,
                        right = cx + upperWidth / 2,
                        bottom = handTop + handHeight,
                        topLeftCornerRadius = CornerRadius.Zero,
                        topRightCornerRadius = topCorner,
                        bottomLeftCornerRadius = CornerRadius(armRadius, armRadius),
                        bottomRightCornerRadius = CornerRadius(armRadius, armRadius)
                    )
                )
            }
            drawPath(path = rightHandPath, color = defaultSkin)
            
            val legTop = torsoTop + torsoHeight
            
            val isDress = top?.category?.equals("Vestido", ignoreCase = true) == true
            val isSkirt = bottom?.category?.equals("Falda", ignoreCase = true) == true
            val drawSkirt = isDress || (isSkirt && hasBottom)
            
            val legColor = if (drawSkirt) defaultSkin else bottomColor
            
            val leftLegPath = androidx.compose.ui.graphics.Path().apply {
                addRoundRect(
                    androidx.compose.ui.geometry.RoundRect(
                        left = cx - legWidth,
                        top = legTop,
                        right = cx,
                        bottom = legTop + legHeight,
                        topLeftCornerRadius = CornerRadius.Zero,
                        topRightCornerRadius = CornerRadius.Zero,
                        bottomLeftCornerRadius = CornerRadius(legRadius, legRadius),
                        bottomRightCornerRadius = CornerRadius(legRadius, legRadius)
                    )
                )
            }
            drawPath(path = leftLegPath, color = legColor)
            
            val rightLegPath = androidx.compose.ui.graphics.Path().apply {
                addRoundRect(
                    androidx.compose.ui.geometry.RoundRect(
                        left = cx,
                        top = legTop,
                        right = cx + legWidth,
                        bottom = legTop + legHeight,
                        topLeftCornerRadius = CornerRadius.Zero,
                        topRightCornerRadius = CornerRadius.Zero,
                        bottomLeftCornerRadius = CornerRadius(legRadius, legRadius),
                        bottomRightCornerRadius = CornerRadius(legRadius, legRadius)
                    )
                )
            }
            drawPath(path = rightLegPath, color = legColor)
            
            val isShort = bottom?.category?.equals("Short", ignoreCase = true) == true
            if (isShort && !drawSkirt) {
                val legSkinHeight = legHeight - 35f * u
                val legSkinTop = legTop + legHeight - legSkinHeight
                
                val leftLegSkinPath = androidx.compose.ui.graphics.Path().apply {
                    addRoundRect(
                        androidx.compose.ui.geometry.RoundRect(
                            left = cx - legWidth,
                            top = legSkinTop,
                            right = cx,
                            bottom = legSkinTop + legSkinHeight,
                            topLeftCornerRadius = CornerRadius.Zero,
                            topRightCornerRadius = CornerRadius.Zero,
                            bottomLeftCornerRadius = CornerRadius(legRadius, legRadius),
                            bottomRightCornerRadius = CornerRadius(legRadius, legRadius)
                        )
                    )
                }
                drawPath(path = leftLegSkinPath, color = defaultSkin)
                
                val rightLegSkinPath = androidx.compose.ui.graphics.Path().apply {
                    addRoundRect(
                        androidx.compose.ui.geometry.RoundRect(
                            left = cx,
                            top = legSkinTop,
                            right = cx + legWidth,
                            bottom = legSkinTop + legSkinHeight,
                            topLeftCornerRadius = CornerRadius.Zero,
                            topRightCornerRadius = CornerRadius.Zero,
                            bottomLeftCornerRadius = CornerRadius(legRadius, legRadius),
                            bottomRightCornerRadius = CornerRadius(legRadius, legRadius)
                        )
                    )
                }
                drawPath(path = rightLegSkinPath, color = defaultSkin)
            }
            
            if (!hasBottom && !isDress) {
                val briefPath = androidx.compose.ui.graphics.Path().apply {
                    moveTo(cx - legWidth, legTop - 10f * u)
                    lineTo(cx + legWidth, legTop - 10f * u)
                    lineTo(cx + legWidth, legTop + 3f * u)
                    lineTo(cx + 8f * u, legTop + 10f * u)
                    quadraticTo(
                        cx, legTop + 18f * u,
                        cx - 8f * u, legTop + 10f * u
                    )
                    lineTo(cx - legWidth, legTop + 3f * u)
                    close()
                }
                drawPath(path = briefPath, color = Color.White)
            }
            
            if (drawSkirt) {
                val skirtPath = androidx.compose.ui.graphics.Path().apply {
                    val topWidth = torsoWidth / 2
                    val bottomWidth = topWidth + 15f * u
                    val skirtLength = 55f * u
                    val cr = 8f * u
                    
                    moveTo(cx - topWidth, legTop)
                    lineTo(cx + topWidth, legTop)
                    
                    lineTo(cx + bottomWidth - 2.5f * u, legTop + skirtLength - cr)
                    
                    quadraticTo(
                        cx + bottomWidth + 1f * u, legTop + skirtLength,
                        cx + bottomWidth - cr, legTop + skirtLength
                    )
                    
                    lineTo(cx - bottomWidth + cr, legTop + skirtLength)
                    
                    quadraticTo(
                        cx - bottomWidth - 1f * u, legTop + skirtLength,
                        cx - bottomWidth + 2.5f * u, legTop + skirtLength - cr
                    )
                    
                    close()
                }
                val skirtColor = if (isDress) topColor else bottomColor
                drawPath(path = skirtPath, color = skirtColor)
            }
            
            if (shoesColor != null) {
                val isBoots = shoes?.category?.equals("Botas", ignoreCase = true) == true
                val shoeHeight = if (isBoots) 40f * u else 25f * u
                val shoeTop = legTop + legHeight - shoeHeight
                
                val leftShoePath = androidx.compose.ui.graphics.Path().apply {
                    addRoundRect(
                        androidx.compose.ui.geometry.RoundRect(
                            left = cx - legWidth,
                            top = shoeTop,
                            right = cx,
                            bottom = shoeTop + shoeHeight,
                            topLeftCornerRadius = CornerRadius.Zero,
                            topRightCornerRadius = CornerRadius.Zero,
                            bottomLeftCornerRadius = CornerRadius(legRadius, legRadius),
                            bottomRightCornerRadius = CornerRadius(legRadius, legRadius)
                        )
                    )
                }
                drawPath(path = leftShoePath, color = shoesColor)
                
                val rightShoePath = androidx.compose.ui.graphics.Path().apply {
                    addRoundRect(
                        androidx.compose.ui.geometry.RoundRect(
                            left = cx,
                            top = shoeTop,
                            right = cx + legWidth,
                            bottom = shoeTop + shoeHeight,
                            topLeftCornerRadius = CornerRadius.Zero,
                            topRightCornerRadius = CornerRadius.Zero,
                            bottomLeftCornerRadius = CornerRadius(legRadius, legRadius),
                            bottomRightCornerRadius = CornerRadius(legRadius, legRadius)
                        )
                    )
                }
                drawPath(path = rightShoePath, color = shoesColor)
            }
            
            val zOrder = mapOf(
                "joyería" to 1,
                "corbata" to 2,
                "bufanda" to 3
            )
            val sortedAccessories = accessories.sortedBy { zOrder[it.category?.lowercase()] ?: 10 }
            
            sortedAccessories.forEach { accessory ->
                val accCategory = accessory.category
                val accColor = accessory.colorHex?.let { parseColorHex(it) } ?: Color.Black
                
                if (accCategory?.equals("Cinturón", ignoreCase = true) == true) {
                    drawRect(
                        color = accColor,
                        topLeft = Offset(cx - torsoWidth / 2, legTop - 6f * u),
                        size = Size(torsoWidth, 6f * u)
                    )
                } else if (accCategory?.equals("Corbata", ignoreCase = true) == true) {
                    val tiePath = androidx.compose.ui.graphics.Path().apply {
                        moveTo(cx - 4f * u, torsoTop)
                        lineTo(cx + 4f * u, torsoTop)
                        lineTo(cx + 3f * u, torsoTop + 8f * u)
                        lineTo(cx - 3f * u, torsoTop + 8f * u)
                        close()
                        moveTo(cx - 3f * u, torsoTop + 8f * u)
                        lineTo(cx + 3f * u, torsoTop + 8f * u)
                        lineTo(cx + 6f * u, torsoTop + 43f * u)
                        lineTo(cx, torsoTop + 53f * u)
                        lineTo(cx - 6f * u, torsoTop + 43f * u)
                        close()
                    }
                    drawPath(path = tiePath, color = accColor)
                } else if (accCategory?.equals("Bufanda", ignoreCase = true) == true) {
                    drawRoundRect(
                        color = accColor,
                        topLeft = Offset(cx - 16f * u, torsoTop - 6f * u),
                        size = Size(32f * u, 16f * u),
                        cornerRadius = CornerRadius(8f * u, 8f * u)
                    )
                    drawRoundRect(
                        color = accColor,
                        topLeft = Offset(cx + 2f * u, torsoTop + 5f * u),
                        size = Size(10f * u, 35f * u),
                        cornerRadius = CornerRadius(4f * u, 4f * u)
                    )
                } else if (accCategory?.equals("Mochila", ignoreCase = true) == true || accCategory?.equals("Bolso", ignoreCase = true) == true) {
                    val bagWidth = 30f * u
                    val bagHeight = 50f * u
                    val bagTop = legTop + legHeight - bagHeight
                    val bagLeft = cx + legWidth + 15f * u
                    
                    drawRoundRect(
                        color = accColor,
                        topLeft = Offset(bagLeft, bagTop),
                        size = Size(bagWidth, bagHeight),
                        cornerRadius = CornerRadius(armRadius, armRadius)
                    )
                } else if (accCategory?.equals("Reloj", ignoreCase = true) == true) {
                    val wristY = torsoTop + armHeight - 20f * u
                    drawRect(
                        color = accColor,
                        topLeft = Offset(cx + torsoWidth / 2, wristY),
                        size = Size(armWidth, 6f * u)
                    )
                } else if (accCategory?.equals("Joyería", ignoreCase = true) == true) {
                    drawArc(
                        color = accColor,
                        startAngle = 0f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = Offset(cx - 10f * u, torsoTop - 8f * u),
                        size = Size(20f * u, 16f * u),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f * u)
                    )
                    drawCircle(
                        color = accColor,
                        radius = 3.5f * u,
                        center = Offset(cx, torsoTop + 8f * u)
                    )
                }
            }
            }
        }
    }
}

@Composable
fun UnifiedMainSlot(title: String, mainItem: GarmentResponse?, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        if (mainItem != null) {
            AsyncImage(
                model = mainItem.imageUrl,
                contentDescription = mainItem.name,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize().padding(4.dp)
            )
        }
    }
}

@Composable
fun UnifiedAccessoryRow(accessories: List<GarmentResponse>, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        accessories.take(2).forEach { acc ->
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .border(1.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = acc.imageUrl,
                    contentDescription = acc.name,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize().padding(2.dp)
                )
            }
        }
    }
}

@Composable
fun CategorySlider(
    title: String,
    items: List<GarmentResponse>,
    selectedItem: GarmentResponse? = null,
    selectedItems: List<GarmentResponse> = emptyList(),
    onItemSelected: (GarmentResponse) -> Unit
) {
    if (items.isEmpty()) return

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
        Row(
            modifier = Modifier.padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color.Black)
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Rounded.KeyboardArrowDown,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(Color.Black)
                    .clickable { /* TODO: Filter options */ },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.FilterList,
                    contentDescription = "Filter",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val nestedScrollConnection = remember {
            object : NestedScrollConnection {
                override fun onPostScroll(
                    consumed: Offset,
                    available: Offset,
                    source: NestedScrollSource
                ): Offset {
                    return available.copy(y = 0f)
                }
            }
        }

        LazyRow(
            modifier = Modifier.nestedScroll(nestedScrollConnection),
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)
        ) {
            items(items) { garment ->
                GarmentCard(
                    garment = garment,
                    isSelected = garment == selectedItem || selectedItems.contains(garment),
                    onClick = { onItemSelected(garment) }
                )
            }
        }
    }
}


@Composable
fun GarmentCard(
    garment: GarmentResponse,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) Color.White else Color.Transparent
    
    Box(
        modifier = Modifier
            .size(90.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF6EEE6))
            .clickable { onClick() }
            .then(
                if (isSelected) Modifier.border(3.dp, borderColor, RoundedCornerShape(16.dp))
                else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = garment.imageUrl,
            contentDescription = garment.name,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp))
        )
    }
}

fun parseColorHex(hexStr: String): Color {
    return try {
        val colorInt = android.graphics.Color.parseColor(hexStr)
        Color(colorInt)
    } catch (e: Exception) {
        Color.Gray
    }
}
