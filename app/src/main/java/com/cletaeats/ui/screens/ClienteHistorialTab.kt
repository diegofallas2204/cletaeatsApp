package com.cletaeats.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cletaeats.network.PedidoItem
import com.cletaeats.ui.components.OrderCard
import com.cletaeats.ui.theme.BrownDark
import com.cletaeats.ui.theme.Cream
import com.cletaeats.ui.theme.OrangeSoft
import com.cletaeats.ui.theme.WhiteCard

enum class OrderFilterStatus {
    TODOS, ACTIVOS, ENTREGADOS, CANCELADOS
}

@Composable
fun ClienteHistorialTab(
    historial: List<PedidoItem>,
    onTrackClick: (PedidoItem) -> Unit,
    onCancelClick: (PedidoItem) -> Unit,
    onRateClick: (PedidoItem) -> Unit = {},
    filterStatus: OrderFilterStatus = OrderFilterStatus.ACTIVOS,
    onFilterChange: (OrderFilterStatus) -> Unit = {}
) {
    // Función para mapear estado del backend a términos legibles
    fun normalizeStatus(estado: String?): String {
        val rawStatus = estado ?: "pendiente"
        return if (rawStatus == "suspendido") "cancelado" else rawStatus.lowercase()
    }

    // Filtrar pedidos según el filtro seleccionado
    val filteredHistorial = when (filterStatus) {
        OrderFilterStatus.TODOS -> historial
        OrderFilterStatus.ACTIVOS -> historial.filter {
            val status = normalizeStatus(it.estado)
            status != "cancelado" && status != "entregado"
        }
        OrderFilterStatus.ENTREGADOS -> historial.filter {
            normalizeStatus(it.estado) == "entregado"
        }
        OrderFilterStatus.CANCELADOS -> historial.filter {
            normalizeStatus(it.estado) == "cancelado"
        }
    }

    Column(Modifier.fillMaxSize().background(Cream).padding(16.dp)) {
        Text(
            text = "Mi Historial de Pedidos",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 20.sp,
            color = BrownDark,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // ── Filtros ──────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OrderFilterStatus.entries.forEach { filter ->
                ElevatedFilterChip(
                    selected = filterStatus == filter,
                    onClick = { onFilterChange(filter) },
                    label = {
                        Text(
                            when (filter) {
                                OrderFilterStatus.TODOS -> "Todos"
                                OrderFilterStatus.ACTIVOS -> "Activos"
                                OrderFilterStatus.ENTREGADOS -> "Entregados"
                                OrderFilterStatus.CANCELADOS -> "Cancelados"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = OrangeSoft,
                        selectedLabelColor = androidx.compose.ui.graphics.Color.White,
                        containerColor = WhiteCard,
                        labelColor = BrownDark
                    )
                )
            }
        }

        // ── Lista de pedidos ─────────────────────────────────────
        if (filteredHistorial.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = when (filterStatus) {
                        OrderFilterStatus.TODOS -> "Aún no tienes pedidos registrados."
                        OrderFilterStatus.ACTIVOS -> "No hay pedidos activos en este momento."
                        OrderFilterStatus.ENTREGADOS -> "No tienes pedidos entregados aún."
                        OrderFilterStatus.CANCELADOS -> "No hay pedidos cancelados."
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredHistorial) { pedido ->
                    OrderCard(
                        pedido = pedido,
                        onTrackClick = { onTrackClick(pedido) },
                        onCancelClick = { onCancelClick(pedido) },
                        onRateClick = if ((pedido.estado ?: "").lowercase() == "entregado") {
                            { onRateClick(pedido) }
                        } else null
                    )
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}
