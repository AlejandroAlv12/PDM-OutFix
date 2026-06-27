package com.pdm0126.outfix.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pdm0126.outfix.data.api.RetrofitClient
import com.pdm0126.outfix.ui.bouncyClickable
import com.pdm0126.outfix.ui.liquidGlass
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.drawscope.translate
import android.os.Build
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.clickable
import androidx.compose.animation.core.animateFloat
@Composable
fun ProfileScreen(
    onLogoutClick: () -> Unit, 
    onShowAuth: () -> Unit = {}
) {
    val sessionManager = RetrofitClient.sessionManager
    val hasSession by sessionManager?.sessionState?.collectAsState(initial = sessionManager.fetchAuthToken() != null)
        ?: remember { mutableStateOf(false) }

    val displayName = if (hasSession) sessionManager?.fetchUserDisplayName() ?: "Usuario" else "Bienvenido a OutFix"
    val email = if (hasSession) sessionManager?.fetchUserEmail() ?: "correo@ejemplo.com" else "Inicia sesión para guardar y sincronizar tu ropa en todos tus dispositivos."

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.TopCenter
            ) {
                Box(
                    modifier = Modifier
                        .padding(top = 20.dp)
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFD1D1D1))
                )
                Box(
                    modifier = Modifier
                        .padding(top = 125.dp)
                        .requiredSize(100.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFD1D1D1))
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
                color = Color.Gray,
                textAlign = if (hasSession) androidx.compose.ui.text.style.TextAlign.Start else androidx.compose.ui.text.style.TextAlign.Center,
                modifier = if (hasSession) Modifier else Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            Box(
                modifier = Modifier
                    .fillMaxWidth(if (hasSession) 0.5f else 1f)
                    .height(56.dp)
                    .bouncyClickable { if (hasSession) onLogoutClick() else onShowAuth() }
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (hasSession) Color(0xFFFF0000).copy(alpha = 0.5f) else Color(0xFF2B5EA0).copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (hasSession) "Cerrar Sesión" else "Iniciar Sesión",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(96.dp))
        }
    }
}

@Composable
fun LogoutDialogOverlay(
    showLogoutDialog: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    appBackgroundLayer: androidx.compose.ui.graphics.layer.GraphicsLayer?,
    pagerCoords: LayoutCoordinates?
) {
    var exitDirection by remember { mutableStateOf(1f) }

    LaunchedEffect(showLogoutDialog) {
        if (showLogoutDialog) {
            exitDirection = 1f
        }
    }

    val transitionState = remember { androidx.compose.animation.core.MutableTransitionState(false) }
    transitionState.targetState = showLogoutDialog
    val transition = androidx.compose.animation.core.updateTransition(transitionState, label = "LogoutDialogTransition")

    val bgAlpha by transition.animateFloat(
        transitionSpec = { androidx.compose.animation.core.tween(300) },
        label = "BgAlpha"
    ) { if (it) 1f else 0f }

    val containerOffsetY by transition.animateFloat(
        transitionSpec = { androidx.compose.animation.core.tween(600, easing = androidx.compose.animation.core.FastOutSlowInEasing) },
        label = "containerOffset"
    ) { if (it) 0f else 1500f * exitDirection }

    if (transition.currentState || transition.targetState) {
        var dialogCoords: LayoutCoordinates? by remember { mutableStateOf(null) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f * bgAlpha))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {
                        exitDirection = 1f
                        onDismiss()
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .onGloballyPositioned { dialogCoords = it }
                    .graphicsLayer {
                        translationY = containerOffsetY
                    }
                    .clip(RoundedCornerShape(28.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    )
            ) {
                if (Build.VERSION.SDK_INT >= 31) {
                    Canvas(
                        modifier = Modifier
                            .matchParentSize()
                            .liquidGlass(
                                blur = 30f,
                                refraction = 0.9f,
                                saturation = 0.5f,
                                edge = 1.5f,
                                normalizedRadius = 0.15f
                            )
                    ) {
                        if (appBackgroundLayer != null && pagerCoords?.isAttached == true && dialogCoords?.isAttached == true) {
                            val sPos = pagerCoords.positionInRoot()
                            val dPos = dialogCoords!!.positionInRoot()
                            translate(left = -(dPos.x - sPos.x), top = -(dPos.y - sPos.y + containerOffsetY)) {
                                drawLayer(appBackgroundLayer)
                            }
                        }
                    }
                    Box(
                        modifier = Modifier.matchParentSize()
                            .background(Color(0xFFE0E0E0).copy(alpha = 0.6f))
                    )
                } else {
                    Box(
                        modifier = Modifier.matchParentSize()
                            .background(Color(0xFFE0E0E0).copy(alpha = 0.95f))
                    )
                }

                Box(
                    modifier = Modifier.matchParentSize()
                        .border(
                            1.dp,
                            Color.White.copy(alpha = 0.5f),
                            RoundedCornerShape(28.dp)
                        )
                )

                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = "Cerrar sesión",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "¿Estás seguro de que deseas cerrar sesión?",
                        fontSize = 16.sp,
                        color = Color.DarkGray
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Box(
                            modifier = Modifier
                                .bouncyClickable { 
                                    exitDirection = 1f
                                    onDismiss() 
                                }
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Cancelar",
                                color = Color(0xFFFF0000),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .bouncyClickable { 
                                    exitDirection = -1f
                                    onConfirm() 
                                }
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Salir",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
