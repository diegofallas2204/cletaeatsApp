package com.cletaeats.ui.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Login
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cletaeats.ui.theme.*
import com.cletaeats.network.CletaApi
import com.cletaeats.network.LoginRequest
import com.cletaeats.network.TokenManager
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit, onNavigateToRegister: () -> Unit = {}) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rol by remember { mutableStateOf("cliente") } // Nuevo selector de roles
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("Credenciales inválidas. Inténtalo de nuevo.") }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🍜 CletaEats",
            style = MaterialTheme.typography.displayLarge.copy(
                color = BrownDark,
                fontSize = 32.sp
            ),
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Selector de roles (Cliente vs Repartidor)
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RoleCard("Cliente", rol == "cliente", Modifier.weight(1f)) { rol = "cliente" }
            RoleCard("Repartidor", rol == "repartidor", Modifier.weight(1f)) { rol = "repartidor" }
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Usuario",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
            )
            OutlinedTextField(
                value = username,
                onValueChange = { input -> username = input.replace("\n", "").replace("\r", "") },
                placeholder = { Text("Tu usuario") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(androidx.compose.ui.focus.FocusDirection.Down) }),
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

        Spacer(modifier = Modifier.height(14.dp))

        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Contraseña",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
            )
            OutlinedTextField(
                value = password,
                onValueChange = { input -> password = input.replace("\n", "").replace("\r", "") },
                placeholder = { Text("••••••••") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                }),
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

        Spacer(modifier = Modifier.height(24.dp))

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
                                if (token != null) {
                                    TokenManager.token = token
                                    TokenManager.username = username
                                    // Sobrescribimos el rol del TokenManager con el seleccionado en pantalla
                                    TokenManager.rol = rol
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
            modifier = Modifier.fillMaxWidth().height(55.dp),
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
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ingresar", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        TextButton(onClick = onNavigateToRegister) {
            Text(
                text = "¿Sin cuenta? Regístrate acá",
                color = BrownDark,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        if (showError) {
            Text(
                text = errorMessage,
                color = RedAccent,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(top = 12.dp)
            )
        }
    }
}

@Composable
private fun RoleCard(label: String, isSelected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    OutlinedCard(
        onClick = onClick,
        modifier = modifier,
        colors = CardDefaults.outlinedCardColors(
            containerColor = if (isSelected) BrownDark else Color.Transparent
        ),
        border = BorderStroke(1.dp, BrownDark)
    ) {
        Box(Modifier.fillMaxWidth().padding(10.dp), Alignment.Center) {
            Text(label, color = if (isSelected) Cream else BrownDark, fontWeight = FontWeight.Bold)
        }
    }
}
