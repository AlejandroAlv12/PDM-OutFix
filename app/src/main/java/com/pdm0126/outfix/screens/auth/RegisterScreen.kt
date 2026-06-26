package com.pdm0126.outfix.screens.auth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pdm0126.outfix.data.api.RetrofitClient
import com.pdm0126.outfix.data.api.dto.RegisterRequest
import com.pdm0126.outfix.ui.theme.LimeGreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA))
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Crear Cuenta",
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFF423D38),
            fontFamily = androidx.compose.ui.text.font.FontFamily.Serif
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Únete a Outfix para organizar tu armario",
            fontSize = 16.sp,
            color = Color.Gray
        )
        
        Spacer(modifier = Modifier.height(48.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Nombre de usuario", color = Color.Gray) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            textStyle = TextStyle(color = Color.Black),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedIndicatorColor = LimeGreen,
                unfocusedIndicatorColor = Color(0xFFE0E0E0),
                cursorColor = Color.Black
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo electrónico", color = Color.Gray) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            textStyle = TextStyle(color = Color.Black),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedIndicatorColor = LimeGreen,
                unfocusedIndicatorColor = Color(0xFFE0E0E0),
                cursorColor = Color.Black
            ),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña", color = Color.Gray) },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            textStyle = TextStyle(color = Color.Black),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedIndicatorColor = LimeGreen,
                unfocusedIndicatorColor = Color(0xFFE0E0E0),
                cursorColor = Color.Black
            ),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (username.isBlank() || email.isBlank() || password.isBlank()) {
                    Toast.makeText(context, "Llena todos los campos", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                
                coroutineScope.launch {
                    isLoading = true
                    try {
                        val response = RetrofitClient.authApi.register(
                            RegisterRequest(displayName = username.trim(), email = email.trim(), password = password)
                        )
                        if (response.success && response.data != null) {
                            RetrofitClient.sessionManager?.apply {
                                saveAuthToken(response.data.token)
                                saveUserId(response.data.user.id)
                                saveUserEmail(response.data.user.email)
                                saveUserDisplayName(response.data.user.displayName)
                            }
                            onRegisterSuccess()
                        } else {
                            Toast.makeText(context, response.message, Toast.LENGTH_LONG).show()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(context, "Error de red al registrar", Toast.LENGTH_SHORT).show()
                    } finally {
                        isLoading = false
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            } else {
                Text("Registrarse", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "¿Ya tienes cuenta? ", color = Color.Gray)
            Text(
                text = "Iniciar Sesión",
                color = LimeGreen,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable {
                    if (!isLoading) onNavigateToLogin()
                }
            )
        }
    }
}
