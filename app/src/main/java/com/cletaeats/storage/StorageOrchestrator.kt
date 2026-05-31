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

    /**
     * En memoria únicamente — se resetea al cerrar la app.
     * No se persiste en SharedPreferences.
     */
    var isDiskExpansionMode: Boolean = false
        set(value) {
            field = value
            if (value) {
                // Expansión usa umbral pequeño fijo (5 pedidos)
                LocalTransactionCounter.threshold = StorageThreshold.PEQUENO
            }
            Log.d(TAG, "StorageOrchestrator: diskExpansion = $value")
        }

    fun init(context: Context) {
        prefs = context.applicationContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sqliteHelper = CletaSQLiteHelper(context.applicationContext)
        // Arrancar siempre en estado limpio: API activo, cloud/expansión desactivados
        isDiskExpansionMode = false
        isCloudForced = false
        SyncManager.setApiMode(true)
        Log.d(TAG, "StorageOrchestrator: init → API=ON, cloud=OFF, expansion=OFF")
    }

    var isCloudForced: Boolean
        get() = prefs.getBoolean(KEY_CLOUD_FORCED, false)
        set(value) {
            prefs.edit().putBoolean(KEY_CLOUD_FORCED, value).apply()
            Log.d(TAG, "StorageOrchestrator: cloudForced = $value")
        }

    /**
     * Determina el modo de almacenamiento según el estado actual.
     *
     * Sin internet          → LOCAL (ilimitado, sin API ni cloud)
     * Cloud forzado         → CLOUD_FORCED
     * Expansión de disco    → DISK_EXPANSION o CLOUD_OVERFLOW según contador
     * Default (con internet)→ API
     */
    fun determinarModo(): StorageMode {
        val online = SyncManager.isOnline()

        if (!online) return StorageMode.LOCAL

        if (isCloudForced) return StorageMode.CLOUD_FORCED

        if (isDiskExpansionMode) {
            return if (LocalTransactionCounter.isLocalFull)
                StorageMode.CLOUD_OVERFLOW
            else
                StorageMode.DISK_EXPANSION
        }

        return StorageMode.API
    }

    fun guardarPedidoLocal(pedido: PedidoItem, sqlHelper: CletaSQLiteHelper? = null): StorageMode {
        val modo = determinarModo()
        val helper = sqlHelper ?: sqliteHelper

        return when {
            modo.isCloud() -> {
                CloudPedidoStorage.guardar(pedido)
                Log.d(TAG, "StorageOrchestrator: Pedido #${pedido.id} → NUBE ($modo)")
                modo
            }
            else -> {
                val actuales = helper.obtenerPedidos().toMutableList()
                actuales.add(0, pedido)
                helper.guardarPedidos(actuales)
                if (isDiskExpansionMode) LocalTransactionCounter.increment()
                Log.d(TAG, "StorageOrchestrator: Pedido #${pedido.id} → LOCAL (${LocalTransactionCounter.count}/${LocalTransactionCounter.threshold.limit})")
                modo
            }
        }
    }
}
