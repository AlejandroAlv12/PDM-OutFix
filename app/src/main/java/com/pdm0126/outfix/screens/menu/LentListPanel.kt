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
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun LentListPanel(
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

            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(40.dp)
                    .bouncyClickable(onClick = onAddClick)
                    .clip(CircleShape)
                    .background(LimeGreen.copy(alpha = 0.5f)),
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
            val sortedItems = lentItems.sortedBy { it.isReturned }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(sortedItems, key = { it.id }) { item ->
                    LentItemCard(item = item, onClick = { onItemClick(item) })
                }
            }
        }
    }
}

@Composable
fun LentItemCard(item: LentItem, onClick: () -> Unit) {
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
                val badgeColor = if (item.isReturned) LimeGreen.copy(alpha = 0.5f) else Color(0xFFFFF2A8)
                val badgeText = if (item.isReturned) "Reclamado" else "Reclamar: ${item.reclaimDate}"
                val contentColor = if (item.isReturned) Color.White else Color.Black
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(badgeColor)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = badgeText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = contentColor
                        )
                        if (item.isReturned) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = null,
                                tint = contentColor,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}