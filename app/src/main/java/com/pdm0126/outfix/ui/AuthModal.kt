package com.pdm0126.outfix.ui

import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Checkroom
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pdm0126.outfix.data.api.RetrofitClient
import com.pdm0126.outfix.data.api.dto.LoginRequest
import com.pdm0126.outfix.data.api.dto.RegisterRequest
import kotlinx.coroutines.launch
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthModal(
    isVisible: Boolean = true,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    var isLogin by remember { mutableStateOf(true) }
    
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    
    val density = androidx.compose.ui.platform.LocalDensity.current.density
    
    val flipRotation by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isLogin) 0f else 180f,
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 700, easing = androidx.compose.animation.core.FastOutSlowInEasing),
        label = "flip"
    )
    val isFlipped = flipRotation > 90f
    val displayIsLogin = !isFlipped
    
    val transitionState = remember { androidx.compose.animation.core.MutableTransitionState(false) }
    transitionState.targetState = isVisible
    val transition = androidx.compose.animation.core.updateTransition(transitionState, label = "AuthModalTransition")
    
    val containerOffsetX by transition.animateFloat(
        transitionSpec = { androidx.compose.animation.core.tween(600, easing = androidx.compose.animation.core.FastOutSlowInEasing) },
        label = "containerOffset"
    ) { if (it) 0f else 1000f }
    
    val logoOffsetY by transition.animateFloat(
        transitionSpec = { androidx.compose.animation.core.tween(600, delayMillis = if (targetState) 50 else 0, easing = androidx.compose.animation.core.FastOutSlowInEasing) },
        label = "logoOffset"
    ) { if (it) 0f else -1000f }
    
    val nameOffsetY by transition.animateFloat(
        transitionSpec = { androidx.compose.animation.core.tween(600, delayMillis = if (targetState) 25 else 25, easing = androidx.compose.animation.core.FastOutSlowInEasing) },
        label = "nameOffset"
    ) { if (it) 0f else -1000f }
    
    val sloganOffsetY by transition.animateFloat(
        transitionSpec = { androidx.compose.animation.core.tween(600, delayMillis = if (targetState) 0 else 50, easing = androidx.compose.animation.core.FastOutSlowInEasing) },
        label = "sloganOffset"
    ) { if (it) 0f else -1000f }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo
        Box(
            modifier = Modifier
                .graphicsLayer { translationY = logoOffsetY }
                .size(100.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White)
        ) {
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(id = com.pdm0126.outfix.R.drawable.logo),
                contentDescription = "Logo",
                contentScale = androidx.compose.ui.layout.ContentScale.FillWidth,
                alignment = Alignment.TopCenter,
                modifier = Modifier.fillMaxSize()
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "OutFix",
            fontSize = 40.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
            fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
            modifier = Modifier.graphicsLayer { translationY = nameOffsetY }
        )
        
        Text(
            text = "Tu outfit listo antes de salir.",
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.8f),
            fontWeight = FontWeight.Bold,
            fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
            modifier = Modifier.graphicsLayer { translationY = sloganOffsetY }
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Modal Card
        Box(
            modifier = Modifier
                .graphicsLayer { 
                    translationX = containerOffsetX
                    rotationY = flipRotation
                    cameraDistance = 12f * density
                }
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFFE0E0E0).copy(alpha = 0.95f))
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer { rotationY = if (isFlipped) 180f else 0f }
                    .animateContentSize(animationSpec = androidx.compose.animation.core.tween(500, easing = androidx.compose.animation.core.FastOutSlowInEasing))
            ) {
                
                if (!displayIsLogin) {
                    TextField(
                        value = username,
                        onValueChange = { username = it },
                        placeholder = { Text("Nombre de usuario", color = Color.Black.copy(alpha = 0.6f), fontWeight = FontWeight.Bold) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp)),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = Color.Black,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black
                        ),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                TextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = { Text("Correo electrónico", color = Color.Black.copy(alpha = 0.6f), fontWeight = FontWeight.Bold) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp)),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = Color.Black,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    ),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                TextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = { Text("Contraseña", color = Color.Black.copy(alpha = 0.6f), fontWeight = FontWeight.Bold) },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp)),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = Color.Black,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    ),
                    singleLine = true
                )
                
                if (!displayIsLogin) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    TextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        placeholder = { Text("Repetir contraseña", color = Color.Black.copy(alpha = 0.6f), fontWeight = FontWeight.Bold) },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp)),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = Color.Black,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black
                        ),
                        singleLine = true
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Botón Entrar / Registrarse
                Button(
                    onClick = {
                        if (email.isBlank() || password.isBlank() || (!isLogin && username.isBlank()) || (!isLogin && confirmPassword.isBlank())) {
                            Toast.makeText(context, "Llena todos los campos", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (!isLogin && password != confirmPassword) {
                            Toast.makeText(context, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (!isLogin && password.length < 8) {
                            Toast.makeText(context, "La contraseña debe tener al menos 8 caracteres", Toast.LENGTH_LONG).show()
                            return@Button
                        }
                        
                        coroutineScope.launch {
                            isLoading = true
                            try {
                                val response = if (isLogin) {
                                    RetrofitClient.authApi.login(LoginRequest(email.trim(), password))
                                } else {
                                    RetrofitClient.authApi.register(RegisterRequest(
                                        email = email.trim(), 
                                        password = password, 
                                        displayName = username.trim()
                                    ))
                                }
                                
                                if (response.success && response.data != null) {
                                    RetrofitClient.sessionManager?.apply {
                                        saveAuthToken(response.data.token)
                                        saveUserId(response.data.user.id)
                                        saveUserEmail(response.data.user.email)
                                        saveUserDisplayName(response.data.user.displayName)
                                    }
                                    onSuccess()
                                } else {
                                    Toast.makeText(context, response.message, Toast.LENGTH_LONG).show()
                                }
                            } catch (e: retrofit2.HttpException) {
                                try {
                                    val errorJson = e.response()?.errorBody()?.string()
                                    val msg = JSONObject(errorJson ?: "").optString("message", "Error (${e.code()})")
                                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                } catch (parseException: Exception) {
                                    Toast.makeText(context, "Error (${e.code()})", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                Toast.makeText(context, "Error de red", Toast.LENGTH_SHORT).show()
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C5CA8)),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                        Text(if (displayIsLogin) "Entrar" else "Registrarse", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Botón Google
                Button(
                    onClick = {
                        Toast.makeText(context, "Pronto podrás iniciar sesión con Google", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF2F2F2))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        androidx.compose.foundation.Image(
                            painter = androidx.compose.ui.res.painterResource(id = com.pdm0126.outfix.R.drawable.ic_google),
                            contentDescription = "Google Logo",
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .size(24.dp)
                        )
                        Text(
                            text = "Continuar con Google", 
                            color = Color.Black, 
                            fontSize = 16.sp, 
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = if (displayIsLogin) "¿Aun no tienes cuenta? " else "¿Ya tienes cuenta? ", color = Color.Black, fontWeight = FontWeight.Bold)
                    Text(
                        text = if (displayIsLogin) "Crear cuenta" else "Iniciar sesión",
                        color = Color(0xFF1E3A8A),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable {
                            if (!isLoading) {
                                isLogin = !isLogin
                                email = ""
                                password = ""
                                username = ""
                            }
                        }
                    )
                }
            }
        }
    }
}
