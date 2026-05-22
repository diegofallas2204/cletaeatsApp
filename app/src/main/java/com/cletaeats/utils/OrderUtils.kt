package com.cletaeats.utils

import android.annotation.SuppressLint
import com.cletaeats.network.CartItem
import com.cletaeats.network.CreateOrderPayload
import com.cletaeats.network.OrderItem
import com.cletaeats.network.OrderRequest
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object OrderUtils {
    @SuppressLint("NewApi")
    fun createPayload(
        restaurantId: Int,
        cartItems: List<CartItem>,
        tarjetaSeleccionada: String,
        isFeriado: Boolean = false
    ): CreateOrderPayload {
        val subtotal = cartItems.sumOf { (it.combo.precio + if (it.agrandado) 1500.0 else 0.0) * it.cantidad }
        val iva = subtotal * 0.13
        val envio = 1500.0
        val total = subtotal + iva + envio
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
        val ahora = LocalDateTime.now().format(formatter)

        return CreateOrderPayload(
            pedido = OrderRequest(
                restauranteId = restaurantId,
                subtotal = subtotal,
                costoEnvio = envio,
                iva = iva,
                total = total,
                distanciaKm = 5.0,
                fechaPedido = ahora,
                fechaEntrega = null,
                numeroTarjeta = tarjetaSeleccionada,
                notas = null,
                detalles = cartItems.map {
                    OrderItem(
                        comboId = it.combo.id,
                        cantidad = it.cantidad,
                        precio = it.combo.precio + if (it.agrandado) 1500.0 else 0.0,
                        notas = null,
                        agrandado = it.agrandado
                    )
                }
            ),
            esFeriado = isFeriado
        )
    }
}
