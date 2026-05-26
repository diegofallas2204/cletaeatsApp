package com.cletaeats.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cletaeats.network.PedidoItem
import com.cletaeats.network.TokenManager
import com.cletaeats.ui.theme.*

@Composable
fun RepartidorPerfilTab(
    pedidos: List<PedidoItem>,
    isOnline: Boolean,
    onOnlineToggle: (Boolean) -> Unit
) {
    val username = TokenManager.username ?: "Repartidor Cleta"
    val completados = pedidos.filter { it.estado?.lowercase() == "entregado" }.size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Avatar
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(BrownDark),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Person, null, tint = Cream, modifier = Modifier.size(56.dp))
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = username,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = BrownDark
        )
        Text(
            text = "Repartidor Oficial • CletaEats",
            color = TextMid,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Metrics Grid Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Star, null, tint = OrangeSoft, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Calificación", color = TextMid, fontSize = 11.sp)
                    Text("⭐ 4.9", fontWeight = FontWeight.Bold, color = TextDark, fontSize = 15.sp)
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.DirectionsBike, null, tint = BrownLight, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Vehículo", color = TextMid, fontSize = 11.sp)
                    Text("Cleta", fontWeight = FontWeight.Bold, color = TextDark, fontSize = 15.sp)
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.CheckCircle, null, tint = GreenAccent, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Viajes", color = TextMid, fontSize = 11.sp)
                    Text("$completados", fontWeight = FontWeight.Bold, color = TextDark, fontSize = 15.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Connection Switch Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isOnline) GreenAccent else Color.Gray)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isOnline) "Disponible (Online)" else "Desconectado (Offline)",
                            fontWeight = FontWeight.Bold,
                            color = TextDark,
                            fontSize = 14.sp
                        )
                        Text(
                            text = if (isOnline) "Recibiendo nuevos pedidos en tu zona" else "Activa para recibir pedidos",
                            color = TextMid.copy(alpha = 0.7f),
                            fontSize = 11.sp
                        )
                    }
                }
                Switch(
                    checked = isOnline,
                    onCheckedChange = onOnlineToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = GreenAccent,
                        uncheckedThumbColor = Color.LightGray,
                        uncheckedTrackColor = Cream
                    )
                )
            }
        }
    }
}
