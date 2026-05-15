package com.cletaeats.ui.auth

import android.util.Log
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cletaeats.network.CletaApi
import com.cletaeats.network.RegisterRequest
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

    Box(modifier = Modifier.fillMaxSize().background(Cream)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Crear Cuenta", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = BrownDark)
            Text("Únete a CletaEats hoy", color = BrownLight, modifier = Modifier.padding(bottom = 24.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RoleCard("Cliente", rol == "cliente", Modifier.weight(1f)) { rol = "cliente" }
                RoleCard("Repartidor", rol == "repartidor", Modifier.weight(1f)) { rol = "repartidor" }
            }

            Spacer(modifier = Modifier.height(16.dp))

            CletaInput(username, { username = it }, "Usuario")
            CletaInput(password, { password = it }, "Contraseña", isPassword = true)
            CletaInput(nombre, { nombre = it }, "Nombre Completo")
            CletaInput(cedula, { cedula = it }, "Cédula")
            CletaInput(email, { email = it }, "Correo Electrónico")
            CletaInput(telefono, { telefono = it }, "Teléfono")
            CletaInput(direccion, { direccion = it }, "Dirección Exacta", singleLine = false)

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
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
                                // Nota: Usamos 'error' en lugar de 'mensaje' para coincidir con ApiResponse
                                Log.e("CletaEats", "Error: ${resp.error ?: "Error desconocido"}")
                            }
                        } catch (e: Exception) {
                            Log.e("CletaEats", "Fallo: ${e.message}")
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(55.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrownDark),
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading
            ) {
                if (isLoading) CircularProgressIndicator(color = Cream, modifier = Modifier.size(24.dp))
                else Text("Registrarse", color = Cream, fontWeight = FontWeight.Bold)
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

@Composable
private fun CletaInput(value: String, onValueChange: (String) -> Unit, label: String, isPassword: Boolean = false, singleLine: Boolean = true) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        singleLine = singleLine,
        visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = OrangeSoft,
            unfocusedBorderColor = BrownLight,
            focusedLabelColor = BrownDark
        )
    )
}