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
fun LentDetailPanel(
    lentItem: LentItem?,
    onBack: () -> Unit,
    onReturned: (LentItem) -> Unit
) {
    val item = lentItem ?: return

    Column(modifier = Modifier.fillMaxSize()) {
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
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Devuelto", color = com.pdm0126.outfix.ui.theme.LimeGreen.copy(alpha = 0.5f), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = null,
                                tint = com.pdm0126.outfix.ui.theme.LimeGreen.copy(alpha = 0.5f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            if (!item.isReturned) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .bouncyClickable { onReturned(item) }
                        .clip(RoundedCornerShape(14.dp))
                        .background(com.pdm0126.outfix.ui.theme.LimeGreen.copy(alpha = 0.5f)),
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
fun LentDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 13.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.Black)
    }
}