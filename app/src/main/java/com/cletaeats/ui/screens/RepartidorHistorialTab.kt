package com.cletaeats.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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

import com.cletaeats.ui.tracking.RepartidorTrackingMapComponent

enum class RepartidorFilterStatus {
    ACTIVOS, ENTREGADOS, CANCELADOS
}

@Composable
fun RepartidorHistorialTab(
    pedidos: List<PedidoItem>,
    isSubmitting: Boolean,
    onUpdateStatus: (PedidoItem, String) -> Unit
) {
    var filterStatus by remember { mutableStateOf(RepartidorFilterStatus.ACTIVOS) }
    var selectedActivo by remember { mutableStateOf<PedidoItem?>(null) }

    LaunchedEffect(filterStatus, pedidos) {
        if (filterStatus != RepartidorFilterStatus.ACTIVOS) {
            selectedActivo = null
        } else if (selectedActivo != null && pedidos.none { it.id == selectedActivo?.id }) {
            selectedActivo = null
        }
    }

    fun normalizeStatus(estado: String?): String {
        val rawStatus = estado ?: "pendiente"
        return if (rawStatus == "suspendido") "cancelado" else rawStatus.lowercase()
    }

    val filteredPedidos = when (filterStatus) {
        RepartidorFilterStatus.ACTIVOS -> pedidos.filter {
            val status = normalizeStatus(it.estado)
            status == "aceptado" || status == "en_camino" || status == "en camino"
        }
        RepartidorFilterStatus.ENTREGADOS -> pedidos.filter {
            normalizeStatus(it.estado) == "entregado"
        }
        RepartidorFilterStatus.CANCELADOS -> pedidos.filter {
            normalizeStatus(it.estado) == "cancelado"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
            .padding(16.dp)
    ) {
        Text(
            text = "Mis Repartos",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = BrownDark
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Filtros
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RepartidorFilterStatus.entries.forEach { filter ->
                ElevatedFilterChip(
                    selected = filterStatus == filter,
                    onClick = { filterStatus = filter },
                    label = {
                        Text(
                            when (filter) {
                                RepartidorFilterStatus.ACTIVOS -> "Activos"
                                RepartidorFilterStatus.ENTREGADOS -> "Entregados"
                                RepartidorFilterStatus.CANCELADOS -> "Cancelados"
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

        if (filterStatus == RepartidorFilterStatus.ACTIVOS) {
            if (selectedActivo == null) {
                if (filteredPedidos.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize().weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.DirectionsBike, null, tint = BrownLight, modifier = Modifier.size(56.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Sin entregas activas", fontWeight = FontWeight.Bold, color = TextDark, fontSize = 18.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Ve a la pestaña de 'Inicio' para aceptar tu próximo pedido.",
                                color = TextMid,
                                fontSize = 14.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredPedidos) { pedido ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(12.dp),
                                onClick = { selectedActivo = pedido }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(pedido.restauranteNombre ?: "Restaurante", fontWeight = FontWeight.Bold, color = TextDark)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("Pedido #${pedido.id}", color = TextMid, fontSize = 12.sp)
                                    }
                                    Text("Ver mapa", color = OrangeSoft, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            } else {
                Column(modifier = Modifier.fillMaxSize().weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { selectedActivo = null }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = BrownDark)
                        }
                        Column(modifier = Modifier.padding(end = 8.dp)) {
                            Text("Pedido aceptado", fontWeight = FontWeight.Bold, color = BrownDark)
                            Text("Toca Entregado al finalizar", color = TextMid, fontSize = 12.sp)
                        }
                    }
                    Box(modifier = Modifier.fillMaxSize()) {
                        RepartidorActivoContent(selectedActivo!!, isSubmitting, onUpdateStatus)
                    }
                }
            }
        } else {
            RepartidorListaHistorialContent(filteredPedidos, filterStatus)
        }
    }
}

@Composable
fun RepartidorActivoContent(
    activo: PedidoItem,
    isSubmitting: Boolean,
    onUpdateStatus: (PedidoItem, String) -> Unit
) {
    RepartidorTrackingMapComponent(
        activo = activo,
        isSubmitting = isSubmitting,
        onUpdateStatus = onUpdateStatus
    )
}

@Composable
fun RepartidorListaHistorialContent(
    pedidos: List<PedidoItem>,
    filterStatus: RepartidorFilterStatus
) {
    if (pedidos.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.History, null, tint = BrownLight.copy(alpha = 0.6f), modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = if (filterStatus == RepartidorFilterStatus.ENTREGADOS) "No tienes entregas registradas" else "No hay pedidos cancelados",
                    color = TextMid,
                    fontSize = 14.sp
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(pedidos) { pedido ->
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
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}
