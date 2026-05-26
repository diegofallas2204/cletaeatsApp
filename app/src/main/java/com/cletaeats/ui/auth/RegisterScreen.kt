package com.cletaeats.ui.auth

import android.util.Log
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cletaeats.network.CletaApi
import com.cletaeats.network.RegisterRequest
import com.cletaeats.ui.components.CletaInput
import com.cletaeats.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onBackToLogin: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rol by remember { mutableStateOf("cliente") }
    var nombre by remember { mutableStateOf("") }
    var cedula by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // VALIDATION RULES
    val isUsernameValid = username.matches(Regex("^[a-zA-Z0-9_]{3,20}$"))
    val isPasswordValid = password.length >= 6
    val isNombreValid = nombre.trim().length >= 3 && nombre.all { it.isLetter() || it.isWhitespace() }
    val isCedulaValid = cedula.length in 9..12
    val isEmailValid = email.isNotEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    val isTelefonoValid = telefono.length in 8..15
    val isDireccionValid = direccion.trim().length >= 10

    val isFormValid = isUsernameValid && isPasswordValid && isNombreValid && isCedulaValid && isEmailValid && isTelefonoValid && isDireccionValid

    Box(modifier = Modifier.fillMaxSize().background(Cream)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Crear Cuenta", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = BrownDark)
            Text("Únete a CletaEats hoy", color = BrownLight, modifier = Modifier.padding(bottom = 20.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RoleCard("Cliente", rol == "cliente", Modifier.weight(1f)) { rol = "cliente" }
                RoleCard("Repartidor", rol == "repartidor", Modifier.weight(1f)) { rol = "repartidor" }
            }

            Spacer(modifier = Modifier.height(16.dp))

            CletaInput(
                value = username,
                onValueChange = { input ->
                    val filtered = input.filter { it.isLetterOrDigit() || it == '_' }
                    if (filtered.length <= 20) username = filtered
                },
                label = "Usuario",
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                isError = username.isNotEmpty() && !isUsernameValid,
                supportingText = if (username.isNotEmpty() && !isUsernameValid) "Mínimo 3 caract. (letras, num, _)" else null
            )
            CletaInput(
                value = password,
                onValueChange = { input -> if (input.length <= 30) password = input },
                label = "Contraseña",
                isPassword = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                isError = password.isNotEmpty() && !isPasswordValid,
                supportingText = if (password.isNotEmpty() && !isPasswordValid) "Mínimo 6 caracteres" else null
            )
            CletaInput(
                value = nombre,
                onValueChange = { input ->
                    val filtered = input.filter { it.isLetter() || it.isWhitespace() }
                    if (filtered.length <= 50) nombre = filtered
                },
                label = "Nombre Completo",
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                isError = nombre.isNotEmpty() && !isNombreValid,
                supportingText = if (nombre.isNotEmpty() && !isNombreValid) "Mínimo 3 letras (solo letras/espacios)" else null
            )
            CletaInput(
                value = cedula,
                onValueChange = { input ->
                    val filtered = input.filter { it.isDigit() }
                    if (filtered.length <= 12) cedula = filtered
                },
                label = "Cédula",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                isError = cedula.isNotEmpty() && !isCedulaValid,
                supportingText = if (cedula.isNotEmpty() && !isCedulaValid) "Debe ser de 9 a 12 dígitos" else null
            )
            CletaInput(
                value = email,
                onValueChange = { input -> if (input.length <= 50) email = input.trim() },
                label = "Correo Electrónico",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                isError = email.isNotEmpty() && !isEmailValid,
                supportingText = if (email.isNotEmpty() && !isEmailValid) "Correo inválido" else null
            )
            CletaInput(
                value = telefono,
                onValueChange = { input ->
                    val filtered = input.filter { it.isDigit() }
                    if (filtered.length <= 15) telefono = filtered
                },
                label = "Teléfono",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                isError = telefono.isNotEmpty() && !isTelefonoValid,
                supportingText = if (telefono.isNotEmpty() && !isTelefonoValid) "Debe ser de 8 a 15 dígitos" else null
            )
            CletaInput(
                value = direccion,
                onValueChange = { input -> if (input.length <= 150) direccion = input },
                label = "Dirección Exacta",
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                isError = direccion.isNotEmpty() && !isDireccionValid,
                supportingText = if (direccion.isNotEmpty() && !isDireccionValid) "Mínimo 10 caracteres" else null
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (isFormValid) {
                        scope.launch {
                            isLoading = true
                            try {
                                val request = RegisterRequest(
                                    username, password, rol, nombre, cedula, direccion, telefono, email
                                )
                                val resp = CletaApi.retrofitService.register(request)
                                if (resp.success) {
                                    onRegisterSuccess()
                                } else {
                                    Log.e("CletaEats", "Error: ${resp.error ?: "Error desconocido"}")
                                }
                            } catch (e: Exception) {
                                Log.e("CletaEats", "Fallo: ${e.message}")
                            } finally {
                                isLoading = false
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(55.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrownDark,
                    contentColor = Color.White,
                    disabledContainerColor = Color.LightGray,
                    disabledContentColor = Color.DarkGray
                ),
                shape = RoundedCornerShape(12.dp),
                enabled = isFormValid && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Icon(
                        imageVector = Icons.Default.PersonAdd,
                        contentDescription = "Registrarse",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            TextButton(onClick = onBackToLogin) {
                Text("¿Ya tienes cuenta? Inicia sesión", color = BrownDark)
            }
        }
    }
}

@Composable
private fun RoleCard(label: String, isSelected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    OutlinedCard(
        onClick = onClick,
        modifier = modifier,
        colors = CardDefaults.outlinedCardColors(containerColor = if (isSelected) BrownDark else Color.Transparent),
        border = BorderStroke(1.dp, BrownDark)
    ) {
        Box(Modifier.fillMaxWidth().padding(12.dp), Alignment.Center) {
            Text(label, color = if (isSelected) Cream else BrownDark, fontWeight = FontWeight.Bold)
        }
    }
}
