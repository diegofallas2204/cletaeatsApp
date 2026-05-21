package com.cletaeats.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cletaeats.network.ComboItem
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
