package com.cletaeats

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.cletaeats.network.TokenManager
import com.cletaeats.ui.auth.LoginScreen
import com.cletaeats.ui.auth.RegisterScreen
import com.cletaeats.ui.screens.ClienteHomeScreen
import com.cletaeats.ui.theme.CletaEatsTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            CletaEatsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var currentScreen by remember { 
                        mutableStateOf(if (TokenManager.token == null) "login" else "home") 
                    }

                    if (currentScreen == "login") {
                        LoginScreen(
                            onLoginSuccess = { currentScreen = "home" },
                            onNavigateToRegister = { currentScreen = "register" }
                        )
                    } else if (currentScreen == "register") {
                        RegisterScreen(
                            onRegisterSuccess = { currentScreen = "login" },
                            onBackToLogin = { currentScreen = "login" }
                        )
                    } else {
                        ClienteHomeScreen(onLogout = { 
                            TokenManager.logout()
                            currentScreen = "login" 
                        })
                    }
                }
            }
        }
    }
}