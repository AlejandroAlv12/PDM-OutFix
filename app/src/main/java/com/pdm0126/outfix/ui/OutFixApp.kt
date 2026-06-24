package com.pdm0126.outfix.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.NavKey
import com.pdm0126.outfix.ui.theme.LimeGreen
import kotlinx.serialization.Serializable
import kotlinx.coroutines.launch
import android.os.Build
import androidx.compose.ui.graphics.asComposeRenderEffect
import com.pdm0126.outfix.screens.home.HomeScreen
import com.pdm0126.outfix.screens.editor.OutfitEditorScreen
import com.pdm0126.outfix.screens.planner.WeeklyPlannerScreen
import com.pdm0126.outfix.screens.closet.ClosetScreen
import com.pdm0126.outfix.screens.profile.ProfileScreen
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.foundation.gestures.scrollBy
import kotlin.math.roundToInt

@Serializable
enum class OutFixScreen(val title: String, val icon: ImageVector) : NavKey {
    Home("Home", Icons.Outlined.Home),
    OutfitEditor("Editor", Icons.Outlined.Checkroom),
    WeeklyPlanner("Planner", Icons.Outlined.CalendarToday),
    Closet("Closet", Icons.Outlined.DeleteOutline),
    Profile("Profile", Icons.Outlined.Person)
}

@Composable
fun MainScreen() {
    val screens = OutFixScreen.entries
    val navigationState = rememberNavigationState(
        startRoute = OutFixScreen.Home,
        topLevelRoutes = screens.toSet()
    )
    val navigator = remember { Navigator(navigationState) }

    val pagerState = rememberPagerState(pageCount = { screens.size })
    val flingBehavior = PagerDefaults.flingBehavior(
        state = pagerState,
        snapPositionalThreshold = 0.8f
    )

    LaunchedEffect(navigationState.topLevelRoute) {
        val index = screens.indexOf(navigationState.topLevelRoute)
        if (pagerState.currentPage != index) {
            pagerState.animateScrollToPage(index)
        }
    }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }.collect { page ->
            val screen = screens[page]
            if (navigationState.topLevelRoute != screen) {
                navigationState.topLevelRoute = screen
            }
        }
    }

    val backgroundLayer = rememberGraphicsLayer()
    var pagerCoords: LayoutCoordinates? by remember { mutableStateOf(null) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFFEDDDCC)
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            HorizontalPager(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFEDDDCC))
                    .onGloballyPositioned { pagerCoords = it }
                    .drawWithContent {
                        backgroundLayer.record {
                            drawRect(Color(0xFFEDDDCC))
                            this@drawWithContent.drawContent()
                        }
                        drawLayer(backgroundLayer)
                    },
                state = pagerState,
                flingBehavior = flingBehavior,
                contentPadding = PaddingValues(top = innerPadding.calculateTopPadding())
            ) { page ->
                when (screens[page]) {
                    OutFixScreen.Home -> HomeScreen()
                    OutFixScreen.OutfitEditor -> OutfitEditorScreen()
                    OutFixScreen.WeeklyPlanner -> WeeklyPlannerScreen()
                    OutFixScreen.Closet -> ClosetScreen()
                    OutFixScreen.Profile -> ProfileScreen()
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = innerPadding.calculateBottomPadding())
            ) {
                FloatingBottomNavBar(
                    screens = screens,
                    navigationState = navigationState,
                    pagerState = pagerState,
                    backgroundLayer = backgroundLayer,
                    pagerCoords = pagerCoords,
                    onItemSelected = { screen ->
                        navigator.navigate(screen)
                    }
                )
            }

            var fabGlassOffset by remember { mutableStateOf(Offset.Zero) }
            var showScanner by remember { mutableStateOf(false) }
            var capturedImagePath by remember { mutableStateOf<String?>(null) }
            var capturedCategory by remember { mutableStateOf("") }
            var capturedColors by remember { mutableStateOf<List<androidx.compose.ui.graphics.Color>>(emptyList()) }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 28.dp, bottom = innerPadding.calculateBottomPadding() + 110.dp)
                    .size(72.dp)
                    .onGloballyPositioned { coords ->
                        if (pagerCoords != null) {
                            fabGlassOffset = pagerCoords!!.localPositionOf(coords, Offset.Zero)
                        }
                    }
                    .clip(CircleShape)
                    .clickable { showScanner = true },
                contentAlignment = Alignment.Center
            ) {
                if (Build.VERSION.SDK_INT >= 31) {
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
                        translate(left = -fabGlassOffset.x, top = -fabGlassOffset.y) {
                            drawLayer(backgroundLayer)
                        }
                    }
                } else {
                    Box(modifier = Modifier.fillMaxSize().background(LimeGreen))
                }

                Box(modifier = Modifier.fillMaxSize().background(LimeGreen.copy(alpha = 0.40f)))
                
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "Scan Garment",
                    tint = Color.White,
                    modifier = Modifier.size(50.dp)
                )
            }

            if (showScanner) {
                com.pdm0126.outfix.screens.scan.ScanGarmentScreen(
                    onClose = { showScanner = false },
                    onImageCaptured = { imagePath, category, colors ->
                        showScanner = false
                        capturedImagePath = imagePath
                        capturedCategory = category
                        capturedColors = colors
                    }
                )
            }

            capturedImagePath?.let { imagePath ->
                com.pdm0126.outfix.screens.scan.NewGarmentScreen(
                    imagePath = imagePath,
                    detectedCategory = capturedCategory,
                    detectedColors = capturedColors,
                    onBack = { 
                        capturedImagePath = null
                        showScanner = true
                    },
                    onSave = {
                        capturedImagePath = null
                    }
                )
            }
        }
    }
}

@Composable
fun FloatingBottomNavBar(
    screens: List<OutFixScreen>,
    navigationState: NavigationState,
    pagerState: PagerState,
    backgroundLayer: androidx.compose.ui.graphics.layer.GraphicsLayer,
    pagerCoords: LayoutCoordinates?,
    onItemSelected: (OutFixScreen) -> Unit
) {
    var glassOffset by remember { mutableStateOf(Offset.Zero) }
    val coroutineScope = rememberCoroutineScope()
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 24.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        val maxWidth = maxWidth
        val itemWidth = maxWidth / screens.size
        val indicatorOffset by remember {
            derivedStateOf {
                val position = pagerState.currentPage + pagerState.currentPageOffsetFraction
                itemWidth * position
            }
        }

        Box(
            modifier = Modifier
                .height(72.dp)
                .fillMaxWidth()
                .onGloballyPositioned { coords ->
                    if (pagerCoords != null) {
                        glassOffset = pagerCoords.localPositionOf(coords, Offset.Zero)
                    }
                }
        ) {
            if (Build.VERSION.SDK_INT >= 31) {
                androidx.compose.foundation.Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .liquidGlass(
                            blur = 18f,
                            saturation = 1.5f,
                            refraction = 0.55f,
                            curve = 0.50f,
                            dispersion = 0.25f
                        )
                ) {
                    translate(left = -glassOffset.x, top = -glassOffset.y) {
                        drawLayer(backgroundLayer)
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize().background(Color(0xFF2F2F2F)))
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(36.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.15f),
                                Color.Black.copy(alpha = 0.25f)
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.35f),
                                Color.White.copy(alpha = 0.05f)
                            )
                        ),
                        shape = RoundedCornerShape(36.dp)
                    )
            )
        }

        Row(
            modifier = Modifier
                .height(72.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val position = pagerState.currentPage + pagerState.currentPageOffsetFraction
            screens.forEachIndexed { index, screen ->
                val distance = kotlin.math.abs(position - index).coerceIn(0f, 1f)
                val iconSize = 34.dp - ((34.dp - 26.dp) * distance)
                
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onItemSelected(screen) }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = screen.icon,
                        contentDescription = screen.title,
                        tint = Color.White,
                        modifier = Modifier.size(iconSize)
                    )
                }
            }
        }
        
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = indicatorOffset)
                .width(itemWidth)
                .height(72.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                coroutineScope.launch {
                                    val closestPage = (pagerState.currentPage + pagerState.currentPageOffsetFraction).roundToInt()
                                    pagerState.animateScrollToPage(closestPage)
                                }
                            }
                        ) { change, dragAmount ->
                            change.consume()
                            val scrollAmount = dragAmount * (screenWidthPx / itemWidth.toPx())
                            coroutineScope.launch {
                                pagerState.scrollBy(scrollAmount)
                            }
                        }
                    }
            ) {
                if (Build.VERSION.SDK_INT >= 31) {
                    androidx.compose.foundation.Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .liquidGlass(
                                blur = 10f,
                                saturation = 1.5f,
                                refraction = 0.8f,
                                curve = 0.6f,
                                dispersion = 0.15f,
                                normalizedRadius = 0.5f
                            )
                    ) {
                        val ballOffsetX = indicatorOffset.toPx() + (itemWidth.toPx() - 56.dp.toPx()) / 2f
                        val ballOffsetY = (72.dp.toPx() - 56.dp.toPx()) / 2f
                        translate(
                            left = -glassOffset.x - ballOffsetX,
                            top = -glassOffset.y - ballOffsetY
                        ) {
                            drawLayer(backgroundLayer)
                        }
                    }
                } else {
                    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.35f)))
                }

                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)))

                Box(modifier = Modifier.fillMaxSize().border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.2f),
                    shape = CircleShape
                ))
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .graphicsLayer {
                    clip = true
                    shape = object : androidx.compose.ui.graphics.Shape {
                        override fun createOutline(
                            size: androidx.compose.ui.geometry.Size,
                            layoutDirection: androidx.compose.ui.unit.LayoutDirection,
                            density: androidx.compose.ui.unit.Density
                        ): androidx.compose.ui.graphics.Outline {
                            val centerX = indicatorOffset.toPx() + (itemWidth.toPx() / 2f)
                            val centerY = size.height / 2f
                            val radius = 28.dp.toPx()
                            return androidx.compose.ui.graphics.Outline.Generic(androidx.compose.ui.graphics.Path().apply {
                                addOval(androidx.compose.ui.geometry.Rect(centerX - radius, centerY - radius, centerX + radius, centerY + radius))
                            })
                        }
                    }
                }
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val position = pagerState.currentPage + pagerState.currentPageOffsetFraction
                screens.forEachIndexed { index, screen ->
                    val distance = kotlin.math.abs(position - index).coerceIn(0f, 1f)
                    val iconSize = 34.dp - ((34.dp - 26.dp) * distance)
                    
                    Box(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = screen.icon,
                            contentDescription = null,
                            tint = Color(0xFFDCB888),
                            modifier = Modifier.size(iconSize)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ScreenPlaceholder(screen: OutFixScreen) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = screen.icon,
                contentDescription = null,
                modifier = Modifier.size(100.dp),
                tint = LimeGreen.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = screen.title,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFDCB888)
            )
        }
    }
}