package com.pdm0126.outfix.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import android.graphics.Color
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.pdm0126.outfix.ui.theme.OutFixTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.pdm0126.outfix.data.api.RetrofitClient
import com.pdm0126.outfix.data.prefs.SessionManager
import com.pdm0126.outfix.screens.auth.LoginScreen
import com.pdm0126.outfix.screens.auth.RegisterScreen

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
            var currentScreen by remember { mutableStateOf(if (sessionManager.fetchAuthToken() != null) "main" else "login") }

            OutFixTheme(darkTheme = true) {
                when (currentScreen) {
                    "main" -> MainScreen(
                        onLogout = {
                            sessionManager.clearSession()
                            currentScreen = "login"
                        }
                    )
                    "login" -> LoginScreen(
                        onLoginSuccess = { currentScreen = "main" },
                        onNavigateToRegister = { currentScreen = "register" }
                    )
                    "register" -> RegisterScreen(
                        onRegisterSuccess = { currentScreen = "main" },
                        onNavigateToLogin = { currentScreen = "login" }
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    OutFixTheme {
        Greeting("Android")
    }
}