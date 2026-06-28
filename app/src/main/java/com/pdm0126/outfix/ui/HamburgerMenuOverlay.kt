package com.pdm0126.outfix.ui

import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
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

// ─── Menu navigation states ──────────────────────────────────────────────────
private enum class MenuState {
    MENU,       // Small panel — menu items
    LENT_LIST,  // Expanded — borrowed items list
    ADD_LENT,   // Add new loan
    LENT_DETAIL // Detail of a loan
}

// ─── Global hamburger state ──────────────────────────────────────────────────
object HamburgerMenuState {
    var isOpen by androidx.compose.runtime.mutableStateOf(false)
}

// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HamburgerMenuOverlay(
    appBackgroundLayer: androidx.compose.ui.graphics.layer.GraphicsLayer? = null
) {
    val isOpen = HamburgerMenuState.isOpen
    if (!isOpen) return

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val lentRepository = OutfixApplication.instance.lentRepository
    val garmentRepository = OutfixApplication.instance.garmentRepository
    val lentItems by lentRepository.lentItemsFlow.collectAsState()
    val allGarments by garmentRepository.garmentsFlow.collectAsState(initial = emptyList())

    var menuState by remember { mutableStateOf(MenuState.MENU) }
    var selectedLentItem by remember { mutableStateOf<LentItem?>(null) }

    // Blur background
    val bgAlpha by animateFloatAsState(
        targetValue = if (isOpen) 0.45f else 0f,
        animationSpec = tween(400),
        label = "hamburgerBgAlpha"
    )

    // Panel slide and size
    val isExpanded = menuState != MenuState.MENU
    val panelWidthFraction by animateFloatAsState(
        targetValue = if (isExpanded) 1f else 0.72f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "panelWidth"
    )
    val panelHeightFraction by animateFloatAsState(
        targetValue = if (isExpanded) 1f else 0.45f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "panelHeight"
    )

    // Slide-in from left
    val slideIn = slideInHorizontally(
        initialOffsetX = { -it },
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow)
    ) + fadeIn(animationSpec = tween(200))
    val slideOut = slideOutHorizontally(
        targetOffsetX = { -it },
        animationSpec = tween(300, easing = FastOutLinearInEasing)
    ) + fadeOut(animationSpec = tween(200))

    LaunchedEffect(Unit) {
        lentRepository.refresh()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Dim overlay — dismiss on click
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
                        } else {
                            menuState = MenuState.MENU
                        }
                    }
                )
        )

        // Panel
        AnimatedVisibility(
            visible = isOpen,
            enter = slideIn,
            exit = slideOut,
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val panelWidth = maxWidth * panelWidthFraction
                val panelHeight = maxHeight * panelHeightFraction

                Box(
                    modifier = Modifier
                        .width(panelWidth)
                        .height(panelHeight)
                        .align(Alignment.CenterStart)
                        .clip(
                            RoundedCornerShape(
                                topStart = 0.dp,
                                bottomStart = 0.dp,
                                topEnd = if (isExpanded) 0.dp else 24.dp,
                                bottomEnd = if (isExpanded) 0.dp else 24.dp
                            )
                        )
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
                                onBack = {
                                    menuState = MenuState.MENU
                                },
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
                                        // Mark garment as LENT
                                        val garment = allGarments.find { it.id == garmentId }
                                        if (garment != null) {
                                            garmentRepository.updateGarment(garment.copy(status = "LENT"))
                                        }
                                        // Schedule reclaim notification
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
                                        // Restore garment to AVAILABLE
                                        val garment = allGarments.find { it.id == item.garmentId }
                                        if (garment != null) {
                                            garmentRepository.updateGarment(garment.copy(status = "AVAILABLE"))
                                        }
                                        // Cancel pending notification
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
}

// ─── Panel 1: Main menu ───────────────────────────────────────────────────────
@Composable
private fun MenuPanel(
    onPrestadosClick: () -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 32.dp, horizontal = 24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        // App title
        Text(
            text = "OutFix",
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            color = Color.Black,
            fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        MenuOptionRow(label = "Prestados", onClick = onPrestadosClick)
        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.4f))
        MenuOptionRow(label = "Administrar", onClick = { /* TODO */ })
        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.4f))
        MenuOptionRow(label = "About US", onClick = { /* TODO */ })
    }
}

@Composable
private fun MenuOptionRow(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .bouncyClickable(onClick = onClick)
            .padding(vertical = 18.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = label,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black
        )
    }
}

// ─── Panel 2: Lent items list ─────────────────────────────────────────────────
@Composable
private fun LentListPanel(
    lentItems: List<LentItem>,
    onBack: () -> Unit,
    onAddClick: () -> Unit,
    onItemClick: (LentItem) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(40.dp)
                    .bouncyClickable(onClick = onBack)
                    .clip(CircleShape)
                    .background(Color(0xFFEEEEEE)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.ChevronLeft,
                    contentDescription = "Regresar",
                    tint = Color.Black,
                    modifier = Modifier.size(28.dp)
                )
            }

            Text(
                text = "Prestados",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.align(Alignment.Center)
            )

            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(40.dp)
                    .bouncyClickable(onClick = onAddClick)
                    .clip(CircleShape)
                    .background(LimeGreen),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "Agregar préstamo",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.4f))

        if (lentItems.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Rounded.Checkroom,
                        contentDescription = null,
                        tint = Color.LightGray,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Sin prendas prestadas",
                        fontSize = 16.sp,
                        color = Color.LightGray,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(lentItems, key = { it.id }) { item ->
                    LentItemCard(item = item, onClick = { onItemClick(item) })
                }
            }
        }
    }
}

@Composable
private fun LentItemCard(item: LentItem, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .bouncyClickable(onClick = onClick)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF6F6F6))
            .border(1.dp, Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Garment image
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFEEEEEE)),
                contentAlignment = Alignment.Center
            ) {
                if (!item.garmentImageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = item.garmentImageUrl,
                        contentDescription = item.garmentName,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize().padding(4.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Rounded.Checkroom,
                        contentDescription = null,
                        tint = Color.LightGray,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.borrowerName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                // Reclaim badge
                val badgeColor = if (item.isReturned) LimeGreen else Color(0xFFFFC107)
                val badgeText = if (item.isReturned) "Reclamado ✓" else "Reclamar: ${item.reclaimDate}"
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(badgeColor)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = badgeText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

// ─── Panel 3: Add lent item ───────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddLentPanel(
    allGarments: List<GarmentResponse>,
    onBack: () -> Unit,
    onSave: (garmentId: String, garmentImageUrl: String?, garmentName: String, borrowerName: String, lentDate: String, reclaimDate: String, reclaimDateMillis: Long) -> Unit
) {
    var selectedGarment by remember { mutableStateOf<GarmentResponse?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var borrowerName by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }

    // Dates
    val today = remember { Calendar.getInstance() }
    val todayFormatted = remember {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(today.time)
    }
    var reclaimDateMillis by remember { mutableStateOf(today.timeInMillis + 7L * 24 * 60 * 60 * 1000) } // default: 1 week
    val reclaimDateFormatted = remember(reclaimDateMillis) {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(reclaimDateMillis))
    }
    val reclaimDateShort = remember(reclaimDateMillis) {
        SimpleDateFormat("dd/MM", Locale.getDefault()).format(Date(reclaimDateMillis))
    }
    val todayShort = remember {
        SimpleDateFormat("dd/MM", Locale.getDefault()).format(today.time)
    }

    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = reclaimDateMillis)

    // Filter garments available for lending (not already LENT)
    val availableForLending = remember(allGarments, searchQuery) {
        allGarments
            .filter { it.status == "AVAILABLE" || it.status == "clean" || it.status.isBlank() }
            .filter { g ->
                searchQuery.isBlank() ||
                g.name.contains(searchQuery, ignoreCase = true) ||
                g.category.contains(searchQuery, ignoreCase = true)
            }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { reclaimDateMillis = it }
                    showDatePicker = false
                }) { Text("Confirmar") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(40.dp)
                    .bouncyClickable(onClick = onBack)
                    .clip(CircleShape)
                    .background(Color(0xFFEEEEEE)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.ChevronLeft,
                    contentDescription = "Regresar",
                    tint = Color.Black,
                    modifier = Modifier.size(28.dp)
                )
            }

            Text(
                text = "Prestados",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.align(Alignment.Center)
            )

            // Confirm button (checkmark)
            val canSave = selectedGarment != null && borrowerName.isNotBlank()
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(40.dp)
                    .bouncyClickable(enabled = canSave) {
                        val g = selectedGarment ?: return@bouncyClickable
                        onSave(
                            g.id,
                            g.imageUrl,
                            g.name,
                            borrowerName,
                            todayShort,
                            reclaimDateShort,
                            reclaimDateMillis
                        )
                    }
                    .clip(CircleShape)
                    .background(if (canSave) LimeGreen else Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = "Guardar",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.4f))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Garment selector
            Text("Seleccionar prenda", fontSize = 13.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)

            // Search field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar prenda...", fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LimeGreen,
                    unfocusedBorderColor = Color.LightGray
                )
            )

            // Selected garment preview
            if (selectedGarment != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(LimeGreen.copy(alpha = 0.1f))
                        .border(1.5.dp, LimeGreen, RoundedCornerShape(12.dp))
                        .padding(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(
                            modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFF0F0F0)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!selectedGarment!!.imageUrl.isNullOrBlank()) {
                                AsyncImage(model = selectedGarment!!.imageUrl, contentDescription = null, contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize())
                            } else {
                                Icon(Icons.Rounded.Checkroom, contentDescription = null, tint = Color.LightGray)
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(selectedGarment!!.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text(selectedGarment!!.category, fontSize = 12.sp, color = Color.Gray)
                        }
                        Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = LimeGreen)
                    }
                }
            }

            // Garment list
            if (availableForLending.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No hay prendas disponibles", fontSize = 14.sp, color = Color.LightGray)
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    availableForLending.take(8).forEach { garment ->
                        val isSelected = selectedGarment?.id == garment.id
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .bouncyClickable { selectedGarment = if (isSelected) null else garment }
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) LimeGreen.copy(alpha = 0.12f) else Color(0xFFF6F6F6))
                                .border(
                                    1.dp,
                                    if (isSelected) LimeGreen else Color.LightGray.copy(alpha = 0.3f),
                                    RoundedCornerShape(10.dp)
                                )
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier.size(44.dp).clip(RoundedCornerShape(8.dp)).background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                if (!garment.imageUrl.isNullOrBlank()) {
                                    AsyncImage(model = garment.imageUrl, contentDescription = null, contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize())
                                } else {
                                    Icon(Icons.Rounded.Checkroom, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(24.dp))
                                }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(garment.name, fontSize = 14.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(garment.category, fontSize = 12.sp, color = Color.Gray)
                            }
                            if (isSelected) {
                                Icon(Icons.Rounded.Check, contentDescription = null, tint = LimeGreen, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.4f))

            // Borrower name field
            OutlinedTextField(
                value = borrowerName,
                onValueChange = { borrowerName = it },
                label = { Text("Nombre del prestatario") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LimeGreen,
                    unfocusedBorderColor = Color.LightGray,
                    focusedLabelColor = LimeGreen
                )
            )

            // Reclaim date
            OutlinedTextField(
                value = reclaimDateFormatted,
                onValueChange = {},
                readOnly = true,
                label = { Text("Fecha de reclamación") },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.DateRange,
                        contentDescription = "Elegir fecha",
                        modifier = Modifier.clickable { showDatePicker = true },
                        tint = LimeGreen
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { showDatePicker = true }
                    ),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LimeGreen,
                    unfocusedBorderColor = Color.LightGray,
                    focusedLabelColor = LimeGreen
                )
            )

            // Today's date (read-only)
            OutlinedTextField(
                value = todayFormatted,
                onValueChange = {},
                readOnly = true,
                label = { Text("Fecha actual") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    disabledBorderColor = Color.LightGray,
                    unfocusedBorderColor = Color.LightGray,
                    unfocusedLabelColor = Color.Gray
                )
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ─── Panel 4: Lent item detail ────────────────────────────────────────────────
@Composable
private fun LentDetailPanel(
    lentItem: LentItem?,
    onBack: () -> Unit,
    onReturned: (LentItem) -> Unit
) {
    val item = lentItem ?: return

    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(40.dp)
                    .bouncyClickable(onClick = onBack)
                    .clip(CircleShape)
                    .background(Color(0xFFEEEEEE)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.ChevronLeft,
                    contentDescription = "Regresar",
                    tint = Color.Black,
                    modifier = Modifier.size(28.dp)
                )
            }

            Text(
                text = "Prestados",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.4f))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Garment image (large)
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFF0F0F0)),
                contentAlignment = Alignment.Center
            ) {
                if (!item.garmentImageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = item.garmentImageUrl,
                        contentDescription = item.garmentName,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize().padding(8.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Rounded.Checkroom,
                        contentDescription = null,
                        tint = Color.LightGray,
                        modifier = Modifier.size(72.dp)
                    )
                }
            }

            // Info card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFF6F6F6))
                    .border(1.dp, Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LentDetailRow("Prenda", item.garmentName)
                LentDetailRow("A", item.borrowerName)
                LentDetailRow("Prestado", item.lentDate)
                LentDetailRow("Reclamar", item.reclaimDate)
                if (item.isReturned) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(LimeGreen.copy(alpha = 0.15f))
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Devuelto ✓", color = LimeGreen, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }

            // Devuelto button
            if (!item.isReturned) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .bouncyClickable { onReturned(item) }
                        .clip(RoundedCornerShape(14.dp))
                        .background(LimeGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Devuelto", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                        Icon(Icons.Rounded.Check, contentDescription = null, tint = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun LentDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 13.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.Black)
    }
}

// ─── Local notification helper ────────────────────────────────────────────────
fun scheduleReclaimNotification(context: Context, item: LentItem) {
    // Only schedule if in the future
    val delayMs = item.reclaimDateMillis - System.currentTimeMillis()
    if (delayMs <= 0) return

    val workRequest = androidx.work.OneTimeWorkRequestBuilder<ReclaimNotificationWorker>()
        .setInitialDelay(delayMs, java.util.concurrent.TimeUnit.MILLISECONDS)
        .setInputData(
            androidx.work.workDataOf(
                "item_id" to item.id,
                "borrower_name" to item.borrowerName,
                "garment_name" to item.garmentName
            )
        )
        .addTag("reclaim_${item.id}")
        .build()

    androidx.work.WorkManager.getInstance(context).enqueue(workRequest)
}

fun cancelReclaimNotification(context: Context, itemId: String) {
    androidx.work.WorkManager.getInstance(context).cancelAllWorkByTag("reclaim_$itemId")
}
