package com.cletaeats.network

import android.content.Context
import android.content.SharedPreferences

/**
 * Gestiona la sesión del usuario de forma persistente usando SharedPreferences.
 * La sesión se mantiene aunque se cierre o cambie de tarea la app.
 * Solo se borra al hacer logout manual o desinstalar la app.
 */
object SessionManager {

    private const val PREFS_NAME = "cletaeats_session"
    private const val KEY_TOKEN    = "token"
    private const val KEY_USERNAME = "username"
    private const val KEY_ROL      = "rol"

    private lateinit var prefs: SharedPreferences

    /** Debe llamarse una sola vez en MainActivity.onCreate() */
    fun init(context: Context) {
        prefs = context.applicationContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // ── Token ──────────────────────────────────────────────────────────────
    var token: String?
        get() = prefs.getString(KEY_TOKEN, null)
        set(value) {
            prefs.edit().apply {
                if (value != null) putString(KEY_TOKEN, value) else remove(KEY_TOKEN)
                apply()
            }
        }

    // ── Username ───────────────────────────────────────────────────────────
    var username: String?
        get() = prefs.getString(KEY_USERNAME, null)
        set(value) {
            prefs.edit().apply {
                if (value != null) putString(KEY_USERNAME, value) else remove(KEY_USERNAME)
                apply()
            }
        }

    // ── Rol ────────────────────────────────────────────────────────────────
    var rol: String?
        get() = prefs.getString(KEY_ROL, null)
        set(value) {
            prefs.edit().apply {
                if (value != null) putString(KEY_ROL, value) else remove(KEY_ROL)
                apply()
            }
        }

    // ── Estado ─────────────────────────────────────────────────────────────
    val isLoggedIn: Boolean
        get() = token != null

    // ── Operaciones ────────────────────────────────────────────────────────

    /** Guarda los datos de sesión al hacer login exitoso. */
    fun saveSession(token: String, username: String, rol: String) {
        prefs.edit()
            .putString(KEY_TOKEN, token)
            .putString(KEY_USERNAME, username)
            .putString(KEY_ROL, rol)
            .apply()
    }

    /**
     * Borra la sesión. Solo debe llamarse desde el botón "Salir"
     * o al desinstalar la app (esto último es automático por el SO).
     */
    fun clearSession() {
        prefs.edit().clear().apply()
    }
}
