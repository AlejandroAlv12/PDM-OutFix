package com.pdm0126.outfix.screens.menu

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
import com.pdm0126.outfix.data.api.dto.GarmentResponse
import com.pdm0126.outfix.data.local.LentItem
import com.pdm0126.outfix.ui.AppViewModel
import com.pdm0126.outfix.ui.theme.LimeGreen
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.SimpleDateFormat
import java.util.*






@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HamburgerMenuOverlay(
    appBackgroundLayer: androidx.compose.ui.graphics.layer.GraphicsLayer? = null,
    appViewModel: AppViewModel,
    menuViewModel: MenuViewModel = hiltViewModel()
) {
    val appState by appViewModel.uiState.collectAsState()
    val isOpen = appState.isHamburgerOpen
    val context = androidx.compose.ui.platform.LocalContext.current
    val lentItems by menuViewModel.lentItems.collectAsState()
    val allGarments by menuViewModel.garments.collectAsState()
    val plannerDays by menuViewModel.plannerDays.collectAsState()
    val menuState by menuViewModel.menuState.collectAsState()
    val selectedLentItem by menuViewModel.selectedLentItem.collectAsState()

    LaunchedEffect(isOpen, appState.targetLentItem) {
        val target = appState.targetLentItem
        if (target != null && isOpen) {
            menuViewModel.navigateTo(MenuState.MENU)
            kotlinx.coroutines.delay(600)
            menuViewModel.navigateTo(MenuState.LENT_LIST)
            kotlinx.coroutines.delay(400)
            menuViewModel.selectLentItem(target)
            menuViewModel.navigateTo(MenuState.LENT_DETAIL)
            appViewModel.clearTargetLentItem()
        }
    }

    androidx.activity.compose.BackHandler(enabled = isOpen) {
        when (menuState) {
            MenuState.MENU -> appViewModel.closeHamburgerMenu()
            MenuState.LENT_LIST -> menuViewModel.navigateTo(MenuState.MENU)
            MenuState.ADD_LENT, MenuState.LENT_DETAIL -> menuViewModel.navigateTo(MenuState.LENT_LIST)
        }
    }

    LaunchedEffect(Unit) { menuViewModel.refresh() }

    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val density = androidx.compose.ui.platform.LocalDensity.current.density
    val screenWidthPx = configuration.screenWidthDp * density

    val transitionState = remember { MutableTransitionState(false) }
    transitionState.targetState = isOpen
    val transition = updateTransition(transitionState, label = "HamburgerMenuTransition")
    val containerOffsetX by transition.animateFloat(
        transitionSpec = { tween(600, easing = FastOutSlowInEasing) },
        label = "containerOffset"
    ) { if (it) 0f else -screenWidthPx }

    val titleOffsetY by transition.animateFloat(
        transitionSpec = { tween(600, delayMillis = if (targetState) 50 else 0, easing = FastOutSlowInEasing) },
        label = "titleOffset"
    ) { if (it) 0f else -screenWidthPx }

    val item1OffsetY by transition.animateFloat(
        transitionSpec = { tween(600, delayMillis = if (targetState) 25 else 25, easing = FastOutSlowInEasing) },
        label = "item1Offset"
    ) { if (it) 0f else -screenWidthPx }
    val item2OffsetY by transition.animateFloat(
        transitionSpec = { tween(600, delayMillis = if (targetState) 0 else 50, easing = FastOutSlowInEasing) },
        label = "item2Offset"
    ) { if (it) 0f else -screenWidthPx }

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
                        when (menuState) {
                            MenuState.MENU -> appViewModel.closeHamburgerMenu()
                            MenuState.LENT_LIST -> menuViewModel.navigateTo(MenuState.MENU)
                            MenuState.ADD_LENT, MenuState.LENT_DETAIL -> menuViewModel.navigateTo(MenuState.LENT_LIST)
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
                            onPrestadosClick = { menuViewModel.navigateTo(MenuState.LENT_LIST) },
                            onClose = { appViewModel.closeHamburgerMenu() }
                        )
                        MenuState.LENT_LIST -> LentListPanel(
                            lentItems = lentItems,
                            onBack = { menuViewModel.navigateTo(MenuState.MENU) },
                            onAddClick = { menuViewModel.navigateTo(MenuState.ADD_LENT) },
                            onItemClick = { item ->
                                menuViewModel.selectLentItem(item)
                                menuViewModel.navigateTo(MenuState.LENT_DETAIL)
                            }
                        )
                        MenuState.ADD_LENT -> AddLentPanel(
                            allGarments = allGarments,
                            plannerDays = plannerDays,
                            lentItems = lentItems,
                            onBack = { menuViewModel.navigateTo(MenuState.LENT_LIST) },
                            onSave = { garmentId, garmentImageUrl, garmentName, borrowerName, lentDate, reclaimDate, reclaimDateMillis ->
                                menuViewModel.createLentItem(
                                    garmentId = garmentId,
                                    garmentImageUrl = garmentImageUrl,
                                    garmentName = garmentName,
                                    borrowerName = borrowerName,
                                    lentDate = lentDate,
                                    reclaimDate = reclaimDate,
                                    reclaimDateMillis = reclaimDateMillis,
                                    onCreated = { newItem ->
                                        scheduleReclaimNotification(context, newItem)
                                    }
                                )
                            }
                        )
                        MenuState.LENT_DETAIL -> LentDetailPanel(
                            lentItem = selectedLentItem,
                            onBack = { menuViewModel.navigateTo(MenuState.LENT_LIST) },
                            onReturned = { item ->
                                menuViewModel.markReturned(item) { cancelReclaimNotification(context, item.id) }
                            }
                        )
                    }
                }
            }
        }
    }
}
