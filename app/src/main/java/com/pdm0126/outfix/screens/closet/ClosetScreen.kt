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
import com.pdm0126.outfix.OutfixApplication
import com.pdm0126.outfix.ui.theme.LimeGreen
import kotlinx.coroutines.launch
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
import com.pdm0126.outfix.ui.bouncyClickable
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.onSizeChanged

@Composable
fun ClosetScreen() {
    val coroutineScope = rememberCoroutineScope()
    val repository = OutfixApplication.instance.garmentRepository
    val garments by repository.garmentsFlow.collectAsState(initial = emptyList())
    var isLoading by remember { mutableStateOf(false) }

    var selectedTop by remember { mutableStateOf<GarmentResponse?>(null) }
    var selectedBottom by remember { mutableStateOf<GarmentResponse?>(null) }
    var selectedShoes by remember { mutableStateOf<GarmentResponse?>(null) }
    var selectedHead by remember { mutableStateOf<GarmentResponse?>(null) }
    var selectedAccessory by remember { mutableStateOf<GarmentResponse?>(null) }

    LaunchedEffect(Unit) {
        isLoading = true
        repository.refreshGarments()
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

                val tops = garments.filter { it.category in listOf("Camiseta", "Camisa", "Blusa", "Top", "Suéter", "Chaqueta", "Abrigo", "Vestido") }
                val bottoms = garments.filter { it.category in listOf("Jeans", "Pantalón", "Short", "Falda", "Vestido") }
                val shoes = garments.filter { it.category in listOf("Zapatillas", "Botas", "Zapatos") }
                val headwear = garments.filter { it.category in listOf("Gorra", "Sombrero", "Gorro") }
                val accessories = garments.filter { it.category in listOf("Bolso", "Mochila", "Reloj", "Gafas", "Cinturón", "Corbata", "Bufanda", "Joyería", "Accesorio", "Otros", "Otro") }

                CategorySlider(
                    title = "superior",
                    items = tops,
                    selectedItem = selectedTop,
                    onItemSelected = { selectedTop = if (selectedTop == it) null else it }
                )

                CategorySlider(
                    title = "inferior",
                    items = bottoms,
                    selectedItem = selectedBottom,
                    onItemSelected = { selectedBottom = if (selectedBottom == it) null else it }
                )

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
                        selectedItem = selectedAccessory,
                        onItemSelected = { selectedAccessory = if (selectedAccessory == it) null else it }
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
                    head = selectedHead
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
    head: GarmentResponse?
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .height(280.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF6EEE6))
            .border(1.dp, Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        val defaultSkin = Color(0xFFEAC3AB)
        
        val topColor = remember(top?.colorHex) { top?.colorHex?.let { parseColorHex(it) } ?: Color.White }
        val bottomColor = remember(bottom?.colorHex) { bottom?.colorHex?.let { parseColorHex(it) } ?: Color(0xFF81D4FA) }
        val shoesColor = remember(shoes?.colorHex) { shoes?.colorHex?.let { parseColorHex(it) } ?: Color(0xFF388E3C) }

        Canvas(modifier = Modifier.size(280.dp)) {
            val cx = size.width / 2
            val cy = size.height / 2
            
            drawCircle(color = defaultSkin, radius = 36f, center = Offset(cx, cy - 85f))
            if (head != null) {
                val hatColor = head.colorHex?.let { parseColorHex(it) } ?: Color(0xFF90CAF9)
                drawArc(
                    color = hatColor,
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = true,
                    topLeft = Offset(cx - 36f, cy - 121f),
                    size = Size(73f, 73f)
                )
            }

            drawRoundRect(
                color = topColor,
                topLeft = Offset(cx - 58f, cy - 32f),
                size = Size(117f, 110f),
                cornerRadius = CornerRadius(13f, 13f)
            )

            drawRoundRect(
                color = defaultSkin,
                topLeft = Offset(cx - 84f, cy - 19f),
                size = Size(21f, 91f),
                cornerRadius = CornerRadius(10f, 10f)
            )
            drawRoundRect(
                color = defaultSkin,
                topLeft = Offset(cx + 64f, cy - 19f),
                size = Size(21f, 91f),
                cornerRadius = CornerRadius(10f, 10f)
            )
            
            drawRoundRect(
                color = topColor,
                topLeft = Offset(cx - 84f, cy - 26f),
                size = Size(21f, 45f),
                cornerRadius = CornerRadius(10f, 10f)
            )
            drawRoundRect(
                color = topColor,
                topLeft = Offset(cx + 64f, cy - 26f),
                size = Size(21f, 45f),
                cornerRadius = CornerRadius(10f, 10f)
            )

            drawRoundRect(
                color = bottomColor,
                topLeft = Offset(cx - 52f, cy + 78f),
                size = Size(45f, 104f),
                cornerRadius = CornerRadius(7f, 7f)
            )
            drawRoundRect(
                color = bottomColor,
                topLeft = Offset(cx + 6.5f, cy + 78f),
                size = Size(45f, 104f),
                cornerRadius = CornerRadius(7f, 7f)
            )
            drawRect(
                color = Color(0xFFF6EEE6),
                topLeft = Offset(cx - 7f, cy + 78f),
                size = Size(13.5f, 104f)
            )

            drawArc(
                color = shoesColor,
                startAngle = 0f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(cx - 55f, cy + 169f),
                size = Size(49f, 39f)
            )
            drawArc(
                color = shoesColor,
                startAngle = 0f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(cx + 5f, cy + 169f),
                size = Size(49f, 39f)
            )
        }
    }
}

@Composable
fun CategorySlider(
    title: String,
    items: List<GarmentResponse>,
    selectedItem: GarmentResponse?,
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
                    .clickable {  }
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
                    isSelected = garment == selectedItem,
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
            contentScale = ContentScale.Crop,
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
