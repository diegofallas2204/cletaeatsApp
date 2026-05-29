package com.cletaeats.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cletaeats.network.PedidoItem
import com.cletaeats.ui.theme.*

enum class OrderSortOption {
    RECIENTES, PRECIO_MAYOR, PRECIO_MENOR
}

@Composable
fun RepartidorInicioTab(
    pedidos: List<PedidoItem>,
    isRefreshing: Boolean,
    onAcceptOrder: (PedidoItem) -> Unit,
    onRefresh: () -> Unit
) {
    var sortOption by remember { mutableStateOf(OrderSortOption.RECIENTES) }

    val disponibles = pedidos.filter { 
        val est = it.estado?.lowercase() ?: ""
        // El backend devuelve "preparacion", "pendiente" o "preparando"
        est == "pendiente" || est == "preparando" || est == "preparacion"
    }

    val sortedDisponibles = when (sortOption) {
        OrderSortOption.RECIENTES -> disponibles.sortedByDescending { it.id }
        OrderSortOption.PRECIO_MAYOR -> disponibles.sortedByDescending { it.total ?: 0.0 }
        OrderSortOption.PRECIO_MENOR -> disponibles.sortedBy { it.total ?: 0.0 }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Pedidos Disponibles",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = BrownDark
            )
            TextButton(onClick = onRefresh, colors = ButtonDefaults.textButtonColors(contentColor = BrownMid)) {
                Text(if (isRefreshing) "Actualizando..." else "Actualizar")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Filtros / Ordenamiento
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OrderSortOption.entries.forEach { option ->
                ElevatedFilterChip(
                    selected = sortOption == option,
                    onClick = { sortOption = option },
                    label = {
                        Text(
                            when (option) {
                                OrderSortOption.RECIENTES -> "Más Recientes"
                                OrderSortOption.PRECIO_MAYOR -> "Precio: Mayor a Menor"
                                OrderSortOption.PRECIO_MENOR -> "Precio: Menor a Mayor"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = OrangeSoft,
                        selectedLabelColor = Color.White,
                        containerColor = WhiteCard,
                        labelColor = BrownDark
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (sortedDisponibles.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.DirectionsBike,
                        contentDescription = null,
                        modifier = Modifier.size(72.dp),
                        tint = BrownLight
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Buscando nuevos pedidos...",
                        fontWeight = FontWeight.Medium,
                        color = TextMid,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Mantén la app abierta para recibir alertas",
                        color = TextMid.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(sortedDisponibles) { pedido ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Restaurant, null, tint = OrangeSoft, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = pedido.restauranteNombre ?: "Restaurante",
                                    fontWeight = FontWeight.Bold,
                                    color = TextDark,
                                    fontSize = 16.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Pedido #${pedido.id}", color = TextMid, fontSize = 14.sp)
                                Text(
                                    text = "CRC ${pedido.total ?: 0.0}",
                                    fontWeight = FontWeight.Bold,
                                    color = GreenAccent,
                                    fontSize = 15.sp
                                )
                            }
                            if (!pedido.notas.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Notas: ${pedido.notas}",
                                    color = TextMid.copy(alpha = 0.8f),
                                    fontSize = 13.sp,
                                    modifier = Modifier
                                        .background(Cream.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { onAcceptOrder(pedido) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = OrangeSoft),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.LocalMall, null, tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Aceptar Pedido", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}
