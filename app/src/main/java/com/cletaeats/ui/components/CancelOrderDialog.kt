package com.cletaeats.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.cletaeats.network.PedidoItem
import com.cletaeats.ui.theme.BrownDark
import com.cletaeats.ui.theme.BrownMid
import com.cletaeats.ui.theme.Cream
import com.cletaeats.ui.theme.TextDark

@Composable
fun CancelOrderDialog(
    order: PedidoItem,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cancelar Pedido", fontWeight = FontWeight.Bold, color = BrownDark) },
        text = { Text("¿Estás seguro de que deseas cancelar este pedido? Esta acción no se puede deshacer.", color = TextDark) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text("Sí, Cancelar", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = BrownMid)
            }
        },
        containerColor = Cream
    )
}
