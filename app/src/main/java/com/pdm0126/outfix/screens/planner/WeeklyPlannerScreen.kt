package com.pdm0126.outfix.screens.planner

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.border
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.combinedClickable
import androidx.compose.ui.layout.boundsInRoot

import java.util.Calendar

import com.pdm0126.outfix.data.mock.DayInfo
import com.pdm0126.outfix.data.mock.MockDatabase
import com.pdm0126.outfix.ui.GlobalNavigationState
import com.pdm0126.outfix.ui.OutFixScreen
import com.pdm0126.outfix.screens.closet.ClosetOverlayState
import com.pdm0126.outfix.ui.bouncyClickable
import com.pdm0126.outfix.ui.liquidGlass
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.Icon
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.graphics.graphicsLayer
@Composable
fun WeeklyPlannerScreen() {
    val currentDayOfWeek = remember { Calendar.getInstance().get(Calendar.DAY_OF_WEEK) }
    
    var editingDay by remember { androidx.compose.runtime.mutableStateOf<String?>(null) }
    
    val trigger = MockDatabase.updateTrigger.value

    val daysList = MockDatabase.plannerDays
    val rotatedDays = remember(currentDayOfWeek, daysList.toList(), trigger) {
        val todayIndex = daysList.indexOfFirst { it.calendarDay == currentDayOfWeek }.takeIf { it >= 0 } ?: 0
        daysList.drop(todayIndex) + daysList.take(todayIndex)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEDDDCC))
            .clickable(
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                indication = null
            ) {
                editingDay = null
            }
            .padding(start = 12.dp, end = 12.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            rotatedDays.take(3).forEach { d ->
                DayCard(
                    day = d.day,
                    top = d.topGarment,
                    bottom = d.bottomGarment,
                    shoes = d.shoesGarment,
                    head = d.hatGarment,
                    accessories = d.accessories,
                    modifier = Modifier.weight(1f),
                    isCurrentDay = d.calendarDay == currentDayOfWeek,
                    isEditing = editingDay == d.day,
                    onClick = { 
                        editingDay = if (editingDay == d.day) null else d.day 
                    },
                    onEditClick = {
                        ClosetOverlayState.plannerEditDay = d.day
                        ClosetOverlayState.hasLoadedPlannerDay = false
                        GlobalNavigationState.requestedTab = OutFixScreen.Closet
                        editingDay = null
                    },
                    onLongClick = { bounds ->
                        ClosetOverlayState.detailDayInfo = d
                        ClosetOverlayState.detailDayBounds = bounds
                        ClosetOverlayState.isDayOverlayActive = true
                        editingDay = null
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            rotatedDays.drop(3).take(3).forEach { d ->
                DayCard(
                    day = d.day,
                    top = d.topGarment,
                    bottom = d.bottomGarment,
                    shoes = d.shoesGarment,
                    head = d.hatGarment,
                    accessories = d.accessories,
                    modifier = Modifier.weight(1f),
                    isCurrentDay = d.calendarDay == currentDayOfWeek,
                    isEditing = editingDay == d.day,
                    onClick = { 
                        editingDay = if (editingDay == d.day) null else d.day 
                    },
                    onEditClick = {
                        ClosetOverlayState.plannerEditDay = d.day
                        ClosetOverlayState.hasLoadedPlannerDay = false
                        GlobalNavigationState.requestedTab = OutFixScreen.Closet
                        editingDay = null
                    },
                    onLongClick = { bounds ->
                        ClosetOverlayState.detailDayInfo = d
                        ClosetOverlayState.detailDayBounds = bounds
                        ClosetOverlayState.isDayOverlayActive = true
                        editingDay = null
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            val d = rotatedDays.last()
            DayCard(
                day = d.day,
                top = d.topGarment,
                bottom = d.bottomGarment,
                shoes = d.shoesGarment,
                head = d.hatGarment,
                accessories = d.accessories,
                modifier = Modifier.width(115.dp),
                isCurrentDay = d.calendarDay == currentDayOfWeek,
                isEditing = editingDay == d.day,
                onClick = { 
                    editingDay = if (editingDay == d.day) null else d.day 
                },
                onEditClick = {
                    ClosetOverlayState.plannerEditDay = d.day
                    ClosetOverlayState.hasLoadedPlannerDay = false
                    GlobalNavigationState.requestedTab = OutFixScreen.Closet
                    editingDay = null
                },
                onLongClick = { bounds ->
                    ClosetOverlayState.detailDayInfo = d
                    ClosetOverlayState.detailDayBounds = bounds
                    ClosetOverlayState.isDayOverlayActive = true
                    editingDay = null
                }
            )
        }
        
        Spacer(modifier = Modifier.height(120.dp))
    }
}

@Composable
fun DayCard(
    day: String,
    top: com.pdm0126.outfix.data.api.dto.GarmentResponse?,
    bottom: com.pdm0126.outfix.data.api.dto.GarmentResponse?,
    shoes: com.pdm0126.outfix.data.api.dto.GarmentResponse?,
    head: com.pdm0126.outfix.data.api.dto.GarmentResponse?,
    accessories: List<com.pdm0126.outfix.data.api.dto.GarmentResponse> = emptyList(),
    modifier: Modifier = Modifier,
    isCurrentDay: Boolean = false,
    isEditing: Boolean = false,
    onClick: () -> Unit = {},
    onEditClick: () -> Unit = {},
    onLongClick: ((androidx.compose.ui.geometry.Rect) -> Unit)? = null
) {
    val hapticFeedback = LocalHapticFeedback.current
    val cardLayer = androidx.compose.ui.graphics.rememberGraphicsLayer()
    var rootCoords by remember { mutableStateOf<androidx.compose.ui.layout.LayoutCoordinates?>(null) }
    var buttonCoords by remember { mutableStateOf<androidx.compose.ui.layout.LayoutCoordinates?>(null) }

    var cardBounds by remember { mutableStateOf<androidx.compose.ui.geometry.Rect?>(null) }

    Box(
        modifier = modifier
            .aspectRatio(0.48f)
            .clip(RoundedCornerShape(12.dp))
            .combinedClickable(
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                indication = null,
                onClick = { onClick() },
                onLongClick = { 
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    cardBounds?.let { onLongClick?.invoke(it) }
                }
            )
            .onGloballyPositioned { coords ->
                rootCoords = coords
                try { cardBounds = coords.boundsInRoot() } catch (e: Exception) {}
            }
    ) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .drawWithContent {
                cardLayer.record {
                    drawRect(Color(0xFFF6EEE6))
                    this@drawWithContent.drawContent()
                }
                drawLayer(cardLayer)
            }
            .then(
                if (isCurrentDay) Modifier.border(3.dp, Color.White, RoundedCornerShape(12.dp))
                else Modifier
            )
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = day,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = Color.Black,
            fontFamily = androidx.compose.ui.text.font.FontFamily.Serif
        )
        Spacer(modifier = Modifier.height(8.dp))
        com.pdm0126.outfix.ui.CharacterWithClothes(
            top = top,
            bottom = bottom,
            shoes = shoes,
            head = head,
            accessories = accessories,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )
    }
    
        AnimatedVisibility(
            visible = isEditing,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            val entranceScale by transition.animateFloat(
                transitionSpec = { androidx.compose.animation.core.spring(
                    dampingRatio = androidx.compose.animation.core.Spring.DampingRatioNoBouncy,
                    stiffness = 400f
                ) },
                label = "entranceScale"
            ) { state ->
                if (state == androidx.compose.animation.EnterExitState.Visible) 1f else 0.01f
            }

            var isPressedInstant by remember { mutableStateOf(false) }
            val buttonScale by androidx.compose.animation.core.animateFloatAsState(
                targetValue = if (isPressedInstant) 1.08f else 1f,
                animationSpec = androidx.compose.animation.core.spring(
                    dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
                    stiffness = 400f
                ),
                label = "editButtonScale"
            )
            
            val totalScale = entranceScale * buttonScale
            
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .onGloballyPositioned { buttonCoords = it }
                    .graphicsLayer {
                        scaleX = totalScale
                        scaleY = totalScale
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
                    ) { onEditClick() },
                contentAlignment = Alignment.Center
            ) {
                if (android.os.Build.VERSION.SDK_INT >= 31) {
                    androidx.compose.foundation.Canvas(
                        modifier = Modifier.matchParentSize().liquidGlass(
                            blur = 5f,
                            saturation = 1.2f,
                            refraction = 0.5f,
                            curve = 0.5f,
                            dispersion = 0.15f,
                            normalizedRadius = 0.5f
                        )
                    ) {
                        scale(
                            scaleX = 1f / totalScale,
                            scaleY = 1f / totalScale,
                            pivot = center
                        ) {
                            if (rootCoords != null && buttonCoords != null && rootCoords!!.isAttached && buttonCoords!!.isAttached) {
                                val offset = rootCoords!!.localPositionOf(buttonCoords!!, Offset.Zero)
                                translate(left = -offset.x, top = -offset.y) {
                                    drawLayer(cardLayer)
                                }
                            }
                        }
                    }
                } else {
                    Box(modifier = Modifier.matchParentSize().background(Color.Black.copy(alpha = 0.5f), androidx.compose.foundation.shape.CircleShape))
                }
                Box(modifier = Modifier.matchParentSize().background(Color.Black.copy(alpha = 0.40f), androidx.compose.foundation.shape.CircleShape))
                Icon(
                    imageVector = Icons.Rounded.Edit,
                    contentDescription = "Edit Day",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}
