package com.pdm0126.outfit

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.NavKey
import com.pdm0126.outfit.ui.theme.GlassWhite
import com.pdm0126.outfit.ui.theme.LimeGreen
import kotlinx.serialization.Serializable
import kotlinx.coroutines.launch

@Serializable
enum class OutFixScreen(val title: String, val icon: ImageVector) : NavKey {
    Home("Home", Icons.Default.Home),
    OutfitEditor("Editor", Icons.Default.Edit),
    WeeklyPlanner("Planner", Icons.Default.DateRange),
    Closet("Closet", Icons.Default.Checkroom),
    Profile("Profile", Icons.Default.Person)
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
        snapshotFlow { pagerState.currentPage }.collect { page ->
            val screen = screens[page]
            if (navigationState.topLevelRoute != screen) {
                navigationState.topLevelRoute = screen
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFFEDDDCC) 
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            HorizontalPager(
                state = pagerState,
                flingBehavior = flingBehavior,
                modifier = Modifier.fillMaxSize(),
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
    onItemSelected: (OutFixScreen) -> Unit
) {
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
                .blur(30.dp)
                .clip(RoundedCornerShape(36.dp))
                .background(Color.White.copy(alpha = 0.1f))
        )

        Box(
            modifier = Modifier
                .height(72.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(36.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.35f),
                            Color.White.copy(alpha = 0.15f)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.5f),
                            Color.White.copy(alpha = 0.1f)
                        )
                    ),
                    shape = RoundedCornerShape(36.dp)
                )
        )

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
                .height(72.dp)
                .padding(6.dp)
                .clip(RoundedCornerShape(36.dp))
                .background(LimeGreen.copy(alpha = 0.25f))
        )

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
                            tint = if (isSelected) LimeGreen else Color.Black.copy(alpha = 0.5f),
                            modifier = Modifier.size(if (isSelected) 28.dp else 24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HomeScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Welcome to Outfit",
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFF423D38),
            modifier = Modifier.padding(top = 16.dp, bottom = 16.dp)
        )
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            items(10) { index ->
                Box(
                    modifier = Modifier
                        .aspectRatio(0.8f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (index % 2 == 0) LimeGreen.copy(alpha = 0.3f) 
                            else Color.White.copy(alpha = 0.8f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Outfit #$index", fontWeight = FontWeight.Medium)
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

@Composable fun OutfitEditorScreen() = ScreenPlaceholder(OutFixScreen.OutfitEditor)
@Composable fun WeeklyPlannerScreen() = ScreenPlaceholder(OutFixScreen.WeeklyPlanner)
@Composable fun ClosetScreen() = ScreenPlaceholder(OutFixScreen.Closet)
@Composable fun ProfileScreen() = ScreenPlaceholder(OutFixScreen.Profile)
