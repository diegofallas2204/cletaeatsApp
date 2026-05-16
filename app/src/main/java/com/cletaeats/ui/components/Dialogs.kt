package com.cletaeats.ui.components

import android.util.Log
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
import androidx.compose.foundation.layout.size // <--- Asegúrate de que esta esté presente

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
    // ESTADO CRÍTICO: Forzamos la selección de la primera tarjeta si existe
    var selectedValue by remember(tarjetas) {
        mutableStateOf(tarjetas.firstOrNull()?.numeroTarjeta ?: "")
    }
    var showForm by remember { mutableStateOf(tarjetas.isEmpty()) }

    var num by remember { mutableStateOf("") }
    var exp by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Método de Pago", fontWeight = FontWeight.Bold) },
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
                            Text("💳 **** ${tarjeta.numeroTarjeta.takeLast(4)}", Modifier.padding(start = 8.dp))
                        }
                    }
                    TextButton(onClick = { showForm = true }) { Text("+ Agregar Tarjeta") }
                } else {
                    OutlinedTextField(value = num, onValueChange = { num = it }, label = { Text("Número") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = exp, onValueChange = { exp = it }, label = { Text("MM/AA") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = cvv, onValueChange = { cvv = it }, label = { Text("CVV") }, modifier = Modifier.fillMaxWidth())

                    // Busca el botón "Guardar y Seleccionar" y déjalo así:
                    Button(
                        onClick = {
                            if (num.isNotEmpty() && exp.isNotEmpty() && cvv.isNotEmpty()) {
                                val nueva = MetodoPago(
                                    numeroTarjeta = num, // Asegúrate de que este nombre coincida con tu Models.kt
                                    fechaVencimiento = exp,
                                    cvv = cvv
                                )
                                onSaveCard(nueva) // <--- ESTO ENVÍA LA TARJETA AL BACKEND
                                selectedValue = num // La selecciona para el pedido
                                showForm = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrownDark)
                    ) {
                        Text("Guardar y Seleccionar")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                // QUITAMOS EL ENABLED TEMPORALMENTE PARA PROBAR EL CLIC
                onClick = {
                    Log.d("CletaEats", "BOTON CLICK: Intentando confirmar con $selectedValue")
                    if (selectedValue.isNotEmpty()) onConfirm(selectedValue)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = BrownDark)
            ) {
                if (isSubmitting) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                else Text("Pagar y Finalizar")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cerrar") } }
    )
}