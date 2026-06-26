package com.pdm0126.outfix.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ExitToApp
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pdm0126.outfix.data.api.RetrofitClient

@Composable
fun ProfileScreen(onLogout: () -> Unit) {
    val sessionManager = RetrofitClient.sessionManager
    val displayName = sessionManager?.fetchUserDisplayName() ?: "Usuario"
    val email = sessionManager?.fetchUserEmail() ?: "correo@ejemplo.com"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(Color(0xFFDCB888)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Person,
                contentDescription = "Avatar",
                tint = Color.White,
                modifier = Modifier.size(80.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = displayName,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF423D38)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = email,
            fontSize = 16.sp,
            color = Color.Gray
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Button(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373))
        ) {
            Icon(
                imageVector = Icons.Rounded.ExitToApp,
                contentDescription = "Cerrar sesión",
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Cerrar sesión", 
                color = Color.White, 
                fontSize = 18.sp, 
                fontWeight = FontWeight.Bold
            )
        }
    }
}
