package com.pdm0126.outfix.screens.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.graphics.graphicsLayer
import com.pdm0126.outfix.ui.AppViewModel
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import com.pdm0126.outfix.data.model.DayInfo
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.pdm0126.outfix.OutfixApplication
import com.pdm0126.outfix.data.api.dto.GarmentResponse
import com.pdm0126.outfix.ui.CharacterWithClothes
import com.pdm0126.outfix.screens.closet.parseColorHex
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Calendar
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    appViewModel: AppViewModel,
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    androidx.compose.foundation.layout.Box(modifier = androidx.compose.ui.Modifier.fillMaxSize()) {
        val appState by appViewModel.uiState.collectAsState()
        
        val plannerDays by homeViewModel.plannerDays.collectAsState()
        val garments by homeViewModel.garments.collectAsState()
        val lentItems by homeViewModel.lentItems.collectAsState()
    
        val currentDayOfWeek = remember { Calendar.getInstance().get(Calendar.DAY_OF_WEEK) }
        val todayInfo = plannerDays.find { it.calendarDay == currentDayOfWeek }
        
        val pendingLentItems = remember(lentItems) { lentItems.filter { !it.isReturned } }
        val streak = remember(plannerDays, currentDayOfWeek) { homeViewModel.computeStreak(plannerDays) }
    
        val scrollState = rememberScrollState()
        val density = androidx.compose.ui.platform.LocalDensity.current
        val coroutineScope = rememberCoroutineScope()
        
        var previousScrollOffset by remember { mutableStateOf(0) }
        LaunchedEffect(scrollState) {
            androidx.compose.runtime.snapshotFlow { scrollState.value }
                .collect { currentOffset ->
                    val diff = currentOffset - previousScrollOffset
                    
                    if (diff > 20) {
                        appViewModel.setFabVisible(false)
                    } else if (diff < -20) {
                        appViewModel.setFabVisible(true)
                    }
                    
                    if (currentOffset == 0) {
                        appViewModel.setFabVisible(true)
                    }
                    
                    if (kotlin.math.abs(diff) > 20) {
                        previousScrollOffset = currentOffset
                    }
                }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))
    
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val haptic = LocalHapticFeedback.current
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp)
                        .onGloballyPositioned { coords ->
                            try { appViewModel.setHomeOverlayBounds(coords.boundsInRoot()) } catch (e: Exception) {}
                        }
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFFF6EEE6))
                        .clickable {
                            val topOffsetPx = with(density) { 24.dp.toPx() }
                            if (scrollState.value > topOffsetPx) return@clickable
                            
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            val info = DayInfo(
                                day = "Hoy",
                                calendarDay = Calendar.getInstance().get(Calendar.DAY_OF_WEEK),
                                topColor = Color.Transparent,
                                bottomColor = Color.Transparent,
                                shoesColor = Color.Transparent,
                                hatColor = Color.Transparent,
                                topGarment = todayInfo?.topGarment,
                                bottomGarment = todayInfo?.bottomGarment,
                                shoesGarment = todayInfo?.shoesGarment,
                                hatGarment = todayInfo?.hatGarment,
                                accessories = todayInfo?.accessories ?: emptyList()
                            )
                            appViewModel.showHomeOverlay(info, appState.homeOverlayBounds)
                        }
                        .graphicsLayer { alpha = if (appState.isHomeOverlayActive) 0f else 1f }
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val headCategories = remember { listOf("Gafas", "Joyería") }
                    val topCategories = remember { listOf("Bufanda", "Corbata") }
                    val bottomCategories = remember { listOf("Reloj", "Cinturón") }
                    val shoesCategories = remember { listOf("Bolso", "Mochila", "Otro") }

                    val headAccs = remember(todayInfo?.accessories) { todayInfo?.accessories?.filter { it.category in headCategories } ?: emptyList() }
                    val topAccs = remember(todayInfo?.accessories) { todayInfo?.accessories?.filter { it.category in topCategories } ?: emptyList() }
                    val bottomAccs = remember(todayInfo?.accessories) { todayInfo?.accessories?.filter { it.category in bottomCategories } ?: emptyList() }
                    val shoesAccs = remember(todayInfo?.accessories) { todayInfo?.accessories?.filter { it.category in shoesCategories } ?: emptyList() }

                    val isDress = todayInfo?.topGarment?.category?.equals("Vestido", ignoreCase = true) == true

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                            .border(1.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    ) {
                        com.pdm0126.outfix.screens.closet.SlotWithAccessories("Cabeza", todayInfo?.hatGarment, headAccs, Modifier.weight(1f))

                        if (isDress) {
                            com.pdm0126.outfix.screens.closet.SlotWithAccessories("Vestido", todayInfo?.topGarment, topAccs, Modifier.weight(3f))
                        } else {
                            com.pdm0126.outfix.screens.closet.SlotWithAccessories("Superior", todayInfo?.topGarment, topAccs, Modifier.weight(2f))
                            com.pdm0126.outfix.screens.closet.SlotWithAccessories("Inferior", todayInfo?.bottomGarment, bottomAccs, Modifier.weight(2f))
                        }
                        com.pdm0126.outfix.screens.closet.SlotWithAccessories("Calzado", todayInfo?.shoesGarment, shoesAccs, Modifier.weight(1.5f))
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .aspectRatio(0.5f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Hoy",
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = Color.Black,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Serif
                        )
                        
                        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                            CharacterWithClothes(
                                top = todayInfo?.topGarment,
                                bottom = todayInfo?.bottomGarment,
                                shoes = todayInfo?.shoesGarment,
                                head = todayInfo?.hatGarment,
                                accessories = todayInfo?.accessories ?: emptyList(),
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        
                        val allGarments = listOfNotNull(
                            todayInfo?.topGarment,
                            todayInfo?.bottomGarment,
                            todayInfo?.shoesGarment,
                            todayInfo?.hatGarment
                        ) + (todayInfo?.accessories ?: emptyList())
                        
                        val styles = allGarments.mapNotNull { it.style }.filter { it.isNotBlank() }
                        
                        val style = if (styles.isEmpty()) {
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
                        }
                        
                        Text(
                            text = style.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.Black,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Serif
                        )
                    }
                }
                
                var isRandomPressedInstant by remember { mutableStateOf(false) }
                val randomScale by androidx.compose.animation.core.animateFloatAsState(
                    targetValue = if (isRandomPressedInstant) 0.92f else 1f,
                    animationSpec = androidx.compose.animation.core.spring(
                        dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
                        stiffness = 400f
                    ),
                    label = "randomWidgetScale"
                )
                val hapticFeedback = androidx.compose.ui.platform.LocalHapticFeedback.current
                val context = androidx.compose.ui.platform.LocalContext.current
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(140.dp)
                            .graphicsLayer {
                                scaleX = randomScale
                                scaleY = randomScale
                            }
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFF6EEE6))
                            .pointerInput(Unit) {
                                awaitEachGesture {
                                    awaitFirstDown(requireUnconsumed = false)
                                    isRandomPressedInstant = true
                                    
                                    val hapticJob = coroutineScope.launch {
                                        var interval = 300L
                                        while (isActive) {
                                            hapticFeedback.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                                            kotlinx.coroutines.delay(interval)
                                            interval = (interval * 0.75).toLong().coerceAtLeast(15L)
                                        }
                                    }
                                    
                                    var heldFor2Seconds = false
                                    var wasCancelled = false
                                    try {
                                        withTimeout(1300L) {
                                            val upEvent = waitForUpOrCancellation()
                                            if (upEvent == null) {
                                                wasCancelled = true
                                            }
                                        }
                                    } catch (e: androidx.compose.ui.input.pointer.PointerEventTimeoutCancellationException) {
                                        hapticJob.cancel()
                                        isRandomPressedInstant = false
                                        heldFor2Seconds = true
                                        
                                        homeViewModel.shuffleAndSaveToday(todayInfo)
                                        
                                        hapticFeedback.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                        waitForUpOrCancellation()
                                    }
                                    
                                    hapticJob.cancel()
                                    isRandomPressedInstant = false
                                    
                                    if (!heldFor2Seconds && !wasCancelled) {
                                        android.widget.Toast.makeText(context, "Mantén presionado para cambiar", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Shuffle,
                            contentDescription = "Shuffle",
                            tint = Color.Black,
                            modifier = Modifier.size(56.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(140.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFF6EEE6))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Racha",
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                color = Color.Black,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Serif
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Icon(
                                imageVector = Icons.Rounded.LocalFireDepartment,
                                contentDescription = "Racha",
                                tint = Color(0xFFFF7043),
                                modifier = Modifier.size(50.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = streak.toString(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp,
                                color = Color.Black
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFF6EEE6))
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Semanal",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.Black,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Serif
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    val currentDayIndex = plannerDays.indexOfFirst { it.calendarDay == currentDayOfWeek }.takeIf { it >= 0 } ?: 0
                    val rotatedDays = if (plannerDays.isNotEmpty()) {
                        plannerDays.drop(currentDayIndex) + plannerDays.take(currentDayIndex)
                    } else emptyList()
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        rotatedDays.forEach { day ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = day.day.uppercase(),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    color = Color.Black,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Serif
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                val defaultSkin = Color(0xFFEAC3AB)
                                val headColor = day.hatGarment?.colorHex?.let { parseColorHex(it) } ?: day.hatColor?.takeIf { it != Color.Transparent } ?: defaultSkin
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(headColor)
                                        .border(0.dp, Color.Black, CircleShape)
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Box(
                                    modifier = Modifier
                                        .size(width = 24.dp, height = 48.dp)
                                        .clip(RoundedCornerShape(percent = 50))
                                        .border(0.dp, Color.Black, RoundedCornerShape(percent = 50))
                                ) {
                                    Column(modifier = Modifier.fillMaxSize()) {
                                        val topC = day.topGarment?.colorHex?.let { parseColorHex(it) } ?: if (day.topColor != Color.Transparent) day.topColor else defaultSkin
                                        val bottomC = day.bottomGarment?.colorHex?.let { parseColorHex(it) } ?: if (day.bottomColor != Color.Transparent) day.bottomColor else defaultSkin
                                        
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .weight(1f)
                                                .background(topC)
                                        )
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .weight(1f)
                                                .background(bottomC)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            if (pendingLentItems.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFF6EEE6))
                        .padding(vertical = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Prestados",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color.Black,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Serif
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        val consumeHorizontalScroll = remember {
                            object : androidx.compose.ui.input.nestedscroll.NestedScrollConnection {
                                override fun onPostScroll(
                                    consumed: androidx.compose.ui.geometry.Offset,
                                    available: androidx.compose.ui.geometry.Offset,
                                    source: androidx.compose.ui.input.nestedscroll.NestedScrollSource
                                ): androidx.compose.ui.geometry.Offset {
                                    return available.copy(y = 0f)
                                }
                            }
                        }
                        
                        val lentScrollLayer = androidx.compose.ui.graphics.rememberGraphicsLayer()
                        
                        BoxWithConstraints(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clipToBounds()
                        ) {
                            val currentWidthPx = constraints.maxWidth.toFloat()
                            
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(horizontal = 24.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .nestedScroll(consumeHorizontalScroll)
                                    .drawWithContent {
                                        lentScrollLayer.record {
                                            this@drawWithContent.drawContent()
                                        }
                                    }
                            ) {
                                items(pendingLentItems) { lentItem ->
                                    Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.White)
                                        .border(1.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                        .clickable {
                                            appViewModel.openHamburgerMenu(lentItem)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    AsyncImage(
                                        model = lentItem.garmentImageUrl,
                                        contentDescription = lentItem.garmentName,
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier.fillMaxSize().padding(8.dp)
                                    )
                                }
                                }
                            }
                            
                            com.pdm0126.outfix.ui.HorizontalEdgesProgressiveBlurLayer(
                                modifier = Modifier.matchParentSize(),
                                contentLayer = lentScrollLayer,
                                maxBlur = 30f,
                                edgeWidthFraction = if (currentWidthPx > 0) with(androidx.compose.ui.platform.LocalDensity.current) { 24.dp.toPx() } / currentWidthPx else 0.1f
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(120.dp))
        }
        
    }
}
