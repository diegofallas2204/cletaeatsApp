package com.cletaeats.storage

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.cletaeats.database.CletaSQLiteHelper
import com.cletaeats.database.SyncManager
import com.cletaeats.network.PedidoItem

object StorageOrchestrator {

    private const val TAG = "CletaEats"
    private const val PREFS_NAME = "cletaeats_storage_orch"
    private const val KEY_CLOUD_FORCED = "cloud_forced"

    private lateinit var prefs: SharedPreferences
    private lateinit var sqliteHelper: CletaSQLiteHelper

    fun init(context: Context) {
        prefs = context.applicationContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sqliteHelper = CletaSQLiteHelper(context.applicationContext)
    }

    var isCloudForced: Boolean
        get() = prefs.getBoolean(KEY_CLOUD_FORCED, false)
        set(value) {
            prefs.edit().putBoolean(KEY_CLOUD_FORCED, value).apply()
            Log.d(TAG, "StorageOrchestrator: cloud forzado = $value")
        }

    /**
     * Lógica central de decisión de modo.
     *
     * Sin internet → LOCAL sin límite (cloud no disponible).
     * Con internet + toggle cloud → CLOUD_FORCED.
     * Con internet + API activa → API.
     * Con internet + API caída + local lleno → CLOUD_OVERFLOW.
     * Con internet + API caída + local disponible → LOCAL.
     */
    fun determinarModo(): StorageMode {
        val online = SyncManager.isOnline()

        if (!online) {
            // Auto-apagar cloud forzado si se fue el internet
            if (isCloudForced) isCloudForced = false
            return StorageMode.LOCAL
        }

        if (isCloudForced) return StorageMode.CLOUD_FORCED

        if (SyncManager.isApiMode) return StorageMode.API

        // API caída, estamos en modo LOCAL
        return if (LocalTransactionCounter.isLocalFull) {
            StorageMode.CLOUD_OVERFLOW
        } else {
            StorageMode.LOCAL
        }
    }

    /**
     * Guarda un pedido en el destino correcto según el modo actual.
     * Retorna el modo donde fue guardado para que el llamador pueda reaccionar.
     */
    fun guardarPedidoLocal(pedido: PedidoItem, sqlHelper: CletaSQLiteHelper? = null): StorageMode {
        val modo = determinarModo()
        val helper = sqlHelper ?: sqliteHelper

        return when {
            modo.isCloud() -> {
                CloudPedidoStorage.guardar(pedido)
                Log.d(TAG, "StorageOrchestrator: Pedido #${pedido.id} → NUBE (modo=$modo, cloud=${CloudPedidoStorage.count})")
                modo
            }
            else -> {
                val actuales = helper.obtenerPedidos().toMutableList()
                actuales.add(0, pedido)
                helper.guardarPedidos(actuales)
                LocalTransactionCounter.increment()
                Log.d(TAG, "StorageOrchestrator: Pedido #${pedido.id} → LOCAL (${LocalTransactionCounter.count}/${LocalTransactionCounter.threshold.limit})")
                StorageMode.LOCAL
            }
        }
    }
}
