package com.cletaeats.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cletaeats.ui.theme.BrownDark
import com.cletaeats.ui.theme.OrangeSoft

@Composable
fun RatingDialog(
    pedidoId: Int,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (rating: Int, comentario: String) -> Unit
) {
    var selectedRating by remember { mutableIntStateOf(0) }
    var comentario by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        shape = RoundedCornerShape(20.dp),
        title = {
            Text("Valorar Pedido #$pedidoId", fontWeight = FontWeight.ExtraBold, color = BrownDark)
        },
        text = {
            Column {
                Text("¿Cómo fue tu experiencia?", color = BrownDark, fontSize = 14.sp)
                Spacer(Modifier.height(12.dp))

                // Estrellas
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    (1..5).forEach { star ->
                        IconButton(
                            onClick = { selectedRating = star },
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(
                                imageVector = if (star <= selectedRating) Icons.Filled.Star else Icons.Outlined.StarOutline,
                                contentDescription = "$star estrellas",
                                tint = if (star <= selectedRating) OrangeSoft else Color.Gray,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = comentario,
                    onValueChange = { comentario = it },
                    label = { Text("Comentario (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedRating, comentario.trim()) },
                enabled = selectedRating > 0 && !isSubmitting,
                colors = ButtonDefaults.buttonColors(containerColor = OrangeSoft)
            ) {
                if (isSubmitting) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp))
                else Text("Enviar", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSubmitting) {
                Text("Cancelar")
            }
        }
    )
}
