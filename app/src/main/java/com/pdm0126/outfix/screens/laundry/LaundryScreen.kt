package com.pdm0126.outfix.screens.laundry

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocalLaundryService
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.pdm0126.outfix.OutfixApplication
import com.pdm0126.outfix.data.api.dto.GarmentResponse
import kotlinx.coroutines.launch

@Composable
fun LaundryScreen() {
    val coroutineScope = rememberCoroutineScope()
    val repository = OutfixApplication.instance.garmentRepository
    val allGarments by repository.garmentsFlow.collectAsState(initial = emptyList())
    
    val laundryItems = remember(allGarments) {
        allGarments.filter { it.status == "IN_WASH" }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEBE0D3)) // Beige background matching mockup
            .padding(horizontal = 24.dp)
            .padding(top = 40.dp)
    ) {
        // App bar area (asumiendo que el TopAppBar se maneja fuera, pero podemos agregar el título aquí si es necesario)
        Text(
            text = "Cesto",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        
        Spacer(modifier = Modifier.height(24.dp))

        if (laundryItems.isNotEmpty()) {
            Button(
                onClick = {
                    coroutineScope.launch {
                        laundryItems.forEach { garment ->
                            repository.markAsWashed(garment.id)
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE37B75)),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Lavar y vaciar", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }

        if (laundryItems.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(bottom = 80.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Outlined.LocalLaundryService,
                        contentDescription = "Empty",
                        modifier = Modifier.size(80.dp),
                        tint = Color.Gray.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("¡Tu cesto de ropa está vacío!", color = Color.DarkGray, fontSize = 18.sp)
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 120.dp), // Padding para la barra de navegación inferior
                modifier = Modifier.fillMaxSize()
            ) {
                items(laundryItems) { garment ->
                    LaundryGarmentItem(
                        garment = garment,
                        onWashClick = {
                            coroutineScope.launch {
                                repository.markAsWashed(garment.id)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun LaundryGarmentItem(
    garment: GarmentResponse,
    onWashClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Caja de imagen con fondo gris
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFEFEFEF))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = garment.imageUrl,
                    contentDescription = garment.name,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            Spacer(modifier = Modifier.width(20.dp))
            
            // Textos
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Usada",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Recientemente",
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )
            }
        }
        
        // Icono de lavadora (arriba a la derecha)
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(1.5.dp, Color.Black, RoundedCornerShape(8.dp))
                .clickable { onWashClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.LocalLaundryService,
                contentDescription = "Lavar",
                tint = Color.Black,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
