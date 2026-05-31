package com.cletaeats.network

import android.util.Log
import com.cletaeats.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Cliente Retrofit centralizado contra Railway (misma base que Postman).
 */
object CletaApiClient {

    private const val TAG = "CletaEats"

    private val okHttpClient: OkHttpClient by lazy {
        val builder = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)

        if (BuildConfig.DEBUG) {
            val logging = HttpLoggingInterceptor { message ->
                Log.d(TAG, "HTTP: $message")
            }.apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            builder.addInterceptor(logging)
        }

        builder.build()
    }

    private val retrofit: Retrofit by lazy {
        val baseUrl = BuildConfig.API_BASE_URL
        Log.i(TAG, "API base URL: $baseUrl")
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val service: CletaApiService by lazy {
        retrofit.create(CletaApiService::class.java)
    }

    /** Verifica que el dispositivo alcanza Railway (equivalente a GET /api/restaurantes en Postman). */
    suspend fun pingRailway(): Boolean {
        return try {
            val response = service.getRestaurantes()
            val ok = response.success && !response.data.isNullOrEmpty()
            Log.i(TAG, "Ping Railway: success=${response.success}, restaurantes=${response.data?.size ?: 0}")
            ok
        } catch (e: Exception) {
            Log.e(TAG, "Ping Railway falló: ${e.message}")
            false
        }
    }
}
