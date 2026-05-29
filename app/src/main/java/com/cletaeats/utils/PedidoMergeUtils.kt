package com.cletaeats.utils

import com.cletaeats.network.PedidoItem
import com.cletaeats.network.RestauranteItem

object PedidoMergeUtils {

    private val LOCAL_PRIORITY_STATUSES = setOf(
        "aceptado", "en_camino", "en camino", "preparando", "entregado", "cancelado", "pendiente"
    )

    fun mergeWithLocalCache(
        serverPedidos: List<PedidoItem>,
        localPedidos: List<PedidoItem>,
        restaurantes: List<RestauranteItem>
    ): List<PedidoItem> {
        val localById = localPedidos.associateBy { it.id }

        val mergedFromServer = serverPedidos.map { pedido ->
            val local = localById[pedido.id]
            if (local != null) {
                val mergedStatus = when (local.estado?.lowercase()) {
                    in LOCAL_PRIORITY_STATUSES -> local.estado
                    else -> pedido.estado ?: local.estado
                }
                pedido.copy(
                    restauranteNombre = resolveRestauranteNombre(pedido, local, restaurantes),
                    estado = mergedStatus
                )
            } else {
                pedido.copy(restauranteNombre = resolveRestauranteNombre(pedido, null, restaurantes))
            }
        }

        return (mergedFromServer + localPedidos)
            .distinctBy { it.id }
            .map { pedido ->
                pedido.copy(
                    restauranteNombre = resolveRestauranteNombre(
                        pedido,
                        localById[pedido.id],
                        restaurantes
                    )
                )
            }
    }

    private fun resolveRestauranteNombre(
        pedido: PedidoItem,
        local: PedidoItem?,
        restaurantes: List<RestauranteItem>
    ): String {
        val localNombre = local?.restauranteNombre
        if (!localNombre.isNullOrBlank() && localNombre != "Restaurante") {
            return localNombre
        }
        if (!pedido.restauranteNombre.isNullOrBlank() && pedido.restauranteNombre != "Restaurante") {
            return pedido.restauranteNombre
        }
        if (pedido.restauranteId != null) {
            return restaurantes.find { it.id == pedido.restauranteId }?.nombre ?: "Restaurante"
        }
        return pedido.restauranteNombre ?: "Restaurante"
    }
}
