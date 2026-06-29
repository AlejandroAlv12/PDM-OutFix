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
import com.pdm0126.outfix.screens.closet.ClosetOverlayState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
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
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen() {
    androidx.compose.foundation.layout.Box(modifier = androidx.compose.ui.Modifier.fillMaxSize()) {
        val coroutineScope = rememberCoroutineScope()
        val repository = OutfixApplication.instance.plannerRepository
        val garmentRepo = OutfixApplication.instance.garmentRepository
    
        val plannerDays by repository.plannerDaysFlow.collectAsState(initial = emptyList())
        val garments by garmentRepo.garmentsFlow.collectAsState(initial = emptyList())
    
        val currentDayOfWeek = remember { Calendar.getInstance().get(Calendar.DAY_OF_WEEK) }
        val todayInfo = plannerDays.find { it.calendarDay == currentDayOfWeek }
        
        val lentGarments = remember(garments) { garments.filter { it.status == "LENT" } }
        
        val streak = remember(plannerDays, currentDayOfWeek) {
            var count = 0
            var checkDay = currentDayOfWeek
            var isFirstCheck = true
            
            for (i in 0 until 7) {
                val dayInfo = plannerDays.find { it.calendarDay == checkDay }
                val hasOutfit = dayInfo != null && (dayInfo.topGarment != null || dayInfo.bottomGarment != null || dayInfo.shoesGarment != null || dayInfo.hatGarment != null)
                
                if (hasOutfit) {
                    count++
                } else if (isFirstCheck) {
                } else {
                    break
                }
                
                isFirstCheck = false
                checkDay = if (checkDay == java.util.Calendar.SUNDAY) java.util.Calendar.SATURDAY else checkDay - 1
            }
            count
        }
    
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))
    
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val haptic = LocalHapticFeedback.current
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(300.dp)
                        .onGloballyPositioned { coords ->
                            try { ClosetOverlayState.homeOverlayBounds = coords.boundsInRoot() } catch (e: Exception) {}
                        }
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFF6EEE6))
                        .combinedClickable(
                            onLongClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                ClosetOverlayState.homeDayInfo = DayInfo(
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
                                ClosetOverlayState.isHomeOverlayActive = true
                            },
                            onClick = {}
                        )
                        .graphicsLayer { alpha = if (ClosetOverlayState.isHomeOverlayActive) 0f else 1f }
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Hoy",
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            color = Color.Black,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Serif
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                            CharacterWithClothes(
                                top = todayInfo?.topGarment,
                                bottom = todayInfo?.bottomGarment,
                                shoes = todayInfo?.shoesGarment,
                                head = todayInfo?.hatGarment,
                                accessories = todayInfo?.accessories ?: emptyList(),
                                modifier = Modifier.fillMaxSize().scale(1.5f)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
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
                            fontSize = 16.sp,
                            color = Color.Black,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Serif
                        )
                    }
                }
                
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .height(300.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
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
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFF6EEE6))
                            .clickable {
                                val availableTops = garments.filter { it.status == "AVAILABLE" && it.category in listOf("Camiseta", "Camisa", "Blusa", "Top", "Suéter", "Chaqueta", "Abrigo", "Vestido") }
                                val availableBottoms = garments.filter { it.status == "AVAILABLE" && it.category in listOf("Jeans", "Pantalón", "Short", "Falda") }
                                val availableShoes = garments.filter { it.status == "AVAILABLE" && it.category in listOf("Zapatillas", "Botas", "Zapatos") }
                                
                                val randomTop = availableTops.randomOrNull()
                                val randomBottom = if (randomTop?.category?.equals("Vestido", ignoreCase = true) == true) null else availableBottoms.randomOrNull()
                                val randomShoes = availableShoes.randomOrNull()
                                
                                if (todayInfo != null) {
                                    coroutineScope.launch {
                                        repository.saveDayOutfit(
                                            dayKey = todayInfo.day,
                                            top = randomTop,
                                            bottom = randomBottom,
                                            shoes = randomShoes,
                                            head = todayInfo.hatGarment,
                                            accessories = todayInfo.accessories
                                        )
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
            
            if (lentGarments.isNotEmpty()) {
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
                            text = "Prestados",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color.Black,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Serif
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(lentGarments) { garment ->
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.White)
                                        .border(1.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    AsyncImage(
                                        model = garment.imageUrl,
                                        contentDescription = garment.name,
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier.fillMaxSize().padding(8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(120.dp))
        }
        
    }
}
