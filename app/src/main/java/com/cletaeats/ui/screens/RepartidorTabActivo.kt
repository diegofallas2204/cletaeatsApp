package com.cletaeats.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cletaeats.network.PedidoItem
import com.cletaeats.ui.theme.*

@Composable
fun RepartidorTabActivo(
    pedidos: List<PedidoItem>,
    isSubmitting: Boolean,
    onUpdateStatus: (PedidoItem, String) -> Unit
) {
    val activo = pedidos.firstOrNull { 
        val est = it.estado?.lowercase() ?: ""
        est == "aceptado" || est == "en_camino" || est == "en camino"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Entrega en Curso",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = BrownDark,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (activo == null) {
            Box(
                modifier = Modifier.fillMaxSize().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.DirectionsBike, null, tint = BrownLight, modifier = Modifier.size(56.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Sin entregas activas",
                            fontWeight = FontWeight.Bold,
                            color = TextDark,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Ve a la pestaña de 'Disponibles' para aceptar tu próximo pedido.",
                            color = TextMid,
                            fontSize = 14.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            val esEnCamino = activo.estado?.lowercase()?.contains("camino") == true

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Restaurant, null, tint = OrangeSoft, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = activo.restauranteNombre ?: "Restaurante",
                            fontWeight = FontWeight.ExtraBold,
                            color = TextDark,
                            fontSize = 18.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Pedido #${activo.id}", color = TextMid, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Divider(color = Cream)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Total Ganancia:", color = TextMid, fontSize = 13.sp)
                            Text(
                                "CRC ${activo.total ?: 0.0}",
                                fontWeight = FontWeight.ExtraBold,
                                color = GreenAccent,
                                fontSize = 18.sp
                            )
                        }
                        Box(
                            modifier = Modifier
                                .background(
                                    if (esEnCamino) GreenAccent.copy(alpha = 0.15f) else OrangeSoft.copy(alpha = 0.15f),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (esEnCamino) "En Camino" else "Por Retirar",
                                color = if (esEnCamino) GreenAccent else OrangeSoft,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    if (!activo.notas.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Indicaciones del cliente:", color = TextMid, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(activo.notas!!, color = TextDark, fontSize = 13.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Step Progress Indicator
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Pasos de Entrega", fontWeight = FontWeight.Bold, color = BrownDark, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = true, onClick = null, colors = RadioButtonDefaults.colors(selectedColor = GreenAccent))
                        Text("Aceptado y Asignado", fontWeight = FontWeight.Bold, color = TextDark, fontSize = 14.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = esEnCamino, onClick = null, colors = RadioButtonDefaults.colors(selectedColor = GreenAccent))
                        Text("Retirado en Restaurante (En Camino)", fontWeight = if (esEnCamino) FontWeight.Bold else FontWeight.Normal, color = if (esEnCamino) TextDark else Color.Gray, fontSize = 14.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = false, onClick = null)
                        Text("Entregado al Cliente", color = Color.Gray, fontSize = 14.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    if (esEnCamino) {
                        onUpdateStatus(activo, "entregado")
                    } else {
                        onUpdateStatus(activo, "en_camino")
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = if (esEnCamino) GreenAccent else OrangeSoft),
                shape = RoundedCornerShape(16.dp),
                enabled = !isSubmitting
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Icon(if (esEnCamino) Icons.Default.CheckCircle else Icons.Default.DirectionsBike, null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (esEnCamino) "Confirmar Entrega" else "Pedido Retirado - En Camino",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}
