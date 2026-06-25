package com.pdm0126.outfix.screens.scan

import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.ui.zIndex
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.pdm0126.outfix.ui.liquidGlass
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.graphics.drawscope.translate
import android.os.Build
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.pdm0126.outfix.ui.theme.LimeGreen
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewGarmentScreen(
    imagePath: String,
    detectedCategory: String = "Desconocido",
    detectedColors: List<Color> = emptyList(),
    onBack: () -> Unit,
    onSave: () -> Unit
) {
    var title by remember { mutableStateOf(if (detectedCategory != "Desconocido") detectedCategory else "") }
    var selectedCategory by remember { mutableStateOf(if (detectedCategory != "Desconocido") detectedCategory else "Camisa") }
    var showCategoryMenu by remember { mutableStateOf(false) }
    val categories = listOf(
        "Camiseta", "Camisa", "Blusa", "Top", "Suéter", "Chaqueta", "Abrigo",
        "Pantalón", "Jeans", "Short", "Falda",
        "Cabeza", "Calzado", "Vestido", "Bolso", "Accesorio", "Otro"
    )
    
    val styles = listOf("casual", "formal", "deportiva", "verano", "invierno")
    var selectedStyle by remember { mutableStateOf("casual") }
    
    val colors = if (detectedColors.isNotEmpty()) detectedColors else listOf(Color(0xFF8B2011), Color(0xFFFFB68C))
    var selectedColor by remember { mutableStateOf(colors.firstOrNull() ?: Color.Gray) }
    
    var estiloEj by remember { mutableStateOf("") }
    var marcaEj by remember { mutableStateOf("") }
    
    var selectedSizeIndex by remember { mutableStateOf(1) }
    
    val backgroundLayer = rememberGraphicsLayer()
    var buttonCoords: LayoutCoordinates? by remember { mutableStateOf(null) }
    var screenCoords: LayoutCoordinates? by remember { mutableStateOf(null) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFFAFAFA)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .onGloballyPositioned { screenCoords = it }
                    .drawWithContent {
                        backgroundLayer.record {
                            drawRect(Color(0xFFFAFAFA))
                            this@drawWithContent.drawContent()
                        }
                        drawLayer(backgroundLayer)
                    }
                    .padding(top = 24.dp)
            ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFBDBDBD))
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ChevronLeft,
                        contentDescription = "Atrás",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Text(
                    text = "Nueva prenda",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                    color = Color.Black
                )

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(LimeGreen.copy(alpha = 0.5f))
                        .clickable { onSave() },
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

            HorizontalDivider(color = Color(0xFFE5E5E5), thickness = 1.dp)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                
                Box(
                    modifier = Modifier.wrapContentSize(Alignment.TopStart).zIndex(10f)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Color.Black)
                            .onGloballyPositioned { buttonCoords = it }
                            .clickable { showCategoryMenu = !showCategoryMenu }
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = selectedCategory, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = if (showCategoryMenu) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown, 
                                contentDescription = null, 
                                tint = Color.White, 
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFE0E0E0))
                        .border(1.dp, Color(0xFFBDBDBD), RoundedCornerShape(16.dp))
                ) {
                    val file = File(imagePath)
                    if (file.exists()) {
                        AsyncImage(
                            model = Uri.fromFile(file),
                            contentDescription = "Prenda Escaneada",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        )
                    } else {
                        Text("No se encontró la imagen", modifier = Modifier.align(Alignment.Center))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                BasicTextField(
                    value = title,
                    onValueChange = { title = it },
                    textStyle = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFBDBDBD), RoundedCornerShape(8.dp))
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    cursorBrush = SolidColor(Color.Black),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(styles) { style ->
                        val isSelected = style == selectedStyle
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(if (isSelected) Color.Black else Color.Transparent)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) Color.Transparent else Color(0xFFBDBDBD),
                                    shape = RoundedCornerShape(50)
                                )
                                .clickable { selectedStyle = style }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = style,
                                color = if (isSelected) Color.White else Color.Gray,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(text = "Colores", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        colors.forEach { color ->
                            val isSelected = color == selectedColor
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) Color.Gray else Color.LightGray,
                                        shape = CircleShape
                                    )
                                    .clickable { selectedColor = color }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = estiloEj,
                        onValueChange = { estiloEj = it },
                        placeholder = { Text("Estilo: ej. Oversize", color = Color.Gray, fontSize = 14.sp) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedIndicatorColor = Color(0xFFBDBDBD),
                            focusedIndicatorColor = Color.Black
                        ),
                        singleLine = true,
                        textStyle = TextStyle(fontSize = 14.sp)
                    )

                    OutlinedTextField(
                        value = marcaEj,
                        onValueChange = { marcaEj = it },
                        placeholder = { Text("Marca: ej. Adidas", color = Color.Gray, fontSize = 14.sp) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedIndicatorColor = Color(0xFFBDBDBD),
                            focusedIndicatorColor = Color.Black
                        ),
                        singleLine = true,
                        textStyle = TextStyle(fontSize = 14.sp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                CustomSizeSlider(
                    selectedIndex = selectedSizeIndex,
                    onIndexChanged = { selectedSizeIndex = it }
                )
                
                Spacer(modifier = Modifier.height(40.dp))
            }
        }

            val menuVisibleState = remember { MutableTransitionState(false) }
            menuVisibleState.targetState = showCategoryMenu

            if (menuVisibleState.currentState || menuVisibleState.targetState) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { showCategoryMenu = false }
                )
                
                if (buttonCoords != null) {
                    val density = LocalDensity.current
                    val yOffset = with(density) { buttonCoords!!.positionInRoot().y.toDp() + buttonCoords!!.size.height.toDp() + 8.dp }
                    val xOffset = with(density) { buttonCoords!!.positionInRoot().x.toDp() + (buttonCoords!!.size.width.toDp() / 2) - 100.dp }

                    Box(
                        modifier = Modifier
                            .absoluteOffset(x = xOffset, y = yOffset)
                            .width(200.dp)
                            .heightIn(max = 300.dp)
                    ) {
                        AnimatedVisibility(
                            visibleState = menuVisibleState,
                            enter = expandVertically(expandFrom = Alignment.Top, animationSpec = tween(400)) + fadeIn(animationSpec = tween(400)),
                            exit = shrinkVertically(shrinkTowards = Alignment.Top, animationSpec = tween(400)) + fadeOut(animationSpec = tween(400))
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                var menuCoords: LayoutCoordinates? by remember { mutableStateOf(null) }
                                
                                if (Build.VERSION.SDK_INT >= 31) {
                                    androidx.compose.foundation.Canvas(
                                        modifier = Modifier
                                            .matchParentSize()
                                            .onGloballyPositioned { menuCoords = it }
                                            .liquidGlass(
                                                blur = 30f,
                                                saturation = 1.4f,
                                                refraction = 0.6f,
                                                curve = 0.08f,
                                                dispersion = 0.25f,
                                                normalizedRadius = 0.15f
                                            )
                                    ) {
                                        if (menuCoords != null && screenCoords != null) {
                                            val menuPos = menuCoords!!.positionInRoot()
                                            val screenPos = screenCoords!!.positionInRoot()
                                            translate(left = -(menuPos.x - screenPos.x), top = -(menuPos.y - screenPos.y)) {
                                                drawLayer(backgroundLayer)
                                            }
                                            drawRect(Color.White.copy(alpha = 0.4f))
                                        }
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .matchParentSize()
                                            .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(16.dp))
                                    )
                                }
                                
                                LazyColumn(
                                    modifier = Modifier.padding(vertical = 8.dp)
                                ) {
                                    items(categories) { category ->
                                        Text(
                                            text = category,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    selectedCategory = category
                                                    showCategoryMenu = false
                                                }
                                                .padding(horizontal = 16.dp, vertical = 12.dp),
                                            color = Color.Black,
                                            fontWeight = if (category == selectedCategory) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 14.sp,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CustomSizeSlider(
    selectedIndex: Int,
    onIndexChanged: (Int) -> Unit
) {
    val sizes = listOf("XS", "S", "M", "L", "XL")
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = sizes[selectedIndex],
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = Color.Black
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .pointerInput(Unit) {
                    detectHorizontalDragGestures { change, dragAmount ->
                        change.consume()
                        val width = size.width
                        val segment = width / (sizes.size - 1).toFloat()
                        val pos = change.position.x.coerceIn(0f, width.toFloat())
                        val index = (pos / segment).toInt().coerceIn(0, sizes.size - 1)
                        onIndexChanged(index)
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val centerY = size.height / 2f
                val segment = width / (sizes.size - 1)
                
                val activeColor = Color(0xFF007AFF)
                val inactiveColor = Color(0xFFE0E0E0)
                
                val activeEndX = segment * selectedIndex
                
                drawLine(
                    color = activeColor,
                    start = Offset(0f, centerY),
                    end = Offset(activeEndX, centerY),
                    strokeWidth = 4.dp.toPx()
                )
                
                drawLine(
                    color = inactiveColor,
                    start = Offset(activeEndX, centerY),
                    end = Offset(width, centerY),
                    strokeWidth = 4.dp.toPx()
                )
                
                for (i in sizes.indices) {
                    val cx = i * segment
                    val isActive = i <= selectedIndex
                    
                    if (i == selectedIndex) {
                        drawCircle(color = activeColor.copy(alpha = 0.2f), radius = 16.dp.toPx(), center = Offset(cx, centerY))
                        drawCircle(color = Color.White, radius = 8.dp.toPx(), center = Offset(cx, centerY))
                        drawCircle(color = activeColor, radius = 4.dp.toPx(), center = Offset(cx, centerY))
                    } else {
                        drawCircle(
                            color = if (isActive) activeColor else inactiveColor,
                            radius = 5.dp.toPx(),
                            center = Offset(cx, centerY)
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 3.dp.toPx(),
                            center = Offset(cx, centerY)
                        )
                    }
                }
            }
        }
    }
}
