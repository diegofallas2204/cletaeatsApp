package com.cletaeats.network

import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import okhttp3.ResponseBody

private const val BASE_URL = "http://10.0.2.2:8080/"


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
    suspend fun createOrder(@Header("Authorization") token: String, @Body request: CreateOrderPayload): CletaResponse<String>

    @GET("api/cliente/pedidos/historial")
    suspend fun getClienteHistorial(@Header("Authorization") token: String): CletaResponse<List<PedidoItem>>

    @GET("api/cliente/tarjetas")
    suspend fun getTarjetas(@Header("Authorization") token: String): CletaResponse<List<MetodoPago>>

    @POST("api/cliente/tarjetas")
    suspend fun guardarTarjeta(@Header("Authorization") token: String, @Body tarjeta: MetodoPago): CletaResponse<MetodoPago>

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