package com.cletaeats

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.cletaeats.network.TokenManager
import com.cletaeats.ui.screens.LoginScreen
import com.cletaeats.ui.screens.ClienteHomeScreen
import com.cletaeats.ui.theme.CletaEatsTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            CletaEatsTheme {
                var currentScreen by remember { 
                    mutableStateOf(if (TokenManager.token == null) "login" else "home") 
                }

                if (currentScreen == "login") {
                    LoginScreen(onLoginSuccess = { currentScreen = "home" })
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