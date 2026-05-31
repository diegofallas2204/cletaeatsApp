package com.cletaeats.storage

import android.content.Context
import android.content.SharedPreferences

enum class StorageThreshold(val limit: Int, val label: String, val description: String) {
    PEQUENO(5,  "Pequeño (5 pedidos)",  "Se llena rápido – ideal para demo"),
    MEDIANO(10, "Mediano (10 pedidos)", "Uso normal"),
    GRANDE(20,  "Grande (20 pedidos)",  "Aguanta más antes de pasar a nube")
}

object LocalTransactionCounter {

    private const val PREFS_NAME = "cletaeats_storage_counter"
    private const val KEY_COUNT  = "local_pedido_count"
    private const val KEY_THRESHOLD = "storage_threshold"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.applicationContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    val count: Int get() = prefs.getInt(KEY_COUNT, 0)

    var threshold: StorageThreshold
        get() = try {
            StorageThreshold.valueOf(
                prefs.getString(KEY_THRESHOLD, StorageThreshold.MEDIANO.name)!!
            )
        } catch (e: Exception) {
            StorageThreshold.MEDIANO
        }
        set(value) { prefs.edit().putString(KEY_THRESHOLD, value.name).apply() }

    val isLocalFull: Boolean get() = count >= threshold.limit

    fun increment() {
        prefs.edit().putInt(KEY_COUNT, count + 1).apply()
    }

    fun reset() {
        prefs.edit().putInt(KEY_COUNT, 0).apply()
    }
}
