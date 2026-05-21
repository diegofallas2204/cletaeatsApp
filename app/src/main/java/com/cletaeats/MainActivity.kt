package com.cletaeats

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.cletaeats.network.TokenManager
import com.cletaeats.ui.auth.LoginScreen
import com.cletaeats.ui.auth.RegisterScreen
import com.cletaeats.ui.screens.AdminDashboardScreen
import com.cletaeats.ui.screens.ClienteHomeScreen
import com.cletaeats.ui.screens.RepartidorHomeScreen
import com.cletaeats.ui.theme.CletaEatsTheme
import com.cletaeats.utils.connectivityState
import com.cletaeats.utils.ConnectionState
import com.cletaeats.ui.components.NoInternetScreen

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            CletaEatsTheme {
                val connectionState by connectivityState()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (connectionState is ConnectionState.Unavailable) {
                        NoInternetScreen()
                    } else {
                        // Navegación centralizada de la aplicación
                        var currentScreen by remember {
                            mutableStateOf(if (TokenManager.token == null) "login" else "home")
                        }

                        val currentRole = TokenManager.rol ?: "cliente"

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
}