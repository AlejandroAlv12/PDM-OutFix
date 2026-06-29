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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.ui.zIndex

import com.pdm0126.outfix.ui.liquidGlass
import com.pdm0126.outfix.ui.bouncyClickable
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.graphics.drawscope.translate
import android.os.Build
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.pdm0126.outfix.ui.theme.LimeGreen
import com.pdm0126.outfix.ui.HorizontalEdgesProgressiveBlurLayer
import java.io.File
import androidx.compose.ui.graphics.toArgb
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.IntOffset
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

import com.pdm0126.outfix.data.GARMENT_CATEGORIES
import com.pdm0126.outfix.data.GARMENT_STYLES
import com.pdm0126.outfix.data.GARMENT_SIZES
import com.pdm0126.outfix.ui.CustomSizeSlider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewGarmentScreen(
    imagePath: String,
    detectedCategory: String = "Desconocido",
    detectedColors: List<Color> = emptyList(),
    onBack: () -> Unit,
    onSave: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    var isSaving by remember { mutableStateOf(false) }

    var title by remember { mutableStateOf(if (detectedCategory != "Desconocido") detectedCategory else "") }
    var selectedCategory by remember { mutableStateOf(if (detectedCategory != "Desconocido") detectedCategory else "Camisa") }
    var showCategoryMenu by remember { mutableStateOf(false) }
    val categories = GARMENT_CATEGORIES
    var selectedStyle by remember { mutableStateOf("casual") }
    
    val colors = remember(detectedColors) {
        if (detectedColors.isNotEmpty()) detectedColors else listOf(Color(0xFF8B2011), Color(0xFFFFB68C))
    }
    var selectedColor by remember { mutableStateOf(colors.firstOrNull() ?: Color.Gray) }
    
    var estiloEj by remember { mutableStateOf("") }
    var marcaEj by remember { mutableStateOf("") }
    
    var selectedSizeIndex by remember { mutableStateOf(2) }
    var manualTitleEdit by remember { mutableStateOf(false) }

    LaunchedEffect(selectedCategory, marcaEj) {
        if (!manualTitleEdit) {
            title = if (marcaEj.isNotEmpty()) "$selectedCategory $marcaEj" else selectedCategory
        }
    }
    
    val backgroundLayer = rememberGraphicsLayer()
    var buttonCoords: LayoutCoordinates? by remember { mutableStateOf(null) }
    var screenCoords: LayoutCoordinates? by remember { mutableStateOf(null) }
    
    val focusManager = LocalFocusManager.current

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                })
            },
        color = Color(0xFFFAFAFA)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            val targetWidth = 200.dp
            val menuProgress by animateFloatAsState(
                targetValue = if (showCategoryMenu) 1f else 0f,
                animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f),
                label = "menuProgress"
            )
            
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
                        .bouncyClickable { onBack() }
                        .clip(CircleShape)
                        .background(Color(0xFFBDBDBD)),
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
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Serif
                )

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .bouncyClickable(enabled = !isSaving) { 
                            coroutineScope.launch {
                                isSaving = true
                                val hexColor = String.format("#%06X", 0xFFFFFF and selectedColor.toArgb())
                                    val currentSizes = if (selectedCategory in com.pdm0126.outfix.data.SHOE_CATEGORIES) com.pdm0126.outfix.data.SHOE_SIZES else GARMENT_SIZES
                                    val request = com.pdm0126.outfix.data.api.dto.CreateGarmentRequest(
                                        name = title.ifBlank { selectedCategory },
                                        category = selectedCategory,
                                        colorHex = hexColor,
                                        style = if (estiloEj.isNotBlank()) estiloEj else selectedStyle,
                                        brand = marcaEj.ifBlank { null },
                                        size = currentSizes.getOrNull(selectedSizeIndex),
                                    imageUrl = imagePath
                                )
                                try {
                                    val result = com.pdm0126.outfix.OutfixApplication.instance.garmentRepository.createGarment(request)
                                    if (result.isSuccess) {
                                        android.widget.Toast.makeText(context, "Prenda guardada con éxito", android.widget.Toast.LENGTH_SHORT).show()
                                        onSave()
                                    } else {
                                        android.widget.Toast.makeText(context, "Error guardando prenda", android.widget.Toast.LENGTH_SHORT).show()
                                        onSave()
                                    }
                                } catch (e: Exception) {
                                    android.util.Log.e("NewGarmentScreen", "Error", e)
                                    onSave()
                                } finally {
                                    isSaving = false
                                }
                            }
                        }
                        .clip(CircleShape)
                        .background(if (isSaving) Color.Gray else LimeGreen.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = "Guardar",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            HorizontalDivider(color = Color(0xFFE5E5E5), thickness = 1.dp)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                
                com.pdm0126.outfix.ui.LiquidDropdownButton(
                    selectedItem = selectedCategory,
                    isExpanded = showCategoryMenu,
                    onClick = {
                        focusManager.clearFocus()
                        showCategoryMenu = true 
                    },
                    onGloballyPositioned = { buttonCoords = it },
                    modifier = Modifier.wrapContentSize(Alignment.TopStart).zIndex(10f)
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFE0E0E0))
                        .border(1.dp, Color(0xFFBDBDBD), RoundedCornerShape(16.dp))
                ) {
                    val file = remember(imagePath) { File(imagePath) }
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
                    onValueChange = { 
                        title = it
                        manualTitleEdit = true
                    },
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

                val configuration = androidx.compose.ui.platform.LocalConfiguration.current
                val screenWidthDp = configuration.screenWidthDp.dp

                com.pdm0126.outfix.ui.StyleSlider(
                    selectedStyle = selectedStyle,
                    onStyleSelected = { selectedStyle = it }
                )

                Spacer(modifier = Modifier.height(8.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(text = "Colores", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        colors.forEach { color ->
                            val isSelected = color == selectedColor
                            
                            val animatedSize by animateDpAsState(
                                targetValue = if (isSelected) 48.dp else 40.dp,
                                animationSpec = tween(durationMillis = 200),
                                label = "colorSize"
                            )
                            val animatedBorderWidth by animateDpAsState(
                                targetValue = if (isSelected) 3.dp else 2.dp,
                                animationSpec = tween(durationMillis = 200),
                                label = "borderWidth"
                            )
                            val animatedBorderColor by animateColorAsState(
                                targetValue = if (isSelected) Color.Black else Color.LightGray,
                                animationSpec = tween(durationMillis = 200),
                                label = "borderColor"
                            )

                            Box(
                                modifier = Modifier.size(48.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(animatedSize)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                        .border(
                                            width = animatedBorderWidth,
                                            color = animatedBorderColor,
                                            shape = CircleShape
                                        )
                                        .clickable { 
                                            if (!isSelected) {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                selectedColor = color 
                                            }
                                        }
                                        .padding(4.dp)
                                        .background(color, CircleShape)
                                )
                            }
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
                        placeholder = { Text("Estilo:", color = Color.Gray, fontSize = 14.sp) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedIndicatorColor = Color(0xFFBDBDBD),
                            focusedIndicatorColor = Color.Black
                        ),
                        singleLine = true,
                        textStyle = TextStyle(fontSize = 14.sp, color = Color.Black)
                    )

                    OutlinedTextField(
                        value = marcaEj,
                        onValueChange = { marcaEj = it },
                        placeholder = { Text("Marca:", color = Color.Gray, fontSize = 14.sp) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedIndicatorColor = Color(0xFFBDBDBD),
                            focusedIndicatorColor = Color.Black
                        ),
                        singleLine = true,
                        textStyle = TextStyle(fontSize = 14.sp, color = Color.Black)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                if (selectedCategory !in listOf("Bolso", "Mochila", "Gorra", "Sombrero", "Gorro", "Gafas", "Reloj", "Cinturón", "Corbata", "Bufanda", "Joyería", "Otro")) {
                    val currentSizes = if (selectedCategory in com.pdm0126.outfix.data.SHOE_CATEGORIES) com.pdm0126.outfix.data.SHOE_SIZES else GARMENT_SIZES
                    CustomSizeSlider(
                        sizes = currentSizes,
                        selectedIndex = selectedSizeIndex,
                        onIndexChanged = { selectedSizeIndex = it }
                    )
                }
                
                Spacer(modifier = Modifier.height(40.dp))
            }
        }

        com.pdm0126.outfix.ui.LiquidDropdownOverlay(
            isExpanded = showCategoryMenu,
            onDismissRequest = { showCategoryMenu = false },
            buttonCoords = buttonCoords,
            targetWidth = targetWidth,
            backgroundLayer = backgroundLayer,
            screenCoords = screenCoords,
            items = categories,
            selectedItem = selectedCategory,
            onItemSelected = { 
                selectedCategory = it
                showCategoryMenu = false 
            }
        )

        }
    }
}

