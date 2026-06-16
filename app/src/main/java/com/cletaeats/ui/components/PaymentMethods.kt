package com.cletaeats.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cletaeats.network.MetodoPago
import com.cletaeats.ui.theme.*

/**
 * Sección reutilizable "Métodos de Pago": lista las tarjetas, permite agregar
 * (con [AddCardDialog]) y eliminar (con confirmación). El estado de los diálogos
 * se maneja internamente para mantener simples a las pantallas que la usan.
 */
@Composable
fun PaymentMethodsCard(
    tarjetas: List<MetodoPago>,
    onSaveCard: (MetodoPago) -> Unit,
    onDeleteCard: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var deleteTarget by remember { mutableStateOf<MetodoPago?>(null) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = WhiteCard),
        border = BorderStroke(1.dp, CreamDark)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Métodos de Pago", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = BrownDark)
                TextButton(onClick = { showAddDialog = true }) {
                    Text("+ Agregar", color = OrangeSoft, fontWeight = FontWeight.Bold)
                }
            }

            if (tarjetas.isEmpty()) {
                Box(
                    Modifier.fillMaxWidth().padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No hay tarjetas de pago guardadas.", color = TextMid, fontSize = 14.sp)
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    tarjetas.forEach { tarjeta ->
                        TarjetaRow(tarjeta = tarjeta, onDelete = { deleteTarget = tarjeta })
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddCardDialog(
            onDismiss = { showAddDialog = false },
            onSave = { onSaveCard(it); showAddDialog = false }
        )
    }

    deleteTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Eliminar Tarjeta") },
            text = { Text("¿Deseas eliminar la tarjeta terminada en ${target.numeroTarjeta.takeLast(4)}?") },
            confirmButton = {
                TextButton(onClick = { target.id?.let { onDeleteCard(it) }; deleteTarget = null }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun TarjetaRow(tarjeta: MetodoPago, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Cream.copy(alpha = 0.3f)),
        border = BorderStroke(1.dp, CreamDark)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CreditCard, contentDescription = null, tint = BrownMid)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                val brand = when {
                    tarjeta.numeroTarjeta.startsWith("4") -> "VISA"
                    tarjeta.numeroTarjeta.startsWith("5") -> "Mastercard"
                    else -> "Tarjeta"
                }
                Text(
                    "$brand **** ${tarjeta.numeroTarjeta.takeLast(4)}",
                    fontWeight = FontWeight.Bold, color = TextDark, maxLines = 1
                )
                Text("Vence: ${tarjeta.fechaVencimiento}", fontSize = 11.sp, color = TextMid)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

/** Formulario de alta de tarjeta con la misma validación que el checkout del cliente. */
@Composable
fun AddCardDialog(
    onDismiss: () -> Unit,
    onSave: (MetodoPago) -> Unit
) {
    var num by remember { mutableStateOf("") }
    var exp by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }

    val calendar = java.util.Calendar.getInstance()
    val currentYear2Digit = calendar.get(java.util.Calendar.YEAR) % 100
    val currentMonth = calendar.get(java.util.Calendar.MONTH) + 1
    val expMonth = if (exp.length == 5 && exp.contains("/")) exp.substringBefore("/").toIntOrNull() ?: 0 else 0
    val expYear = if (exp.length == 5 && exp.contains("/")) exp.substringAfter("/").toIntOrNull() ?: 0 else 0
    val isNumValid = num.length in 15..16
    val isCvvValid = cvv.length in 3..4
    val isExpFormatValid = exp.length == 5 && exp.contains("/") && expMonth in 1..12
    val isNotExpired = expYear > currentYear2Digit || (expYear == currentYear2Digit && expMonth >= currentMonth)
    val isFormValid = isNumValid && isCvvValid && isExpFormatValid && isNotExpired

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Cream,
        title = { Text("Agregar Tarjeta", fontWeight = FontWeight.Bold, color = BrownDark) },
        text = {
            Column(Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = num,
                    onValueChange = { input -> val f = input.filter { it.isDigit() }; if (f.length <= 16) num = f },
                    label = { Text("Número de Tarjeta") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                    isError = num.isNotEmpty() && !isNumValid,
                    supportingText = if (num.isNotEmpty() && !isNumValid) { { Text("15 o 16 dígitos", color = MaterialTheme.colorScheme.error) } } else null,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                )
                OutlinedTextField(
                    value = exp,
                    onValueChange = { input ->
                        val clean = input.filter { it.isDigit() }
                        if (clean.length <= 4) exp = if (clean.length > 2) "${clean.substring(0, 2)}/${clean.substring(2)}" else clean
                    },
                    label = { Text("Expiración (MM/AA)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                    isError = exp.isNotEmpty() && !(isExpFormatValid && isNotExpired),
                    supportingText = if (exp.isNotEmpty() && !isExpFormatValid) { { Text("Formato MM/AA inválido", color = MaterialTheme.colorScheme.error) } }
                        else if (exp.isNotEmpty() && !isNotExpired) { { Text("La tarjeta está vencida", color = MaterialTheme.colorScheme.error) } }
                        else null,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                )
                OutlinedTextField(
                    value = cvv,
                    onValueChange = { input -> val f = input.filter { it.isDigit() }; if (f.length <= 4) cvv = f },
                    label = { Text("CVV") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(),
                    isError = cvv.isNotEmpty() && !isCvvValid,
                    supportingText = if (cvv.isNotEmpty() && !isCvvValid) { { Text("3 o 4 dígitos", color = MaterialTheme.colorScheme.error) } } else null,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(MetodoPago(numeroTarjeta = num, fechaVencimiento = exp, cvv = cvv)) },
                enabled = isFormValid
            ) { Text("Guardar", color = if (isFormValid) BrownDark else TextMid, fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = BrownMid) }
        }
    )
}
