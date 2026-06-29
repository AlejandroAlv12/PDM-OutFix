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
fun MenuPanel(
    onPrestadosClick: () -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 32.dp, horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "OutFix",
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            color = Color.Black,
            fontFamily = com.pdm0126.outfix.ui.theme.IdiqlatFontFamily,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Box {
            MenuOptionRow(label = "Prestados", onClick = onPrestadosClick)
        }
        HorizontalDivider(
            color = Color.LightGray.copy(alpha = 0.4f)
        )

        Box {
            MenuOptionRow(label = "Administrar", onClick = { /* TODO */ })
        }
        HorizontalDivider(
            color = Color.LightGray.copy(alpha = 0.4f)
        )
        Box {
            MenuOptionRow(label = "About US", onClick = { /* TODO */ })
        }
    }
}

@Composable
fun MenuOptionRow(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .bouncyClickable(onClick = onClick)
            .padding(vertical = 18.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black
        )
    }
}