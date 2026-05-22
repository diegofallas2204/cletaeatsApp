package com.cletaeats.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Payments
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
fun RepartidorTabHistorial(pedidos: List<PedidoItem>) {
    val completados = pedidos.filter { 
        val est = it.estado?.lowercase() ?: ""
        est == "entregado" || est == "cancelado"
    }

    val totalGanado = completados.filter { it.estado?.lowercase() == "entregado" }.sumOf { it.total ?: 0.0 }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
            .padding(16.dp)
    ) {
        Text(
            text = "Historial de Repartos",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = BrownDark
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Earnings Summary Panel
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = BrownDark),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Total Ganado", color = Cream.copy(alpha = 0.8f), fontSize = 13.sp)
                    Text("CRC $totalGanado", color = Cream, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                }
                Card(
                    colors = CardDefaults.cardColors(containerColor = OrangeSoft.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Payments, null, tint = OrangeSoft, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("${completados.size} Viajes", color = OrangeSoft, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (completados.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.History, null, tint = BrownLight.copy(alpha = 0.6f), modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Aún no tienes entregas registradas", color = TextMid, fontSize = 14.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(completados) { pedido ->
                    val esEntregado = pedido.estado?.lowercase() == "entregado"
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = pedido.restauranteNombre ?: "Restaurante",
                                    fontWeight = FontWeight.Bold,
                                    color = TextDark,
                                    fontSize = 15.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (esEntregado) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                        contentDescription = null,
                                        tint = if (esEntregado) GreenAccent else RedAccent,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (esEntregado) "Entregado" else "Cancelado",
                                        color = if (esEntregado) GreenAccent else RedAccent,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Pedido #${pedido.id}", color = TextMid.copy(alpha = 0.7f), fontSize = 12.sp)
                                }
                            }
                            Text(
                                text = "CRC ${pedido.total ?: 0.0}",
                                fontWeight = FontWeight.Bold,
                                color = if (esEntregado) GreenAccent else TextMid,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
