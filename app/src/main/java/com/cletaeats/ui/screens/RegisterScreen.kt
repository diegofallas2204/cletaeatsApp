package com.cletaeats.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PersonAdd
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
import com.cletaeats.network.RegisterRequest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(onRegisterSuccess: () -> Unit, onBackToLogin: () -> Unit) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("cliente") }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isSuccess by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val roles = listOf("cliente", "repartidor")
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBackToLogin) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = BrownDark)
            }
            Spacer(modifier = Modifier.weight(1f))
        }
        
        Text(
            text = "Crear Cuenta",
            style = MaterialTheme.typography.displayMedium.copy(
                color = BrownDark,
                fontSize = 28.sp
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
                placeholder = { Text("Tu nombre de usuario") },
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

        Spacer(modifier = Modifier.height(20.dp))

        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Rol",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
            )
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = selectedRole.replaceFirstChar { it.uppercase() },
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrownLight,
                        unfocusedBorderColor = CreamDark,
                        focusedContainerColor = Cream,
                        unfocusedContainerColor = Cream
                    )
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    roles.forEach { selectionOption ->
                        DropdownMenuItem(
                            text = { Text(selectionOption.replaceFirstChar { it.uppercase() }) },
                            onClick = {
                                selectedRole = selectionOption
                                expanded = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        if (isSuccess) {
            Text(
                text = "¡Cuenta creada exitosamente! Redirigiendo...",
                color = BrownLight,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        Button(
            onClick = {
                if (username.isNotEmpty() && password.isNotEmpty()) {
                    isLoading = true
                    showError = false
                    scope.launch {
                        try {
                            val response = CletaApi.retrofitService.register(
                                RegisterRequest(username, password, selectedRole)
                            )
                            if (response.success) {
                                isSuccess = true
                                kotlinx.coroutines.delay(1500)
                                onRegisterSuccess()
                            } else {
                                errorMessage = response.error ?: "No se pudo registrar la cuenta."
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
            enabled = !isLoading && !isSuccess,
            colors = ButtonDefaults.buttonColors(containerColor = BrownDark)
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PersonAdd,
                        contentDescription = "Registrarse",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp).padding(end = 8.dp)
                    )
                    Text("Registrarse", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
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
