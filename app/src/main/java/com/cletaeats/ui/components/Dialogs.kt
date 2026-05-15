package com.cletaeats.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
            Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = BrownDark)) {
                Text("Continuar", color = Color.White)
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
    var selectedPayment by remember { mutableStateOf("Efectivo") }
    var showNewCardForm by remember { mutableStateOf(false) }
    var numeroTarjeta by remember { mutableStateOf("") }
    var fechaVencimiento by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Método de Pago", fontWeight = FontWeight.Bold, color = BrownDark) },
        text = {
            Column {
                if (!showNewCardForm) {
                    // Opciones de pago
                    Row(Modifier.fillMaxWidth().clickable { selectedPayment = "Efectivo" }.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = selectedPayment == "Efectivo", onClick = { selectedPayment = "Efectivo" }, colors = RadioButtonDefaults.colors(selectedColor = BrownDark))
                        Text("💵 Efectivo")
                    }
                    tarjetas.forEach { tarjeta ->
                        val last4 = if (tarjeta.numeroTarjeta.length >= 4) tarjeta.numeroTarjeta.takeLast(4) else "****"
                        val cardLabel = "💳 Tarjeta *$last4"
                        Row(Modifier.fillMaxWidth().clickable { selectedPayment = cardLabel }.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = selectedPayment == cardLabel, onClick = { selectedPayment = cardLabel }, colors = RadioButtonDefaults.colors(selectedColor = BrownDark))
                            Text(cardLabel)
                        }
                    }
                    TextButton(onClick = { showNewCardForm = true }) { Text("+ Agregar nueva tarjeta", color = BrownMid) }
                } else {
                    // Formulario Nueva Tarjeta
                    OutlinedTextField(value = numeroTarjeta, onValueChange = { numeroTarjeta = it }, label = { Text("Número de Tarjeta") })
                    Row {
                        OutlinedTextField(value = fechaVencimiento, onValueChange = { fechaVencimiento = it }, label = { Text("MM/AA") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = cvv, onValueChange = { cvv = it }, label = { Text("CVV") }, modifier = Modifier.weight(1f))
                    }
                    Button(
                        onClick = {
                            // Convertimos a Int y aseguramos que no sean nulos para que coincida con tu modelo
                            val newCard = MetodoPago(
                                id = 0, // O el valor por defecto que use tu API para nuevos registros
                                numeroTarjeta = numeroTarjeta,
                                fechaVencimiento = fechaVencimiento,
                                cvv = cvv
                            )
                            onSaveCard(newCard)
                            showNewCardForm = false
                        },
                        modifier = Modifier.padding(top = 16.dp).fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = BrownDark)
                    ) {
                        Text("Guardar Tarjeta")
                    }
                }
                if (isSubmitting) CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally), color = BrownDark)
            }
        },
        confirmButton = {
            if (!showNewCardForm) {
                Button(onClick = { onConfirm(selectedPayment) }, enabled = !isSubmitting, colors = ButtonDefaults.buttonColors(containerColor = BrownDark)) {
                    Text("Confirmar Pedido")
                }
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}