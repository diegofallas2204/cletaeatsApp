package com.cletaeats.storage

import com.cletaeats.network.PedidoItem

/**
 * Almacenamiento en nube simulado (in-memory).
 * Representa pedidos que desbordaron el local o fueron enviados a cloud por el usuario.
 * Los datos se pierden al reiniciar la app, igual que un cloud sin persistencia offline.
 */
object CloudPedidoStorage {

    private val _pedidos = mutableListOf<PedidoItem>()

    val count: Int get() = _pedidos.size

    fun guardar(pedido: PedidoItem) {
        _pedidos.add(0, pedido)
    }

    fun obtenerTodos(): List<PedidoItem> = _pedidos.toList()

    fun vaciar() {
        _pedidos.clear()
    }

    /** Carga manual de pedidos (ej: al sincronizar desde la API). */
    fun cargarDesde(pedidos: List<PedidoItem>) {
        _pedidos.clear()
        _pedidos.addAll(pedidos)
    }
}
