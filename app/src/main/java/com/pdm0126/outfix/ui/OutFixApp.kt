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

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 24.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        val maxWidth = maxWidth

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
                Box(modifier = Modifier.fillMaxSize().background(Color(0xFFE0E0E0)))
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

        val itemWidth = maxWidth / screens.size
        val indicatorOffset by remember {
            derivedStateOf {
                val position = pagerState.currentPage + pagerState.currentPageOffsetFraction
                itemWidth * position
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
                    .background(Color.Black.copy(alpha = 0.35f))
            )
        }

        Row(
            modifier = Modifier
                .height(72.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            screens.forEach { screen ->
                val isSelected = navigationState.topLevelRoute == screen

                IconButton(
                    onClick = { onItemSelected(screen) },
                    modifier = Modifier.weight(1f).fillMaxHeight()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = screen.icon,
                            contentDescription = screen.title,
                            tint = if (isSelected) Color(0xFFFBEBB5) else Color.White,
                            modifier = Modifier.size(if (isSelected) 30.dp else 26.dp)
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
                color = Color(0xFF423D38)
            )
        }
    }
}