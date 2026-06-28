package com.pdm0126.outfix.screens.closet

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material.icons.outlined.Save
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
import androidx.compose.ui.zIndex
import androidx.compose.animation.animateContentSize
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.blur
import androidx.compose.ui.layout.boundsInRoot

object ClosetOverlayState {
    var detailGarment by androidx.compose.runtime.mutableStateOf<com.pdm0126.outfix.data.api.dto.GarmentResponse?>(null)
    var detailGarmentBounds by androidx.compose.runtime.mutableStateOf<androidx.compose.ui.geometry.Rect?>(null)
    var isOverlayActive by androidx.compose.runtime.mutableStateOf(false)
    var plannerEditDay by androidx.compose.runtime.mutableStateOf<String?>(null)
    var hasLoadedPlannerDay by androidx.compose.runtime.mutableStateOf(false)
    // Day detail overlay
    var detailDayInfo by androidx.compose.runtime.mutableStateOf<com.pdm0126.outfix.data.mock.DayInfo?>(null)
    var detailDayBounds by androidx.compose.runtime.mutableStateOf<androidx.compose.ui.geometry.Rect?>(null)
    var isDayOverlayActive by androidx.compose.runtime.mutableStateOf(false)
}

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

    androidx.compose.runtime.LaunchedEffect(ClosetOverlayState.plannerEditDay) {
        val plannerDay = ClosetOverlayState.plannerEditDay
        if (plannerDay != null && !ClosetOverlayState.hasLoadedPlannerDay) {
            val dayInfo = com.pdm0126.outfix.data.mock.MockDatabase.plannerDays.find { it.day == plannerDay }
            if (dayInfo != null) {
                selectedTop = dayInfo.topGarment
                selectedBottom = dayInfo.bottomGarment
                selectedShoes = dayInfo.shoesGarment
                selectedHead = dayInfo.hatGarment
                selectedAccessories = dayInfo.accessories
                ClosetOverlayState.hasLoadedPlannerDay = true
            }
        }
    }

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
        
        var isDropdownExpanded by remember { mutableStateOf(false) }
        var dropdownSelectedTitle by remember { mutableStateOf("") }
        var dropdownButtonCoords by remember { mutableStateOf<androidx.compose.ui.layout.LayoutCoordinates?>(null) }
        var dropdownItems by remember { mutableStateOf<List<String>>(emptyList()) }
        var dropdownSelected by remember { mutableStateOf("") }
        var dropdownOnSelect by remember { mutableStateOf<(String) -> Unit>({}) }

        val handleOpenDropdown: (String, androidx.compose.ui.layout.LayoutCoordinates, List<String>, String, (String) -> Unit) -> Unit =
            { title, coords, items, selected, onSelect ->
                dropdownSelectedTitle = title
                dropdownButtonCoords = coords
                dropdownItems = items
                dropdownSelected = selected
                dropdownOnSelect = {
                    onSelect(it)
                    isDropdownExpanded = false
                }
                isDropdownExpanded = true
            }

        val rootLayer = rememberGraphicsLayer()
        
        val tops = remember(garments) { garments.filter { it.category in listOf("Camiseta", "Camisa", "Blusa", "Top", "Suéter", "Chaqueta", "Abrigo", "Vestido") } }
        val bottoms = remember(garments) { garments.filter { it.category in listOf("Jeans", "Pantalón", "Short", "Falda", "Vestido") } }
        val shoes = remember(garments) { garments.filter { it.category in listOf("Zapatillas", "Botas", "Zapatos") } }
        val headwear = remember(garments) { garments.filter { it.category in listOf("Gorra", "Sombrero", "Gorro") } }
        val accessories = remember(garments) { garments.filter { it.category in listOf("Bolso", "Mochila", "Reloj", "Gafas", "Cinturón", "Corbata", "Bufanda", "Joyería", "Accesorio", "Otros", "Otro") } }
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned { rootCoords = it }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawWithContent {
                        rootLayer.record {
                            this@drawWithContent.drawContent()
                        }
                        drawLayer(rootLayer)
                    }
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

                val isDressSelected = selectedTop?.category?.equals("Vestido", ignoreCase = true) == true
                
                CategorySlider(
                    title = "superior",
                    items = tops,
                    selectedItem = selectedTop,
                    isDropdownExpanded = isDropdownExpanded && dropdownSelectedTitle == "superior",
                    onItemSelected = { 
                        selectedTop = if (selectedTop == it) null else it 
                        if (selectedTop?.category?.equals("Vestido", ignoreCase = true) == true) {
                            selectedBottom = null
                        }
                    },
                    onItemLongClick = { garment, bounds -> 
                        ClosetOverlayState.detailGarment = garment
                        ClosetOverlayState.detailGarmentBounds = bounds
                        ClosetOverlayState.isOverlayActive = true
                    },
                    onOpenDropdown = handleOpenDropdown
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
                        isDropdownExpanded = isDropdownExpanded && dropdownSelectedTitle == "inferior",
                        onItemSelected = { selectedBottom = if (selectedBottom == it) null else it },
                        onItemLongClick = { garment, bounds -> 
                        ClosetOverlayState.detailGarment = garment
                        ClosetOverlayState.detailGarmentBounds = bounds
                        ClosetOverlayState.isOverlayActive = true
                    },
                        onOpenDropdown = handleOpenDropdown
                    )
                }

                CategorySlider(
                    title = "calzado",
                    items = shoes,
                    selectedItem = selectedShoes,
                    isDropdownExpanded = isDropdownExpanded && dropdownSelectedTitle == "calzado",
                    onItemSelected = { selectedShoes = if (selectedShoes == it) null else it },
                    onItemLongClick = { garment, bounds -> 
                        ClosetOverlayState.detailGarment = garment
                        ClosetOverlayState.detailGarmentBounds = bounds
                        ClosetOverlayState.isOverlayActive = true
                    },
                    onOpenDropdown = handleOpenDropdown
                )

                if (headwear.isNotEmpty()) {
                    CategorySlider(
                        title = "cabeza",
                        items = headwear,
                        selectedItem = selectedHead,
                        isDropdownExpanded = isDropdownExpanded && dropdownSelectedTitle == "cabeza",
                        onItemSelected = { selectedHead = if (selectedHead == it) null else it },
                        onItemLongClick = { garment, bounds -> 
                        ClosetOverlayState.detailGarment = garment
                        ClosetOverlayState.detailGarmentBounds = bounds
                        ClosetOverlayState.isOverlayActive = true
                    },
                        onOpenDropdown = handleOpenDropdown
                    )
                }

                if (accessories.isNotEmpty()) {
                    CategorySlider(
                        title = "accesorios",
                        items = accessories,
                        selectedItems = selectedAccessories,
                        isDropdownExpanded = isDropdownExpanded && dropdownSelectedTitle == "accesorios",
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
                        },
                        onItemLongClick = { garment, bounds -> 
                            ClosetOverlayState.detailGarment = garment
                            ClosetOverlayState.detailGarmentBounds = bounds
                            ClosetOverlayState.isOverlayActive = true
                        },
                        onOpenDropdown = handleOpenDropdown
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

                val plannerDay = ClosetOverlayState.plannerEditDay
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = plannerDay != null,
                        enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.slideInHorizontally(initialOffsetX = { it }) + androidx.compose.animation.expandHorizontally(expandFrom = Alignment.End, clip = false),
                        exit = androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(durationMillis = 150, delayMillis = 200)) + androidx.compose.animation.slideOutHorizontally(targetOffsetX = { it * 2 }) + androidx.compose.animation.shrinkHorizontally(shrinkTowards = Alignment.End, clip = false)
                    ) {
                        var randomButtonOffset by remember { mutableStateOf(Offset.Zero) }
                        var isRandomPressedInstant by remember { mutableStateOf(false) }
                        val randomScale by androidx.compose.animation.core.animateFloatAsState(
                            targetValue = if (isRandomPressedInstant) 1.08f else 1f,
                            animationSpec = androidx.compose.animation.core.spring(
                                dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
                                stiffness = 400f
                            ),
                            label = "randomButtonScale"
                        )
                        
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .onGloballyPositioned { coords -> 
                                    if (rootCoords != null && rootCoords!!.isAttached && coords.isAttached) {
                                        try {
                                            randomButtonOffset = rootCoords!!.localPositionOf(coords, Offset.Zero)
                                        } catch (e: Exception) {}
                                    }
                                }
                                .graphicsLayer {
                                    scaleX = randomScale
                                    scaleY = randomScale
                                }
                                .pointerInput(Unit) {
                                    awaitEachGesture {
                                        awaitFirstDown(requireUnconsumed = false)
                                        isRandomPressedInstant = true
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                        waitForUpOrCancellation()
                                        isRandomPressedInstant = false
                                    }
                                }
                                .clickable(
                                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                    indication = null
                                ) {
                                    val availableTops = tops.filter { it.status != "IN_WASH" }.ifEmpty { tops }
                                    val availableBottoms = bottoms.filter { it.status != "IN_WASH" }.ifEmpty { bottoms }
                                    val availableShoes = shoes.filter { it.status != "IN_WASH" }.ifEmpty { shoes }
                                    
                                    val randomTop = availableTops.randomOrNull()
                                    if (randomTop != null) {
                                        selectedTop = randomTop
                                        if (randomTop.category.equals("Vestido", ignoreCase = true)) {
                                            selectedBottom = null
                                        } else {
                                            val matchingBottoms = availableBottoms.filter { it.style == randomTop.style }
                                            selectedBottom = if (matchingBottoms.isNotEmpty()) matchingBottoms.random() else availableBottoms.randomOrNull()
                                        }
                                        
                                        val matchingShoes = availableShoes.filter { it.style == randomTop.style }
                                        selectedShoes = if (matchingShoes.isNotEmpty()) matchingShoes.random() else availableShoes.randomOrNull()
                                    }
                                }
                                .clip(androidx.compose.foundation.shape.CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (android.os.Build.VERSION.SDK_INT >= 31) {
                                androidx.compose.foundation.Canvas(
                                    modifier = Modifier.matchParentSize().liquidGlass(
                                        blur = 12f,
                                        saturation = 1.2f,
                                        refraction = 0.5f,
                                        curve = 0.5f,
                                        dispersion = 0.15f,
                                        normalizedRadius = 0.5f
                                    )
                                ) {
                                    scale(
                                        scaleX = 1f / randomScale,
                                        scaleY = 1f / randomScale,
                                        pivot = center
                                    ) {
                                        translate(left = -randomButtonOffset.x, top = -randomButtonOffset.y) {
                                            drawLayer(slidersLayer)
                                        }
                                    }
                                }
                            } else {
                                Box(modifier = Modifier.matchParentSize().background(Color(0xFF89CFF0)))
                            }
                            Box(modifier = Modifier.matchParentSize().background(Color(0xFF89CFF0).copy(alpha = 0.50f)))
                            Icon(imageVector = androidx.compose.material.icons.Icons.Rounded.Shuffle, contentDescription = "Randomize", tint = Color.White)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .height(50.dp)
                            .zIndex(1f)
                            .graphicsLayer {
                                scaleX = buttonScale
                                scaleY = buttonScale
                            }
                            .onGloballyPositioned { coords -> 
                                if (rootCoords != null && rootCoords!!.isAttached && coords.isAttached) {
                                    try {
                                        buttonOffset = rootCoords!!.localPositionOf(coords, Offset.Zero)
                                    } catch (e: Exception) {}
                                }
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
                            ) {
                                if (plannerDay != null) {
                                    val dayInfo = com.pdm0126.outfix.data.mock.MockDatabase.plannerDays.find { it.day == plannerDay }
                                    if (dayInfo != null) {
                                        fun parseColorOrDefault(hex: String?, default: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color.Black): androidx.compose.ui.graphics.Color {
                                            return try {
                                                if (hex.isNullOrBlank()) default else androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(hex))
                                            } catch(e: Exception) { default }
                                        }
                                        
                                        dayInfo.topColor = parseColorOrDefault(selectedTop?.colorHex, Color.White)
                                        dayInfo.bottomColor = parseColorOrDefault(selectedBottom?.colorHex, Color.Black)
                                        dayInfo.shoesColor = parseColorOrDefault(selectedShoes?.colorHex, Color.Black)
                                        dayInfo.hatColor = selectedHead?.colorHex?.let { parseColorOrDefault(it) }
                                        
                                        dayInfo.topGarment = selectedTop
                                        dayInfo.bottomGarment = selectedBottom
                                        dayInfo.shoesGarment = selectedShoes
                                        dayInfo.hatGarment = selectedHead
                                        dayInfo.accessories = selectedAccessories.toList()
                                    }
                                    selectedTop = null
                                    selectedBottom = null
                                    selectedShoes = null
                                    selectedHead = null
                                    selectedAccessories = emptyList()
                                    ClosetOverlayState.plannerEditDay = null
                                    com.pdm0126.outfix.ui.GlobalNavigationState.requestedTab = com.pdm0126.outfix.ui.OutFixScreen.WeeklyPlanner
                                } else {
                                    /* TODO: Show success message or save outfit */ 
                                }
                            }
                            .clip(RoundedCornerShape(percent = 50)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (android.os.Build.VERSION.SDK_INT >= 31) {
                            androidx.compose.foundation.Canvas(
                                modifier = Modifier.matchParentSize().liquidGlass(
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
                            Box(modifier = Modifier.matchParentSize().background(LimeGreen))
                        }
                        Box(modifier = Modifier.matchParentSize().background(LimeGreen.copy(alpha = 0.40f)))
                        
                        Row(
                            modifier = Modifier
                                .animateContentSize(animationSpec = androidx.compose.animation.core.spring(dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy, stiffness = 400f))
                                .fillMaxHeight()
                                .padding(horizontal = 24.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            val currentDayOfWeek = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_WEEK)
                            val isToday = plannerDay != null && com.pdm0126.outfix.data.mock.MockDatabase.plannerDays.find { it.day == plannerDay }?.calendarDay == currentDayOfWeek
                            
                            val buttonText = if (plannerDay != null) {
                                if (isToday) "Guardar hoy" else "Guardar $plannerDay"
                            } else "Guardar"
                            
                            val buttonIcon = if (plannerDay != null) androidx.compose.material.icons.Icons.Outlined.Save else Icons.Rounded.Add

                            Text(buttonText, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.width(12.dp))
                            Icon(imageVector = buttonIcon, contentDescription = "Save", tint = Color.White)
                        }
                    }

                    androidx.compose.animation.AnimatedVisibility(
                        visible = plannerDay != null,
                        enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.slideInHorizontally(initialOffsetX = { -it }) + androidx.compose.animation.expandHorizontally(expandFrom = Alignment.Start, clip = false),
                        exit = androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(durationMillis = 150, delayMillis = 200)) + androidx.compose.animation.slideOutHorizontally(targetOffsetX = { -it * 2 }) + androidx.compose.animation.shrinkHorizontally(shrinkTowards = Alignment.Start, clip = false)
                    ) {
                        var cancelButtonOffset by remember { mutableStateOf(Offset.Zero) }
                        var isCancelPressedInstant by remember { mutableStateOf(false) }
                        val cancelScale by androidx.compose.animation.core.animateFloatAsState(
                            targetValue = if (isCancelPressedInstant) 1.08f else 1f,
                            animationSpec = androidx.compose.animation.core.spring(
                                dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
                                stiffness = 400f
                            ),
                            label = "cancelButtonScale"
                        )
                        
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .onGloballyPositioned { coords -> 
                                    if (rootCoords != null && rootCoords!!.isAttached && coords.isAttached) {
                                        try {
                                            cancelButtonOffset = rootCoords!!.localPositionOf(coords, Offset.Zero)
                                        } catch (e: Exception) {}
                                    }
                                }
                                .graphicsLayer {
                                    scaleX = cancelScale
                                    scaleY = cancelScale
                                }
                                .pointerInput(Unit) {
                                    awaitEachGesture {
                                        awaitFirstDown(requireUnconsumed = false)
                                        isCancelPressedInstant = true
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                        waitForUpOrCancellation()
                                        isCancelPressedInstant = false
                                    }
                                }
                                .clickable(
                                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                    indication = null
                                ) {
                                    selectedTop = null
                                    selectedBottom = null
                                    selectedShoes = null
                                    selectedHead = null
                                    selectedAccessories = emptyList()
                                    ClosetOverlayState.plannerEditDay = null
                                }
                                .clip(androidx.compose.foundation.shape.CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (android.os.Build.VERSION.SDK_INT >= 31) {
                                androidx.compose.foundation.Canvas(
                                    modifier = Modifier.matchParentSize().liquidGlass(
                                        blur = 12f,
                                        saturation = 1.2f,
                                        refraction = 0.5f,
                                        curve = 0.5f,
                                        dispersion = 0.15f,
                                        normalizedRadius = 0.5f
                                    )
                                ) {
                                    scale(
                                        scaleX = 1f / cancelScale,
                                        scaleY = 1f / cancelScale,
                                        pivot = center
                                    ) {
                                        translate(left = -cancelButtonOffset.x, top = -cancelButtonOffset.y) {
                                            drawLayer(slidersLayer)
                                        }
                                    }
                                }
                            } else {
                                Box(modifier = Modifier.matchParentSize().background(Color.Red))
                            }
                            Box(modifier = Modifier.matchParentSize().background(Color.Red.copy(alpha = 0.40f)))
                            Icon(imageVector = Icons.Rounded.Close, contentDescription = "Cancel", tint = Color.White)
                        }
                    }
                }
                           Spacer(modifier = Modifier.height(24.dp))
            }
            
            }

            com.pdm0126.outfix.ui.LiquidWheelPickerOverlay(
                isExpanded = isDropdownExpanded,
                onDismissRequest = { isDropdownExpanded = false },
                buttonCoords = dropdownButtonCoords,
                targetWidth = 160.dp,
                backgroundLayer = rootLayer,
                screenCoords = rootCoords,
                items = dropdownItems,
                selectedItem = dropdownSelected,
                onItemSelected = dropdownOnSelect
            )
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
            com.pdm0126.outfix.ui.CharacterWithClothes(
                top = top,
                bottom = bottom,
                shoes = shoes,
                head = head,
                accessories = accessories,
                modifier = Modifier.fillMaxSize()
            )
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
    androidx.compose.animation.AnimatedContent(
        targetState = accessories.take(2),
        modifier = modifier,
        transitionSpec = {
            androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.spring(stiffness = 300f)) togetherWith 
            androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.spring(stiffness = 300f)) using
            androidx.compose.animation.SizeTransform(
                clip = false,
                sizeAnimationSpec = { _, _ -> androidx.compose.animation.core.spring(stiffness = 300f, dampingRatio = androidx.compose.animation.core.Spring.DampingRatioNoBouncy) }
            )
        },
        label = "AccessoryRowAnimation"
    ) { currentAccessories ->
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            currentAccessories.forEach { acc ->
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
}

@Composable
fun CategorySlider(
    title: String,
    items: List<GarmentResponse>,
    selectedItem: GarmentResponse? = null,
    selectedItems: List<GarmentResponse> = emptyList(),
    isDropdownExpanded: Boolean = false,
    onItemSelected: (GarmentResponse) -> Unit,
    onItemLongClick: ((GarmentResponse, androidx.compose.ui.geometry.Rect) -> Unit)? = null,
    onOpenDropdown: (String, androidx.compose.ui.layout.LayoutCoordinates, List<String>, String, (String) -> Unit) -> Unit
) {
    if (items.isEmpty()) return

    val titleCapitalized = title.replaceFirstChar { it.uppercase() }

    var selectedCategoryFilter by remember { mutableStateOf(titleCapitalized) }
    var buttonCoords by remember { mutableStateOf<androidx.compose.ui.layout.LayoutCoordinates?>(null) }
    
    val availableCategories = remember(items) { 
        (listOf(titleCapitalized) + items.mapNotNull { it.category }.map { it.lowercase().replaceFirstChar { c -> c.uppercase() } }.sorted()).distinct()
    }
    
    val filteredItems = remember(items, selectedCategoryFilter) {
        if (selectedCategoryFilter == titleCapitalized) items 
        else items.filter { it.category?.lowercase()?.equals(selectedCategoryFilter.lowercase()) == true }
    }

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center
        ) {
            com.pdm0126.outfix.ui.LiquidDropdownButton(
                selectedItem = selectedCategoryFilter,
                isExpanded = isDropdownExpanded,
                onClick = {
                    if (buttonCoords != null) {
                        onOpenDropdown(title, buttonCoords!!, availableCategories, selectedCategoryFilter) { selectedCategoryFilter = it }
                    }
                },
                onGloballyPositioned = { buttonCoords = it }
            )
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

        Box(modifier = Modifier.fillMaxWidth()) {
            LazyRow(
                modifier = Modifier.nestedScroll(nestedScrollConnection).fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 24.dp),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)
            ) {
                items(filteredItems) { garment ->
                    GarmentCard(
                        garment = garment,
                        isSelected = garment == selectedItem || selectedItems.contains(garment),
                        onClick = { onItemSelected(garment) },
                        onLongClick = if (onItemLongClick != null) { { bounds -> onItemLongClick(garment, bounds) } } else null
                    )
                }
            }
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        androidx.compose.ui.graphics.Brush.horizontalGradient(
                            0.0f to Color(0xFFEDDDCC),
                            0.05f to Color.Transparent,
                            0.95f to Color.Transparent,
                            1.0f to Color(0xFFEDDDCC)
                        )
                    )
            )
        }
    }
}


@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun GarmentCard(
    garment: GarmentResponse,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: ((androidx.compose.ui.geometry.Rect) -> Unit)? = null
) {
    val borderColor = if (isSelected) Color.White else Color.Transparent
    var bounds by remember { mutableStateOf<androidx.compose.ui.geometry.Rect?>(null) }
    
    val isDetailActive = ClosetOverlayState.isOverlayActive && ClosetOverlayState.detailGarment?.id == garment.id
    
    Box(
        modifier = Modifier
            .size(90.dp)
            .graphicsLayer { alpha = if (isDetailActive) 0f else 1f }
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF6EEE6))
            .onGloballyPositioned { coords ->
                try {
                    bounds = coords.boundsInRoot()
                } catch (e: Exception) {}
            }
            .combinedClickable(
                onClick = onClick,
                onLongClick = { bounds?.let { onLongClick?.invoke(it) } }
            )
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
