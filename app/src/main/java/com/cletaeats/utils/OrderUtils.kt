package com.cletaeats.utils

import android.annotation.SuppressLint
import com.cletaeats.network.CartItem
import com.cletaeats.network.CreateOrderPayload
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object OrderUtils {
    @SuppressLint("NewApi")
    fun createPayload(
        restaurantId: Int,
        cartItems: List<CartItem>,
        tarjetaSeleccionada: String
    ): CreateOrderPayload {
        return CreateOrderPayload(
            restauranteId = restaurantId,
            items = cartItems.map {
                com.cletaeats.network.CreateOrderItem(
                    comboId = it.combo.id,
                    cantidad = it.cantidad,
                    notas = if (it.agrandado) "Agrandado" else null
                )
            }
        )
    }
}
