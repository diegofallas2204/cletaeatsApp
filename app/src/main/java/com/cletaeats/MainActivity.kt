package com.cletaeats

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.cletaeats.network.SessionManager
import com.cletaeats.network.TokenManager
import com.cletaeats.ui.auth.LoginScreen
import com.cletaeats.ui.auth.RegisterScreen
import com.cletaeats.ui.screens.AdminDashboardScreen
import com.cletaeats.ui.screens.ClienteHomeScreen
import com.cletaeats.ui.screens.RepartidorHomeScreen
import com.cletaeats.ui.theme.CletaEatsTheme
import com.cletaeats.utils.LocalCacheManager
import com.cletaeats.utils.connectivityState
import com.cletaeats.utils.ConnectionState
import com.cletaeats.ui.components.NoInternetScreen

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Inicializar managers persistentes (necesitan Context)
        SessionManager.init(this)
        LocalCacheManager.init(this)
        com.cletaeats.database.SyncManager.init(this)

        setContent {
            CletaEatsTheme {
                val connectionState by connectivityState()

                LaunchedEffect(connectionState) {
                    if (connectionState is ConnectionState.Available) {
                        com.cletaeats.database.SyncManager.sincronizar()
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Restaurar sesión: si hay token guardado, ir directo a home
                    var currentScreen by remember {
                        mutableStateOf(if (SessionManager.isLoggedIn) "home" else "login")
                    }

                    if (connectionState is ConnectionState.Unavailable && !SessionManager.isLoggedIn) {
                        NoInternetScreen()
                    } else {

                        val currentRole = SessionManager.rol ?: "cliente"

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