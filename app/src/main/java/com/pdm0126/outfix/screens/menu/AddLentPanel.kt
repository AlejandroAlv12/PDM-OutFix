package com.pdm0126.outfix.screens.menu

import android.content.Context
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.pdm0126.outfix.data.api.dto.GarmentResponse
import com.pdm0126.outfix.data.local.LentItem
import com.pdm0126.outfix.ui.bouncyClickable
import com.pdm0126.outfix.ui.theme.LimeGreen
import com.pdm0126.outfix.OutfixApplication
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLentPanel(
    allGarments: List<GarmentResponse>,
    onBack: () -> Unit,
    onSave: (garmentId: String, garmentImageUrl: String?, garmentName: String, borrowerName: String, lentDate: String, reclaimDate: String, reclaimDateMillis: Long) -> Unit
) {
    var selectedGarment by remember { mutableStateOf<GarmentResponse?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var borrowerName by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current

    val today = remember { Calendar.getInstance() }
    val todayFormatted = remember {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(today.time)
    }
    var reclaimDateMillis by remember { mutableStateOf(today.timeInMillis + 7L * 24 * 60 * 60 * 1000) }
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

    val plannerDays by OutfixApplication.instance.plannerRepository.plannerDaysFlow.collectAsState(initial = emptyList())
    val plannerGarmentIds = remember(plannerDays) {
        plannerDays.flatMap { day -> 
            listOfNotNull(day.topGarment?.id, day.bottomGarment?.id, day.shoesGarment?.id, day.hatGarment?.id) + day.accessories.map { it.id }
        }.toSet()
    }

    val lentItems by OutfixApplication.instance.lentRepository.lentItemsFlow.collectAsState(initial = emptyList())
    val activeLentGarmentIds = remember(lentItems) {
        lentItems.filter { !it.isReturned }.map { it.garmentId }.toSet()
    }

    val availableForLending = remember(allGarments, searchQuery, plannerGarmentIds, activeLentGarmentIds) {
        allGarments
            .filter { it.status == "AVAILABLE" || it.status == "clean" || it.status.isBlank() }
            .filter { it.id !in plannerGarmentIds }
            .filter { it.id !in activeLentGarmentIds }
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { focusManager.clearFocus() }
    ) {
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
                    .background(Color(0xFFBDBDBD)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.ChevronLeft,
                    contentDescription = "Regresar",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            Text(
                text = "Prestados",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                color = Color.Black,
                modifier = Modifier.align(Alignment.Center)
            )

            val canSave = selectedGarment != null && borrowerName.isNotBlank()
            if (canSave) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(40.dp)
                        .bouncyClickable {
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
                        .background(LimeGreen.copy(alpha = 0.5f)),
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
        }

        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.4f))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Seleccionar prenda", fontSize = 13.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar prenda...", fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LimeGreen.copy(alpha = 0.5f),
                    unfocusedBorderColor = Color.LightGray,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                )
            )

            if (selectedGarment != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(LimeGreen.copy(alpha = 0.1f))
                        .border(1.5.dp, LimeGreen.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
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
                        }
                        Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = LimeGreen.copy(alpha = 0.5f))
                    }
                }
            }

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
                                    if (isSelected) LimeGreen.copy(alpha = 0.5f) else Color.LightGray.copy(alpha = 0.3f),
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
                            }
                            if (isSelected) {
                                Icon(Icons.Rounded.Check, contentDescription = null, tint = LimeGreen.copy(alpha = 0.5f), modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.4f))

            OutlinedTextField(
                value = borrowerName,
                onValueChange = { borrowerName = it },
                label = { Text("Nombre del prestatario") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LimeGreen.copy(alpha = 0.5f),
                    unfocusedBorderColor = Color.LightGray,
                    focusedLabelColor = LimeGreen.copy(alpha = 0.5f),
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                )
            )

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
                        tint = LimeGreen.copy(alpha = 0.5f)
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
                    focusedBorderColor = LimeGreen.copy(alpha = 0.5f),
                    unfocusedBorderColor = Color.LightGray,
                    focusedLabelColor = LimeGreen.copy(alpha = 0.5f),
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                )
            )

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
                    unfocusedLabelColor = Color.Gray,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    disabledTextColor = Color.Black
                )
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}