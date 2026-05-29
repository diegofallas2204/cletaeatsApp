package com.cletaeats.database

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.cletaeats.network.*
import com.cletaeats.utils.ConnectionState
import com.cletaeats.utils.currentConnectivityState
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.lang.Exception

object SyncManager {
    private const val TAG = "CletaEats"
    private const val PREFS_NAME = "cletaeats_sync_mode"
    private const val KEY_API_MODE = "api_mode"
    private lateinit var context: Context
    private lateinit var sqliteHelper: CletaSQLiteHelper
    private lateinit var prefs: SharedPreferences
    private val gson = Gson()
    private val scope = CoroutineScope(Dispatchers.IO)
    private var isSyncing = false

    private val _syncCompleted = kotlinx.coroutines.flow.MutableSharedFlow<Unit>()
    val syncCompleted = _syncCompleted.asSharedFlow()

    enum class DataSourceMode { API, LOCAL }

    fun init(appContext: Context) {
        context = appContext.applicationContext
        sqliteHelper = CletaSQLiteHelper(context)
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun isOnline(): Boolean =
        context.currentConnectivityState is ConnectionState.Available

    val dataSourceMode: DataSourceMode
        get() = if (prefs.getBoolean(KEY_API_MODE, true)) DataSourceMode.API else DataSourceMode.LOCAL

    val isApiMode: Boolean
        get() = dataSourceMode == DataSourceMode.API

    fun setApiMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_API_MODE, enabled).apply()
        Log.d(TAG, "SyncManager: modo de datos cambiado a ${if (enabled) "API" else "LOCAL"}")
    }

    fun guardarAccionPendiente(tipo: String, payload: String) {
        sqliteHelper.guardarAccionPendiente(tipo, payload)
    }

    /** Encola la acción y, si hay conexión disponible, lanza sincronización inmediatamente. */
    fun guardarYSincronizar(tipo: String, payload: String) {
        guardarAccionPendiente(tipo, payload)
        sincronizar()
    }

    fun handleOfflineCancel(orderId: Int) {
        val pendientes = sqliteHelper.obtenerAccionesPendientes()
        val pendingCreate = pendientes.find { accion ->
            accion.tipo == "CREATE_ORDER" && extractLocalOrderId(accion.payload) == orderId
        }
        if (pendingCreate != null) {
            sqliteHelper.eliminarAccionPendiente(pendingCreate.id)
            Log.d(TAG, "SyncManager: CREATE_ORDER local $orderId eliminado por cancelación offline")
        } else {
            guardarYSincronizar("CANCEL_ORDER", orderId.toString())
        }
        val actualizados = sqliteHelper.obtenerPedidos().map { pedido ->
            if (pedido.id == orderId) pedido.copy(estado = "cancelado") else pedido
        }
        sqliteHelper.guardarPedidos(actualizados)
    }

    fun sincronizar() {
        if (isSyncing) return
        if (!isOnline()) {
            Log.d(TAG, "SyncManager: Sin conexión, sincronización pospuesta.")
            return
        }
        isSyncing = true
        scope.launch {
            try {
                val token = TokenManager.token
                if (token == null) {
                    Log.d(TAG, "SyncManager: No hay token de autenticación disponible para sincronizar.")
                    isSyncing = false
                    return@launch
                }

                val bearerToken = "Bearer $token"
                val acciones = sqliteHelper.obtenerAccionesPendientes()
                if (acciones.isEmpty()) {
                    Log.d(TAG, "SyncManager: No hay acciones pendientes de sincronización.")
                    isSyncing = false
                    return@launch
                }

                Log.d(TAG, "SyncManager: Iniciando sincronización de ${acciones.size} acciones pendientes...")

                for (accion in acciones) {
                    var handled = false
                    try {
                        when (accion.tipo) {
                            "CREATE_ORDER" -> {
                                val (localOrderId, payload) = parseCreateOrderPayload(accion.payload)
                                val response = CletaApi.retrofitService.createOrder(bearerToken, payload)
                                if (response.success) {
                                    handled = true
                                    val serverOrderId = extractOrderId(response.data)
                                    if (localOrderId != null && serverOrderId != null && serverOrderId != localOrderId) {
                                        sqliteHelper.reemplazarPedidoId(localOrderId, serverOrderId)
                                        sqliteHelper.remapOrderIdInPendingActions(localOrderId, serverOrderId)
                                    }
                                } else {
                                    Log.w(TAG, "SyncManager: CREATE_ORDER no fue aceptado por el backend: ${response.error}")
                                    handled = false
                                }
                            }
                            "CANCEL_ORDER" -> {
                                val orderId = accion.payload.toIntOrNull()
                                if (orderId != null) {
                                    val response = CletaApi.retrofitService.cancelarPedido(bearerToken, orderId)
                                    handled = response.success
                                    if (!handled) Log.w(TAG, "SyncManager: CANCEL_ORDER falló: ${response.error}")
                                }
                            }
                            "SAVE_CARD" -> {
                                val payload = gson.fromJson(accion.payload, MetodoPago::class.java)
                                val response = CletaApi.retrofitService.guardarTarjeta(bearerToken, payload)
                                handled = response.success
                                if (!handled) Log.w(TAG, "SyncManager: SAVE_CARD falló: ${response.error}")
                            }
                            "DELETE_CARD" -> {
                                val cardId = accion.payload.toIntOrNull()
                                if (cardId != null) {
                                    val response = CletaApi.retrofitService.deleteTarjeta(bearerToken, cardId)
                                    handled = response.success
                                    if (!handled) Log.w(TAG, "SyncManager: DELETE_CARD falló: ${response.error}")
                                }
                            }
                            "ASSIGN_ORDER" -> {
                                val orderId = accion.payload.toIntOrNull()
                                if (orderId != null) {
                                    val response = CletaApi.retrofitService.asignarPedido(bearerToken, orderId)
                                    handled = response.success
                                    if (!handled) Log.w(TAG, "SyncManager: ASSIGN_ORDER falló: ${response.error}")
                                }
                            }
                            "UPDATE_ORDER_STATUS" -> {
                                val updateReq = gson.fromJson(accion.payload, UpdateStatusPayload::class.java)
                                if (updateReq != null) {
                                    val response = CletaApi.retrofitService.updateOrderStatus(
                                        bearerToken,
                                        updateReq.orderId,
                                        UpdateStatusRequest(updateReq.estado)
                                    )
                                    handled = response.success
                                    if (!handled) Log.w(TAG, "SyncManager: UPDATE_ORDER_STATUS falló: ${response.error}")
                                }
                            }
                            else -> {
                                Log.w(TAG, "SyncManager: Tipo de acción desconocido: ${accion.tipo}")
                                handled = true // Ignorar para no trabar la cola
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "SyncManager: Error al ejecutar acción ${accion.tipo}: ${e.message}")
                        handled = false
                    }

                    if (handled) {
                        sqliteHelper.eliminarAccionPendiente(accion.id)
                        Log.d(TAG, "SyncManager: Acción ${accion.tipo} (id: ${accion.id}) sincronizada con éxito.")
                    } else {
                        Log.w(TAG, "SyncManager: Deteniendo sincronización temporalmente por fallo en la red.")
                        break // Si falla una, detenemos la cola para mantener el orden secuencial
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "SyncManager: Error crítico durante sincronización: ${e.message}")
            } finally {
                isSyncing = false
                // Notify listeners that a sync attempt finished (successfully or not)
                try {
                    _syncCompleted.tryEmit(Unit)
                } catch (ex: Exception) {
                    Log.w(TAG, "SyncManager: no se pudo emitir evento de sincronización: ${ex.message}")
                }
            }
        }
    }

    private fun parseCreateOrderPayload(rawPayload: String): Pair<Int?, CreateOrderPayload> {
        return try {
            if (rawPayload.contains("\"request\"")) {
                val pending = gson.fromJson(rawPayload, PendingCreateOrderPayload::class.java)
                pending.localOrderId to pending.request
            } else if (rawPayload.contains("\"pedido\"")) {
                val legacy = gson.fromJson(rawPayload, LegacyCreateOrderPayload::class.java)
                legacy.pedido.restauranteId to CreateOrderPayload(
                    restauranteId = legacy.pedido.restauranteId,
                    items = legacy.pedido.detalles.map {
                        CreateOrderItem(
                            comboId = it.comboId,
                            cantidad = it.cantidad,
                            notas = it.notas
                        )
                    }
                )
            } else {
                val current = gson.fromJson(rawPayload, PendingCreateOrderPayload::class.java)
                current.localOrderId to current.request
            }
        } catch (e: Exception) {
            Log.e(TAG, "SyncManager: No se pudo interpretar CREATE_ORDER: ${e.message}")
            null to gson.fromJson(rawPayload, CreateOrderPayload::class.java)
        }
    }

    private fun extractOrderId(responseData: String?): Int? {
        if (responseData.isNullOrBlank()) return null
        return Regex("\\d+").find(responseData)?.value?.toIntOrNull()
    }

    private fun extractLocalOrderId(rawPayload: String): Int? {
        return try {
            if (rawPayload.contains("\"localOrderId\"")) {
                gson.fromJson(rawPayload, PendingCreateOrderPayload::class.java).localOrderId
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}

data class UpdateStatusPayload(
    val orderId: Int,
    val estado: String
)
