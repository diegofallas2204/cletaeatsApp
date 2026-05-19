package com.cletaeats.ui.components

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.cletaeats.network.ComboItem
import com.cletaeats.network.MetodoPago
import com.cletaeats.ui.theme.*

@Composable
fun CheckoutDialog(
    combo: ComboItem,
    notes: String,
    isAgrandado: Boolean,
    onNotesChange: (String) -> Unit,
    onAgrandadoChange: (Boolean) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Detalles del Pedido", fontWeight = FontWeight.Bold, color = BrownDark) },
        text = {
            Column {
                Text(text = combo.nombre, fontWeight = FontWeight.Bold)
                Text(
                    text = "₡${if (isAgrandado) combo.precio + 1500 else combo.precio}",
                    color = OrangeSoft, fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().clickable { onAgrandadoChange(!isAgrandado) }.padding(vertical = 8.dp)
                ) {
                    Checkbox(checked = isAgrandado, onCheckedChange = onAgrandadoChange, colors = CheckboxDefaults.colors(checkedColor = BrownDark))
                    Text("Agrandar combo (+₡1500)", fontWeight = FontWeight.Bold)
                }
                OutlinedTextField(
                    value = notes, onValueChange = onNotesChange,
                    label = { Text("Notas Extra") }, modifier = Modifier.fillMaxWidth(), minLines = 3
                )
            }
        },
        confirmButton = {
            Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = BrownDark, contentColor = Color.White)) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Continuar",
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = BrownMid) }
        },
        containerColor = Cream
    )
}

@Composable
fun PaymentDialog(
    isSubmitting: Boolean,
    tarjetas: List<MetodoPago>,
    onDismiss: () -> Unit,
    onSaveCard: (MetodoPago) -> Unit,
    onConfirm: (String) -> Unit
) {
    // ESTADO CRÍTICO: Forzamos la selección de la primera tarjeta si existe
    var selectedValue by remember(tarjetas) {
        mutableStateOf(tarjetas.firstOrNull()?.numeroTarjeta ?: "")
    }
    var showForm by remember { mutableStateOf(tarjetas.isEmpty()) }

    var num by remember { mutableStateOf("") }
    var exp by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }

    // VALIDATION RULES
    val isNumValid = num.length in 15..16
    val isCvvValid = cvv.length in 3..4

    // Expiration date validity (not expired)
    val calendar = java.util.Calendar.getInstance()
    val currentYear2Digit = calendar.get(java.util.Calendar.YEAR) % 100
    val currentMonth = calendar.get(java.util.Calendar.MONTH) + 1

    val expMonth = if (exp.length == 5 && exp.contains("/")) exp.substringBefore("/").toIntOrNull() ?: 0 else 0
    val expYear = if (exp.length == 5 && exp.contains("/")) exp.substringAfter("/").toIntOrNull() ?: 0 else 0

    val isExpFormatValid = exp.length == 5 && exp.contains("/") && expMonth in 1..12
    val isNotExpired = expYear > currentYear2Digit || (expYear == currentYear2Digit && expMonth >= currentMonth)
    val isExpValid = isExpFormatValid && isNotExpired

    val isFormValid = isNumValid && isCvvValid && isExpValid

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Método de Pago", fontWeight = FontWeight.Bold, color = BrownDark) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (!showForm) {
                    tarjetas.forEach { tarjeta ->
                        Row(
                            Modifier.fillMaxWidth()
                                .clickable { selectedValue = tarjeta.numeroTarjeta }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (selectedValue == tarjeta.numeroTarjeta),
                                onClick = { selectedValue = tarjeta.numeroTarjeta }
                            )
                            Text("💳 **** ${tarjeta.numeroTarjeta.takeLast(4)}", Modifier.padding(start = 8.dp), color = TextDark)
                        }
                    }
                    TextButton(onClick = { showForm = true }) { Text("+ Agregar Tarjeta", color = BrownDark, fontWeight = FontWeight.Bold) }
                } else {
                    OutlinedTextField(
                        value = num,
                        onValueChange = { input ->
                            val filtered = input.filter { it.isDigit() }
                            if (filtered.length <= 16) num = filtered
                        },
                        label = { Text("Número de Tarjeta") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = num.isNotEmpty() && !isNumValid,
                        supportingText = if (num.isNotEmpty() && !isNumValid) { { Text("15 o 16 dígitos", color = MaterialTheme.colorScheme.error) } } else null,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                    OutlinedTextField(
                        value = exp,
                        onValueChange = { input ->
                            val clean = input.filter { it.isDigit() }
                            if (clean.length <= 4) {
                                exp = if (clean.length > 2) {
                                    "${clean.substring(0, 2)}/${clean.substring(2)}"
                                } else {
                                    clean
                                }
                            }
                        },
                        label = { Text("Expiración (MM/AA)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = exp.isNotEmpty() && !isExpValid,
                        supportingText = if (exp.isNotEmpty()) {
                            if (!isExpFormatValid) {
                                { Text("Formato MM/AA inválido", color = MaterialTheme.colorScheme.error) }
                            } else if (!isNotExpired) {
                                { Text("La tarjeta está vencida", color = MaterialTheme.colorScheme.error) }
                            } else null
                        } else null,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                    OutlinedTextField(
                        value = cvv,
                        onValueChange = { input ->
                            val filtered = input.filter { it.isDigit() }
                            if (filtered.length <= 4) cvv = filtered
                        },
                        label = { Text("CVV") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = cvv.isNotEmpty() && !isCvvValid,
                        supportingText = if (cvv.isNotEmpty() && !isCvvValid) { { Text("3 o 4 dígitos", color = MaterialTheme.colorScheme.error) } } else null,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )

                    Button(
                        onClick = {
                            if (isFormValid) {
                                val nueva = MetodoPago(
                                    numeroTarjeta = num,
                                    fechaVencimiento = exp,
                                    cvv = cvv
                                )
                                onSaveCard(nueva)
                                selectedValue = num
                                showForm = false
                            }
                        },
                        enabled = isFormValid,
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrownDark,
                            contentColor = Color.White,
                            disabledContainerColor = Color.LightGray,
                            disabledContentColor = Color.DarkGray
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = "Guardar y Seleccionar",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            val isConfirmEnabled = selectedValue.isNotEmpty() && !isSubmitting
            Button(
                onClick = {
                    Log.d("CletaEats", "BOTON CLICK: Intentando confirmar con $selectedValue")
                    if (selectedValue.isNotEmpty()) onConfirm(selectedValue)
                },
                enabled = isConfirmEnabled,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrownDark,
                    contentColor = Color.White,
                    disabledContainerColor = Color.LightGray,
                    disabledContentColor = Color.DarkGray
                )
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                } else {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Pagar y Finalizar",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cerrar", color = BrownMid, fontWeight = FontWeight.Bold) }
        },
        containerColor = Cream
    )
}