package com.pdm0126.outfix.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Person
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
import androidx.compose.ui.graphics.drawscope.scale
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(if (hasSession) Color(0xFFDCB888) else Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Person,
                    contentDescription = "Avatar",
                    tint = Color.White,
                    modifier = Modifier.size(72.dp)
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

            Spacer(modifier = Modifier.height(48.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
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
    val transition = androidx.compose.animation.core.updateTransition(
        targetState = showLogoutDialog,
        label = "LogoutDialogTransition"
    )
    val alpha by transition.animateFloat(
        transitionSpec = { androidx.compose.animation.core.tween(300) },
        label = "Alpha"
    ) { if (it) 1f else 0f }
    val scale by transition.animateFloat(
        transitionSpec = {
            if (targetState) {
                androidx.compose.animation.core.spring(dampingRatio = 0.6f, stiffness = 400f)
            } else {
                androidx.compose.animation.core.tween(200)
            }
        },
        label = "Scale"
    ) { if (it) 1f else 0.85f }

    if (alpha > 0f) {
        var dialogCoords: LayoutCoordinates? by remember { mutableStateOf(null) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f * alpha))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        this.alpha = alpha
                    }
                    .onGloballyPositioned { dialogCoords = it }
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
                                refraction = 0.5f,
                                edge = 1.5f,
                                normalizedRadius = 0.15f
                            )
                    ) {
                        if (appBackgroundLayer != null && pagerCoords?.isAttached == true && dialogCoords?.isAttached == true) {
                            scale(scaleX = 1f / scale, scaleY = 1f / scale, pivot = androidx.compose.ui.geometry.Offset.Zero) {
                                val sPos = pagerCoords.positionInRoot()
                                val dPos = dialogCoords!!.positionInRoot()
                                translate(left = -(dPos.x - sPos.x), top = -(dPos.y - sPos.y)) {
                                    drawLayer(appBackgroundLayer)
                                }
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
                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier.bouncyClickable { onDismiss() }
                        ) {
                            Text(
                                "Cancelar",
                                color = Color.Gray,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(
                            onClick = onConfirm,
                            modifier = Modifier.bouncyClickable { onConfirm() }
                        ) {
                            Text(
                                "Sí, salir",
                                color = Color(0xFFFF0000),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
