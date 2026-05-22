package com.cletaeats.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.cletaeats.network.ComboItem
import com.cletaeats.network.RestauranteItem
import com.cletaeats.network.UserProfile
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Caché local de datos estáticos (restaurantes y combos) usando SharedPreferences + Gson.
 *
 * Estrategia cache-first con TTL de 24 horas:
 *  - Si existe caché reciente → devuelve los datos locales inmediatamente (null = sin caché).
 *  - Si hay conexión y el caché es viejo o no existe → llama al API y actualiza el caché.
 *  - Si no hay conexión y el caché es viejo → usa el caché viejo (mejor que nada).
 *
 * Los PEDIDOS no se cachean aquí; siempre se consultan al servidor.
 */
object LocalCacheManager {

    private const val PREFS_NAME           = "cletaeats_cache"
    private const val KEY_RESTAURANTES     = "restaurantes_json"
    private const val KEY_RESTAURANTES_TS  = "restaurantes_ts"
    private const val KEY_COMBOS_PREFIX    = "combos_json_"
    private const val KEY_COMBOS_TS_PREFIX = "combos_ts_"
    private const val KEY_USER_PROFILE     = "user_profile_json"
    private const val KEY_USER_PROFILE_TS  = "user_profile_ts"

    /** TTL de 24 horas en milisegundos */
    private const val TTL_MS = 24 * 60 * 60 * 1000L

    private lateinit var prefs: SharedPreferences
    private val gson = Gson()

    /** Debe llamarse una sola vez en MainActivity.onCreate() */
    fun init(context: Context) {
        prefs = context.applicationContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Restaurantes
    // ─────────────────────────────────────────────────────────────────────────

    /** Guarda la lista de restaurantes junto con el timestamp actual. */
    fun saveRestaurantes(list: List<RestauranteItem>) {
        val json = gson.toJson(list)
        prefs.edit()
            .putString(KEY_RESTAURANTES, json)
            .putLong(KEY_RESTAURANTES_TS, System.currentTimeMillis())
            .apply()
        Log.d("LocalCache", "Restaurantes guardados en caché (${list.size} items)")
    }

    /**
     * Devuelve la lista de restaurantes si el caché es válido (< TTL).
     * Retorna null si no hay caché o si está vencido.
     */
    fun getRestaurantes(): List<RestauranteItem>? {
        val json = prefs.getString(KEY_RESTAURANTES, null) ?: return null
        val ts   = prefs.getLong(KEY_RESTAURANTES_TS, 0L)
        if (System.currentTimeMillis() - ts > TTL_MS) {
            Log.d("LocalCache", "Caché de restaurantes expirado")
            return null
        }
        return try {
            val type = object : TypeToken<List<RestauranteItem>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            Log.e("LocalCache", "Error parseando restaurantes: ${e.message}")
            null
        }
    }

    /**
     * Devuelve la lista aunque esté expirada (para uso offline).
     * Útil cuando no hay conexión y queremos mostrar algo.
     */
    fun getRestaurantesOffline(): List<RestauranteItem>? {
        val json = prefs.getString(KEY_RESTAURANTES, null) ?: return null
        return try {
            val type = object : TypeToken<List<RestauranteItem>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            null
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Combos por restaurante
    // ─────────────────────────────────────────────────────────────────────────

    /** Guarda los combos de un restaurante específico. */
    fun saveCombos(restauranteId: Int, list: List<ComboItem>) {
        val json = gson.toJson(list)
        prefs.edit()
            .putString("$KEY_COMBOS_PREFIX$restauranteId", json)
            .putLong("$KEY_COMBOS_TS_PREFIX$restauranteId", System.currentTimeMillis())
            .apply()
        Log.d("LocalCache", "Combos de restaurante $restauranteId guardados (${list.size} items)")
    }

    /**
     * Devuelve los combos de un restaurante si el caché es válido.
     * Retorna null si no hay caché o si está vencido.
     */
    fun getCombos(restauranteId: Int): List<ComboItem>? {
        val json = prefs.getString("$KEY_COMBOS_PREFIX$restauranteId", null) ?: return null
        val ts   = prefs.getLong("$KEY_COMBOS_TS_PREFIX$restauranteId", 0L)
        if (System.currentTimeMillis() - ts > TTL_MS) {
            Log.d("LocalCache", "Caché de combos de restaurante $restauranteId expirado")
            return null
        }
        return try {
            val type = object : TypeToken<List<ComboItem>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            Log.e("LocalCache", "Error parseando combos: ${e.message}")
            null
        }
    }

    /** Devuelve combos aunque estén expirados (para uso offline). */
    fun getCombosOffline(restauranteId: Int): List<ComboItem>? {
        val json = prefs.getString("$KEY_COMBOS_PREFIX$restauranteId", null) ?: return null
        return try {
            val type = object : TypeToken<List<ComboItem>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            null
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Perfil de usuario
    // ─────────────────────────────────────────────────────────────────────────

    /** Guarda el perfil completo del usuario. */
    fun saveUserProfile(profile: UserProfile) {
        val json = gson.toJson(profile)
        prefs.edit()
            .putString(KEY_USER_PROFILE, json)
            .putLong(KEY_USER_PROFILE_TS, System.currentTimeMillis())
            .apply()
        Log.d("LocalCache", "Perfil de usuario guardado en caché")
    }

    /**
     * Devuelve el perfil si el caché es válido (< TTL).
     * Retorna null si no hay caché o si está vencido.
     */
    fun getUserProfile(): UserProfile? {
        val json = prefs.getString(KEY_USER_PROFILE, null) ?: return null
        val ts   = prefs.getLong(KEY_USER_PROFILE_TS, 0L)
        if (System.currentTimeMillis() - ts > TTL_MS) {
            Log.d("LocalCache", "Caché de perfil expirado")
            return null
        }
        return try {
            gson.fromJson(json, UserProfile::class.java)
        } catch (e: Exception) {
            Log.e("LocalCache", "Error parseando perfil: ${e.message}")
            null
        }
    }

    /** Devuelve el perfil aunque esté expirado (para uso offline). */
    fun getUserProfileOffline(): UserProfile? {
        val json = prefs.getString(KEY_USER_PROFILE, null) ?: return null
        return try {
            gson.fromJson(json, UserProfile::class.java)
        } catch (e: Exception) {
            null
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Utilidades
    // ─────────────────────────────────────────────────────────────────────────

    /** Borra todo el caché local (útil en debug o al desinstalar). */
    fun clearAll() {
        prefs.edit().clear().apply()
        Log.d("LocalCache", "Caché local limpiado")
    }
}
