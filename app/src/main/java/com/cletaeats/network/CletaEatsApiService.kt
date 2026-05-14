package com.cletaeats.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import okhttp3.ResponseBody

private const val BASE_URL = "http://10.0.2.2:8080/"

// --- AUTH MODELS ---
data class LoginRequest(
    val username: String,
    val password: String
)

data class RegisterRequest(
    val username: String,
    val password: String,
    val rol: String
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
    val restauranteId: Int?,
    val restauranteNombre: String? = null,
    val total: Double?,
    val estado: String?,
    val fecha: String? = null
)

data class OrderRequest(
    val restauranteId: Int,
    val items: List<OrderItem>
)

data class OrderItem(
    val comboId: Int,
    val cantidad: Int,
    val notas: String? = null
)

data class UpdateStatusRequest(
    val estado: String
)

interface CletaApiService {
    @POST("api/usuarios/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("api/usuarios/registrar")
    suspend fun register(@Body request: RegisterRequest): CletaResponse<Any>

    // Restaurantes (Público)
    @GET("api/restaurantes")
    suspend fun getRestaurantes(): CletaResponse<List<RestauranteItem>>

    @GET("api/admin/combos")
    suspend fun getCombos(@Header("Authorization") token: String): CletaResponse<List<ComboItem>>

    @GET("api/admin/combos/{restauranteId}")
    suspend fun getCombosByRestaurant(
        @Header("Authorization") token: String,
        @Path("restauranteId") restauranteId: Int
    ): CletaResponse<List<ComboItem>>

    // Cliente
    @POST("api/cliente/pedidos")
    suspend fun createOrder(@Header("Authorization") token: String, @Body request: OrderRequest): CletaResponse<PedidoItem>

    @GET("api/cliente/pedidos/historial")
    suspend fun getClienteHistorial(@Header("Authorization") token: String): CletaResponse<List<PedidoItem>>

    // Repartidor
    @GET("api/repartidor/pedidos")
    suspend fun getRepartidorPedidos(@Header("Authorization") token: String): CletaResponse<List<PedidoItem>>

    @PUT("api/repartidor/pedidos/{pedidoId}/estado")
    suspend fun updateOrderStatus(
        @Header("Authorization") token: String,
        @Path("pedidoId") pedidoId: Int,
        @Body request: UpdateStatusRequest
    ): CletaResponse<Boolean>

    // For testing raw payload
    @GET("{path}")
    suspend fun getRawPayload(
        @Path("path", encoded = true) path: String,
        @Header("Authorization") token: String? = null
    ): ResponseBody
}

object CletaApi {
    private val retrofit = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl(BASE_URL)
        .build()

    val retrofitService: CletaApiService by lazy {
        retrofit.create(CletaApiService::class.java)
    }
}

object TokenManager {
    var token: String? = null
    var username: String? = null
    var rol: String? = null

    fun logout() {
        token = null
        username = null
        rol = null
    }
}