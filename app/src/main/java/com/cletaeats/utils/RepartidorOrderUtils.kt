package com.cletaeats.utils

import com.cletaeats.network.PedidoItem

object RepartidorOrderUtils {

    private val ACTIVE_STATUSES = setOf(
        "aceptado", "camino", "en_camino", "en camino", "preparando", "preparacion"
    )

    fun hasActiveOrder(pedidos: List<PedidoItem>): Boolean =
        pedidos.any { normalizeStatus(it.estado) in ACTIVE_STATUSES }

    fun findActiveOrder(pedidos: List<PedidoItem>): PedidoItem? =
        pedidos.firstOrNull { normalizeStatus(it.estado) in ACTIVE_STATUSES }

    private fun normalizeStatus(estado: String?): String =
        (estado ?: "pendiente").lowercase()
}
