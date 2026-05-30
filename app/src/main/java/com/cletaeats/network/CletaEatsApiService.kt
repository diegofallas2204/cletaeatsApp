package com.cletaeats.network

import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import okhttp3.ResponseBody

private const val BASE_URL = "https://cletaeatsbe-production.up.railway.app/"

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

    // Perfil del usuario autenticado
    @GET("api/usuarios/perfil")
    suspend fun getUserPerfil(@Header("Authorization") token: String): CletaResponse<UserProfile>

    // Cliente
    @POST("api/cliente/pedidos")
    suspend fun createOrder(@Header("Authorization") token: String, @Body request: CreateOrderPayload): CletaResponse<String>

    @GET("api/cliente/pedidos/historial")
    suspend fun getClienteHistorial(@Header("Authorization") token: String): CletaResponse<List<PedidoItem>>

    @GET("api/cliente/tarjetas")
    suspend fun getTarjetas(@Header("Authorization") token: String): CletaResponse<List<MetodoPago>>

    @POST("api/cliente/tarjetas")
    suspend fun guardarTarjeta(@Header("Authorization") token: String, @Body tarjeta: MetodoPago): CletaResponse<MetodoPago>

    @DELETE("api/cliente/tarjetas/{id}")
    suspend fun deleteTarjeta(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): CletaResponse<String>

    @POST("api/cliente/pedidos/{pedidoId}/valorar")
    suspend fun valorarPedido(
        @Header("Authorization") token: String,
        @Path("pedidoId") pedidoId: Int,
        @Body request: ValoracionRequest
    ): CletaResponse<String>

    @PUT("api/cliente/pedidos/{pedidoId}/cancelar")
    suspend fun cancelarPedido(
        @Header("Authorization") token: String,
        @Path("pedidoId") pedidoId: Int
    ): CletaResponse<String>

    // Repartidor
    @GET("api/repartidor/pedidos")
    suspend fun getRepartidorPedidos(@Header("Authorization") token: String): CletaResponse<List<PedidoItem>>

    @GET("api/repartidor/pedidos/disponibles")
    suspend fun getPedidosDisponibles(@Header("Authorization") token: String): CletaResponse<List<PedidoItem>>

    @PUT("api/repartidor/pedidos/{pedidoId}/asignar")
    suspend fun asignarPedido(
        @Header("Authorization") token: String,
        @Path("pedidoId") pedidoId: Int
    ): CletaResponse<String>

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

/**
 * Alias de compatibilidad que delega a SessionManager.
 * Todo el código existente que use TokenManager sigue funcionando sin cambios,
 * pero ahora los datos se persisten en disco a través de SessionManager.
 */
object TokenManager {
    var token: String?
        get() = SessionManager.token
        set(value) { SessionManager.token = value }

    var username: String?
        get() = SessionManager.username
        set(value) { SessionManager.username = value }

    var rol: String?
        get() = SessionManager.rol
        set(value) { SessionManager.rol = value }

    fun logout() {
        SessionManager.clearSession()
    }
}