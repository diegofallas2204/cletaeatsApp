package com.cletaeats.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.cletaeats.network.CartItem
import com.cletaeats.ui.theme.*

@Composable
fun CartSummaryDialog(
    cartItems: List<CartItem>,
    onCartItemChange: (CartItem) -> Unit,
    onDeleteCartItem: (CartItem) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(24.dp),
            color = Cream
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Tu Carrito",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = BrownDark
                )
                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(cartItems) { item ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = WhiteCard),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(item.combo.nombre, fontWeight = FontWeight.Bold, color = TextDark)
                                        Row(
                                            modifier = Modifier.padding(top = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text("Cantidad:", color = BrownMid, style = MaterialTheme.typography.bodySmall)
                                            IconButton(
                                                onClick = {
                                                    if (item.cantidad > 1) {
                                                        onCartItemChange(item.copy(cantidad = item.cantidad - 1))
                                                    }
                                                },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Text("−", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = BrownDark)
                                            }
                                            Text(
                                                "${item.cantidad}",
                                                fontWeight = FontWeight.Bold,
                                                color = TextDark,
                                                modifier = Modifier.width(24.dp),
                                                textAlign = TextAlign.Center
                                            )
                                            IconButton(
                                                onClick = {
                                                    onCartItemChange(item.copy(cantidad = item.cantidad + 1))
                                                },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Text("+", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = BrownDark)
                                            }
                                        }
                                    }
                                    val itemTotal = (item.combo.precio + if(item.agrandado) 1500.0 else 0.0) * item.cantidad
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "₡$itemTotal",
                                            fontWeight = FontWeight.ExtraBold,
                                            color = OrangeSoft,
                                            modifier = Modifier.padding(bottom = 4.dp)
                                        )
                                        IconButton(onClick = { onDeleteCartItem(item) }, modifier = Modifier.size(28.dp)) {
                                            Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Red, modifier = Modifier.size(20.dp))
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Agrandar combo (+₡1500 c/u)", color = TextMid, style = MaterialTheme.typography.bodySmall)
                                    Switch(
                                        checked = item.agrandado,
                                        onCheckedChange = { checked ->
                                            onCartItemChange(item.copy(agrandado = checked))
                                        },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = BrownDark
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                val subtotal = cartItems.sumOf { (it.combo.precio + if (it.agrandado) 1500.0 else 0.0) * it.cantidad }
                val iva = subtotal * 0.13
                val envio = 1500.0
                val total = subtotal + iva + envio

                Card(
                    colors = CardDefaults.cardColors(containerColor = WhiteCard),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                            Text("Subtotal", color = TextMid)
                            Text("₡${"%.2f".format(subtotal)}", fontWeight = FontWeight.Bold)
                        }
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                            Text("Envío", color = TextMid)
                            Text("₡${envio}", fontWeight = FontWeight.Bold)
                        }
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                            Text("I.V.A (13%)", color = TextMid)
                            Text("₡${"%.2f".format(iva)}", fontWeight = FontWeight.Bold)
                        }
                        HorizontalDivider(Modifier.padding(vertical = 8.dp), color = CreamDark)
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                            Text("Total", fontWeight = FontWeight.ExtraBold, color = BrownDark)
                            Text("₡${"%.2f".format(total)}", fontWeight = FontWeight.ExtraBold, color = OrangeSoft)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    TextButton(onClick = onDismiss) {
                        Text("Seguir comprando", color = BrownMid)
                    }
                    Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(containerColor = BrownDark)
                    ) {
                        Text("Proceder al Pago", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
