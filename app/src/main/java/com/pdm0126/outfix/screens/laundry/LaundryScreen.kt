package com.pdm0126.outfix.screens.laundry
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.background
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocalLaundryService
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
import coil.compose.AsyncImage
import com.pdm0126.outfix.OutfixApplication
import com.pdm0126.outfix.data.api.dto.GarmentResponse
import kotlinx.coroutines.launch
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Locale
import com.pdm0126.outfix.ui.bouncyClickable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import com.pdm0126.outfix.ui.liquidGlass
import android.os.Build

import androidx.compose.animation.togetherWith
import androidx.compose.animation.animateContentSize

@Composable
fun LaundryScreen() {
    val coroutineScope = rememberCoroutineScope()
    val repository = OutfixApplication.instance.garmentRepository
    val plannerRepo = OutfixApplication.instance.plannerRepository
    
    val allGarments by repository.garmentsFlow.collectAsState(initial = emptyList())
    val plannerDays by plannerRepo.plannerDaysFlow.collectAsState(initial = emptyList())
    
    val currentDayOfWeek = remember { Calendar.getInstance().get(Calendar.DAY_OF_WEEK) }
    
    val todayInfo = plannerDays.find { it.calendarDay == currentDayOfWeek }
    
    val usedGarments = remember(allGarments) {
        allGarments.filter { it.status == "IN_WASH" }
    }
    
    val actualGarments = remember(todayInfo, allGarments) {
        listOfNotNull(todayInfo?.topGarment, todayInfo?.bottomGarment).filter { it.status != "IN_WASH" }
    }
    
    val displayItems = remember(usedGarments, actualGarments) {
        actualGarments.map { it to true } + usedGarments.map { it to false }
    }

    var confirmVaciar by remember { mutableStateOf(false) }
    var confirmLavarGarmentId by remember { mutableStateOf<String?>(null) }

    val backgroundLayer = rememberGraphicsLayer()
    var rootCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var buttonOffset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEBE0D3))
            .onGloballyPositioned { rootCoords = it }
            .pointerInput(Unit) {
                detectTapGestures {
                    confirmVaciar = false
                    confirmLavarGarmentId = null
                }
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawWithContent {
                    backgroundLayer.record {
                        drawRect(Color(0xFFEBE0D3))
                        this@drawWithContent.drawContent()
                    }
                    drawContent()
                }
        ) {
            if (displayItems.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(bottom = 80.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Outlined.LocalLaundryService,
                        contentDescription = "Empty",
                        modifier = Modifier.size(80.dp),
                        tint = Color.Gray.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("¡Tu cesto de ropa está vacío!", color = Color.DarkGray, fontSize = 18.sp)
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 96.dp, bottom = 120.dp, start = 24.dp, end = 24.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(displayItems) { (garment, isActual) ->
                    LaundryItemCard(
                        garment = garment,
                        isActual = isActual,
                        isConfirming = confirmLavarGarmentId == garment.id,
                        onWashClick = {
                            if (!isActual) {
                                if (confirmLavarGarmentId == garment.id) {
                                    coroutineScope.launch {
                                        repository.markAsWashed(garment.id)
                                    }
                                    confirmLavarGarmentId = null
                                } else {
                                    confirmLavarGarmentId = garment.id
                                    confirmVaciar = false // Reset the other confirmation
                                }
                            }
                        }
                    )
                }
            }
        }
            }
            if (usedGarments.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 24.dp)
                    .bouncyClickable {
                        if (confirmVaciar) {
                            coroutineScope.launch {
                                usedGarments.forEach { garment ->
                                    repository.markAsWashed(garment.id)
                                }
                            }
                            confirmVaciar = false
                        } else {
                            confirmVaciar = true
                            confirmLavarGarmentId = null // Reset the other confirmation
                        }
                    }
                    .height(48.dp)
                    .onGloballyPositioned { coords ->
                        if (rootCoords != null && rootCoords!!.isAttached && coords.isAttached) {
                            try {
                                buttonOffset = rootCoords!!.localPositionOf(coords, Offset.Zero)
                            } catch (e: Exception) {}
                        }
                    }
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (Build.VERSION.SDK_INT >= 31) {
                    Canvas(
                        modifier = Modifier.matchParentSize().liquidGlass(
                            blur = 12f,
                            saturation = 1.2f,
                            refraction = 0.5f,
                            curve = 0.5f,
                            dispersion = 0.15f,
                            normalizedRadius = 0.5f
                        )
                    ) {
                        translate(left = -buttonOffset.x, top = -buttonOffset.y) {
                            drawLayer(backgroundLayer)
                        }
                    }
                } else {
                    Box(modifier = Modifier.matchParentSize().background(Color.Red))
                }
                val animatedColor by androidx.compose.animation.animateColorAsState(
                    targetValue = if (confirmVaciar) Color.Red.copy(alpha = 0.70f) else Color.Red.copy(alpha = 0.40f),
                    animationSpec = androidx.compose.animation.core.tween(durationMillis = 300),
                    label = "lavarVaciarBgColor"
                )
                Box(modifier = Modifier.matchParentSize().background(animatedColor))
                androidx.compose.animation.AnimatedContent(
                    targetState = confirmVaciar,
                    transitionSpec = {
                        (androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(0)) togetherWith
                         androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(0)))
                        .using(androidx.compose.animation.SizeTransform(
                            clip = false,
                            sizeAnimationSpec = { _, _ -> androidx.compose.animation.core.tween(durationMillis = 300) }
                        ))
                    },
                    label = "lavarVaciarText"
                ) { isConfirming ->
                    Text(
                        text = if (isConfirming) "¿Lavar y vaciar?" else "Lavar y vaciar", 
                        color = Color.White, 
                        fontSize = 16.sp, 
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }
            }
        }
    }

@Composable
fun LaundryItemCard(
    garment: GarmentResponse,
    isActual: Boolean,
    isConfirming: Boolean = false,
    onWashClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFF6EEE6))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = garment.imageUrl,
                    contentDescription = garment.name,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            Spacer(modifier = Modifier.width(20.dp))
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (isActual) "Actual" else "Usada",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                
                val subtitleText = if (isActual) {
                    "Hoy"
                } else {
                    val notes = garment.notes ?: ""
                    if (notes.startsWith("USED:")) {
                        try {
                            val dateStr = notes.substringAfter("USED:")
                            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            val date = sdf.parse(dateStr)
                            val displaySdf = SimpleDateFormat("dd 'de' MMMM", Locale("es", "ES"))
                            "El " + displaySdf.format(date!!)
                        } catch (e: Exception) {
                            "Recientemente"
                        }
                    } else {
                        "Recientemente"
                    }
                }
                
                Text(
                    text = subtitleText,
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )
            }
        }
        
        if (!isActual) {
            val animatedBgColor by androidx.compose.animation.animateColorAsState(
                targetValue = if (isConfirming) Color.DarkGray else Color(0xFFBDBDBD),
                animationSpec = androidx.compose.animation.core.tween(durationMillis = 300),
                label = "lavarItemBgColor"
            )
            
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(40.dp)
                    .bouncyClickable { onWashClick() }
                    .clip(CircleShape)
                    .background(animatedBgColor),
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
