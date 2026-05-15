package com.cletaeats

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.cletaeats.network.TokenManager
import com.cletaeats.ui.auth.LoginScreen
import com.cletaeats.ui.auth.RegisterScreen
import com.cletaeats.ui.screens.AdminDashboardScreen
import com.cletaeats.ui.screens.ClienteHomeScreen
import com.cletaeats.ui.screens.RepartidorHomeScreen
import com.cletaeats.ui.theme.CletaEatsTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            CletaEatsTheme {
                // 1. Declarar el estado en el nivel superior
                var currentScreen by remember {
                    mutableStateOf(if (TokenManager.token == null) "login" else "home")
                }

                // Obtener el rol actual desde el TokenManager
                val currentRole = TokenManager.rol ?: "cliente"

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // 2. Lógica de Navegación centralizada
                    when (currentScreen) {
                        "login" -> {
                            LoginScreen(
                                onLoginSuccess = { currentScreen = "home" },
                                onNavigateToRegister = { currentScreen = "register" }
                            )
                        }
                        "register" -> {
                            RegisterScreen(
                                onRegisterSuccess = { currentScreen = "login" },
                                onBackToLogin = { currentScreen = "login" }
                            )
                        }
                        "home" -> {
                            // 3. Ruteo por Rol dentro de Home
                            when (currentRole.lowercase()) {
                                "admin" -> AdminDashboardScreen(onLogout = {
                                    TokenManager.logout()
                                    currentScreen = "login"
                                })
                                "repartidor" -> RepartidorHomeScreen(onLogout = {
                                    TokenManager.logout()
                                    currentScreen = "login"
                                })
                                else -> ClienteHomeScreen(onLogout = {
                                    TokenManager.logout()
                                    currentScreen = "login"
                                })
                            }
                        }
                    }
                }
            }
        }
    }
}