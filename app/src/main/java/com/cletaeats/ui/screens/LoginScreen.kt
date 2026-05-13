package com.cletaeats.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Login
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cletaeats.ui.theme.*
import com.cletaeats.network.CletaApi
import com.cletaeats.network.LoginRequest
import com.cletaeats.network.TokenManager
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("Credenciales inválidas. Inténtalo de nuevo.") }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Usamos una estructura pura de Compose
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🍜 CletaEats",
            style = MaterialTheme.typography.displayLarge.copy(
                color = BrownDark,
                fontSize = 32.sp
            ),
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Usuario",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
            )
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                placeholder = { Text("Tu usuario admin") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                enabled = !isLoading,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrownLight,
                    unfocusedBorderColor = CreamDark,
                    focusedContainerColor = Cream,
                    unfocusedContainerColor = Cream
                )
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Contraseña",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = { Text("••••••••") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                enabled = !isLoading,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrownLight,
                    unfocusedBorderColor = CreamDark,
                    focusedContainerColor = Cream,
                    unfocusedContainerColor = Cream
                )
            )
        }

        Spacer(modifier = Modifier.height(30.dp))

        Button(
            onClick = {
                if (username.isNotEmpty() && password.isNotEmpty()) {
                    isLoading = true
                    showError = false
                    scope.launch {
                        try {
                            val response = CletaApi.retrofitService.login(LoginRequest(username, password))
                            if (response.success) {
                                val token = response.token ?: response.data?.token
                                val rol = response.rol ?: response.data?.rol
                                if (token != null) {
                                    TokenManager.token = "Bearer $token"
                                    TokenManager.username = username
                                    TokenManager.rol = rol ?: "cliente" // Default to cliente if null
                                    onLoginSuccess()
                                } else {
                                    errorMessage = "Error: No se recibió token."
                                    showError = true
                                }
                            } else {
                                errorMessage = response.error ?: "Credenciales inválidas."
                                showError = true
                            }
                        } catch (e: Exception) {
                            errorMessage = "Error de conexión: ${e.localizedMessage}"
                            showError = true
                        } finally {
                            isLoading = false
                        }
                    }
                } else {
                    errorMessage = "Por favor completa todos los campos."
                    showError = true
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            shape = RoundedCornerShape(16.dp),
            enabled = !isLoading,
            colors = ButtonDefaults.buttonColors(containerColor = BrownDark)
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Login,
                        contentDescription = "Login",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        if (showError) {
            Text(
                text = errorMessage,
                color = RedAccent,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}
