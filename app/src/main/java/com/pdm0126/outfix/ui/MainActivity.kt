package com.pdm0126.outfix.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import android.graphics.Color
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.pdm0126.outfix.data.api.RetrofitClient
import com.pdm0126.outfix.data.prefs.SessionManager
import com.pdm0126.outfix.ui.theme.OutFixTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                Color.TRANSPARENT, Color.TRANSPARENT
            )
        )

        val sessionManager = SessionManager(this)
        RetrofitClient.sessionManager = sessionManager

        setContent {
            OutFixTheme(darkTheme = true) {
                val appViewModel: AppViewModel = hiltViewModel()
                val uiState by appViewModel.uiState.collectAsState()
                
                var showSplash by remember { mutableStateOf(!uiState.isSplashFinished) }

                androidx.compose.foundation.layout.Box(modifier = androidx.compose.ui.Modifier.fillMaxSize()) {
                    MainScreen(
                        onLogout = {
                            sessionManager.clearSession()
                        }
                    )

                    if (showSplash) {
                        com.pdm0126.outfix.screens.splash.ClosetDoorsOverlay(
                            onFinished = {
                                appViewModel.setSplashFinished()
                                showSplash = false
                            }
                        )
                    }
                }
            }
        }
    }
}