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

// --- PERFIL DE USUARIO ---
data class UserProfile(
    @SerializedName(value = "username", alternate = ["usuario"])
    val username: String? = null,
    val nombre: String? = null,
    val cedula: String? = null,
    @SerializedName(value = "direccion", alternate = ["direccion_exacta"])
    val direccion: String? = null,
    val telefono: String? = null,
    val email: String? = null,
    val rol: String? = null
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

data class CartItem(
    val combo: ComboItem,
    var cantidad: Int,
    var agrandado: Boolean = false
)

data class PedidoItem(
    val id: Int,
    @SerializedName(value = "restauranteNombre", alternate = ["restaurante_nombre", "nombre_restaurante"])
    val restauranteNombre: String? = null,

    @SerializedName(value = "restauranteId", alternate = ["restaurante_id", "id_restaurante"])
    val restauranteId: Int? = null,

    val total: Double? = 0.0,
    val estado: String? = "pendiente",

    @SerializedName(value = "fechaPedido", alternate = ["fecha_pedido", "fecha"])
    val fechaPedido: String? = null,

    val notas: String? = null
)

data class CreateOrderPayload(
    val restauranteId: Int,
    val items: List<CreateOrderItem>
)

data class CreateOrderItem(
    val comboId: Int,
    val cantidad: Int,
    val notas: String? = null
)

data class PendingCreateOrderPayload(
    val localOrderId: Int? = null,
    val request: CreateOrderPayload
)

data class LegacyCreateOrderPayload(
    val pedido: OrderRequest,
    val esFeriado: Boolean? = null
)

data class MetodoPago(
    val id: Int? = null,
    val clienteId: Int? = null,
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
    @SerializedName("numeroTarjeta") val numeroTarjeta: String,
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
