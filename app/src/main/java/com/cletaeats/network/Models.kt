package com.cletaeats.network

import com.google.gson.annotations.SerializedName

// Estructura de respuesta unificada obligatoria


// Modelo del Combo basado en la DB (MySQL)
data class Combo(
    val numero_combo: Int,
    val nombre: String,
    val precio: Double,
    val descripcion: String?
)
// --- AUTH MODELS ---
data class LoginRequest(
    val username: String,
    val password: String
)

data class RegisterRequest(
    val username: String,
    val password: String,
    val rol: String,
    val nombre: String,
    val cedula: String,
    val direccion: String,
    val telefono: String,
    val email: String
)

data class LoginResponse(
    val success: Boolean,
    val token: String?,
    val rol: String?,
    val error: String?,
    val data: LoginData? = null
)

data class LoginData(
    val token: String?,
    val rol: String?
)

// --- GENERIC RESPONSE ---
data class CletaResponse<T>(
    // Usamos el nombre 'success' pero permitimos que lea 'exito' si viene así
    @SerializedName(value = "success", alternate = ["exito"])
    val success: Boolean,
    val data: T?,
    val error: String?
)

// --- DOMAIN MODELS ---
data class RestauranteItem(
    val id: Int,
    val nombre: String,
    val cedulaJuridica: String,
    val direccion: String,
    val tipoComida: String? = null
)

data class ComboItem(
    val id: Int,
    val restauranteId: Int,
    val numeroCombo: Int,
    val nombre: String,
    val precio: Double
)

data class PedidoItem(
    val id: Int,
    val restauranteNombre: String? = null,
    val total: Double? = 0.0,
    val estado: String? = "preparacion",
    val fechaPedido: String? = null,
    // Las notas del historial suelen venir en el detalle,
    // pero si el backend las concatena, déjalas aquí como opcionales
    val notas: String? = null
)

data class CreateOrderPayload(
    val pedido: OrderRequest,
    val esFeriado: Boolean
)

data class MetodoPago(
    val id: Int? = null,
    val clienteId: Int? = null,
    // Forzamos que Retrofit envíe "numeroTarjeta" al JSON
    @SerializedName("numeroTarjeta") val numeroTarjeta: String,
    val fechaVencimiento: String,
    val cvv: String
)

data class OrderRequest(
    val restauranteId: Int,
    val subtotal: Double,
    val costoEnvio: Double,
    val iva: Double,
    val total: Double,
    val distanciaKm: Double,
    val fechaPedido: String,
    val fechaEntrega: String?,
    @SerializedName("numeroTarjeta") val numeroTarjeta: String, // Java CamelCase
    val notas: String? = null,
    val detalles: List<OrderItem>
)
data class OrderItem(
    val comboId: Int,
    val cantidad: Int,
    val precio: Double,
    val notas: String? = null,
    val agrandado: Boolean = false
)

data class UpdateStatusRequest(
    val estado: String
)