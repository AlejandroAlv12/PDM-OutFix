package com.pdm0126.outfix.screens.menu

import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import com.pdm0126.outfix.ui.bouncyClickable
import com.pdm0126.outfix.ui.ReclaimNotificationWorker
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.pdm0126.outfix.OutfixApplication
import com.pdm0126.outfix.data.api.dto.GarmentResponse
import com.pdm0126.outfix.data.local.LentItem
import com.pdm0126.outfix.ui.theme.LimeGreen
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HamburgerMenuOverlay(
    appBackgroundLayer: androidx.compose.ui.graphics.layer.GraphicsLayer? = null
) {
    val isOpen = HamburgerMenuState.isOpen

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val lentRepository = OutfixApplication.instance.lentRepository
    val garmentRepository = OutfixApplication.instance.garmentRepository
    val lentItems by lentRepository.lentItemsFlow.collectAsState()
    val allGarments by garmentRepository.garmentsFlow.collectAsState(initial = emptyList())

    var menuState by remember { mutableStateOf(MenuState.MENU) }
    var selectedLentItem by remember { mutableStateOf<LentItem?>(null) }

    val transitionState = remember { MutableTransitionState(false) }
    transitionState.targetState = isOpen
    val transition = updateTransition(transitionState, label = "HamburgerMenuTransition")
    val containerOffsetX by transition.animateFloat(
        transitionSpec = { tween(600, easing = FastOutSlowInEasing) },
        label = "containerOffset"
    ) { if (it) 0f else -1000f }

    val titleOffsetY by transition.animateFloat(
        transitionSpec = { tween(600, delayMillis = if (targetState) 50 else 0, easing = FastOutSlowInEasing) },
        label = "titleOffset"
    ) { if (it) 0f else -1000f }

    val item1OffsetY by transition.animateFloat(
        transitionSpec = { tween(600, delayMillis = if (targetState) 25 else 25, easing = FastOutSlowInEasing) },
        label = "item1Offset"
    ) { if (it) 0f else -1000f }
    val item2OffsetY by transition.animateFloat(
        transitionSpec = { tween(600, delayMillis = if (targetState) 0 else 50, easing = FastOutSlowInEasing) },
        label = "item2Offset"
    ) { if (it) 0f else -1000f }

    if (!transitionState.currentState && !transitionState.targetState) return
    val isExpanded = menuState != MenuState.MENU
    val panelWidthFraction by animateFloatAsState(
        targetValue = if (isExpanded) 0.92f else 0.88f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium),
        label = "panelWidth"
    )
    val panelHeightFraction by animateFloatAsState(
        targetValue = if (isExpanded) 0.88f else 0.56f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium),
        label = "panelHeight"
    )

    LaunchedEffect(Unit) {
        lentRepository.refresh()
    }

    val bgAlpha by transition.animateFloat(
        transitionSpec = { tween(800, easing = FastOutSlowInEasing) },
        label = "hamburgerBgAlpha"
    ) { if (it) 0.4f else 0f }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = bgAlpha))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {
                        if (menuState == MenuState.MENU) {
                            HamburgerMenuState.isOpen = false
                            menuState = MenuState.MENU
                        } else {
                            menuState = MenuState.MENU
                        }
                    }
                )
        )

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val panelWidth  = maxWidth  * panelWidthFraction
            val panelHeight = maxHeight * panelHeightFraction
            val cornerRadius by animateFloatAsState(
                targetValue = if (isExpanded) 20f else 24f,
                animationSpec = tween(400),
                label = "cornerRadius"
            )

            Box(
                modifier = Modifier
                    .width(panelWidth)
                    .height(panelHeight)
                    .align(Alignment.Center)          
                    .graphicsLayer { translationX = containerOffsetX }
                    .clip(RoundedCornerShape(cornerRadius.dp)) 
                    .background(Color.White)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    )
            ) {
                AnimatedContent(
                    targetState = menuState,
                    transitionSpec = {
                        when {
                            targetState.ordinal > initialState.ordinal ->
                                (slideInHorizontally { it } + fadeIn()).togetherWith(slideOutHorizontally { -it } + fadeOut())
                            else ->
                                (slideInHorizontally { -it } + fadeIn()).togetherWith(slideOutHorizontally { it } + fadeOut())
                        }
                    },
                    label = "menuStateTransition"
                ) { state ->
                    when (state) {
                        MenuState.MENU -> MenuPanel(
                            onPrestadosClick = { menuState = MenuState.LENT_LIST },
                            onClose = { HamburgerMenuState.isOpen = false }
                        )
                        MenuState.LENT_LIST -> LentListPanel(
                            lentItems = lentItems,
                            onBack = { menuState = MenuState.MENU },
                            onAddClick = { menuState = MenuState.ADD_LENT },
                            onItemClick = { item ->
                                selectedLentItem = item
                                menuState = MenuState.LENT_DETAIL
                            }
                        )
                        MenuState.ADD_LENT -> AddLentPanel(
                            allGarments = allGarments,
                            onBack = { menuState = MenuState.LENT_LIST },
                            onSave = { garmentId, garmentImageUrl, garmentName, borrowerName, lentDate, reclaimDate, reclaimDateMillis ->
                                coroutineScope.launch {
                                    val newItem = lentRepository.createLentItem(
                                        garmentId = garmentId,
                                        garmentImageUrl = garmentImageUrl,
                                        garmentName = garmentName,
                                        borrowerName = borrowerName,
                                        lentDate = lentDate,
                                        reclaimDate = reclaimDate,
                                        reclaimDateMillis = reclaimDateMillis
                                    )
                                    val garment = allGarments.find { it.id == garmentId }
                                    if (garment != null) {
                                        garmentRepository.updateGarment(garment.copy(status = "LENT"))
                                    }
                                    scheduleReclaimNotification(context, newItem)
                                    menuState = MenuState.LENT_LIST
                                }
                            }
                        )
                        MenuState.LENT_DETAIL -> LentDetailPanel(
                            lentItem = selectedLentItem,
                            onBack = { menuState = MenuState.LENT_LIST },
                            onReturned = { item ->
                                coroutineScope.launch {
                                    lentRepository.markReturned(item.id)
                                    val garment = allGarments.find { it.id == item.garmentId }
                                    if (garment != null) {
                                        garmentRepository.updateGarment(garment.copy(status = "AVAILABLE"))
                                    }
                                    cancelReclaimNotification(context, item.id)
                                    menuState = MenuState.LENT_LIST
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}









