package com.cletaeats.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.cletaeats.network.*
import com.cletaeats.ui.components.ConnectionStatusBanner
import com.cletaeats.ui.components.RepartidorActiveTab
import com.cletaeats.ui.components.RepartidorBottomBar
import com.cletaeats.ui.theme.BrownDark
import com.cletaeats.ui.theme.Cream
import com.cletaeats.ui.tracking.RepartidorTrackingMapScreen
import com.cletaeats.utils.ConnectionState
import com.cletaeats.utils.connectivityState
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import com.cletaeats.utils.PedidoMergeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepartidorHomeScreen(onLogout: () -> Unit) {
    var activeTab by remember { mutableStateOf(RepartidorActiveTab.INICIO) }
    var pedidoSeleccionado by remember { mutableStateOf<PedidoItem?>(null) }
    var pedidos by remember { mutableStateOf<List<PedidoItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isRefreshing by remember { mutableStateOf(false) }
    var isSubmittingStatus by remember { mutableStateOf(false) }
    var isOnline by remember { mutableStateOf(true) }
    val connectionState by connectivityState()
    val networkOnline = connectionState is ConnectionState.Available
    
    val coroutineScope = rememberCoroutineScope()

    val context = androidx.compose.ui.platform.LocalContext.current
    val sqliteHelper = remember { com.cletaeats.database.CletaSQLiteHelper(context) }

    fun refreshData() {
        coroutineScope.launch {
            try {
                val t = TokenManager.token ?: return@launch
                
                // Hacemos las llamadas de forma independiente para que, si una falla (por ejemplo un 404 porque no se ha subido a Railway), la otra siga funcionando.
                var mios: List<PedidoItem> = emptyList()
                try {
                    val responseMios = CletaApi.retrofitService.getRepartidorPedidos("Bearer $t")
                    if (responseMios.success) mios = responseMios.data ?: emptyList()
                } catch (e: Exception) {
                    Log.e("CletaEats", "Error cargando pedidos asignados: ${e.message}")
                }

                var disp: List<PedidoItem> = emptyList()
                try {
                    val responseDisp = CletaApi.retrofitService.getPedidosDisponibles("Bearer $t")
                    if (responseDisp.success) disp = responseDisp.data ?: emptyList()
                } catch (e: Exception) {
                    Log.e("CletaEats", "Error cargando pedidos disponibles (¿Endpoint no existe en prod?): ${e.message}")
                }
                
                // Unificamos la lista evitando duplicados por ID
                val restaurantes = sqliteHelper.obtenerRestaurantes()
                val localPedidos = sqliteHelper.obtenerPedidos()
                val combined = (mios + disp).distinctBy { it.id }

                val mergedWithLocalOnly = PedidoMergeUtils.mergeWithLocalCache(
                    serverPedidos = combined,
                    localPedidos = localPedidos,
                    restaurantes = restaurantes
                )

                // Persistir la lista combinada en caché local para mantener estado offline
                try {
                    sqliteHelper.guardarPedidos(mergedWithLocalOnly)
                } catch (e: Exception) {
                    Log.w("CletaEats", "No se pudo guardar pedidos en SQLite: ${e.message}")
                }

                pedidos = mergedWithLocalOnly
            } catch (e: Exception) {
                Log.e("CletaEats", "Error crítico en refreshData: ${e.message}")
                pedidos = sqliteHelper.obtenerPedidos()
            } finally {
                isLoading = false
                isRefreshing = false
            }
        }
    }

    LaunchedEffect(Unit) {
        isLoading = true
        while (true) {
            refreshData()
            delay(5000) // Poll cada 5 segundos para tiempo real
        }
    }

    // Re-escuchar cuando termine una sincronización global para refrescar datos
    LaunchedEffect(Unit) {
        com.cletaeats.database.SyncManager.syncCompleted.collect {
            refreshData()
        }
    }

    // Actualiza estado en todos los lugares de estado relevantes
    fun applyEstadoLocally(pedidoId: Int, nuevoEstado: String) {
        pedidos = pedidos.map { if (it.id == pedidoId) it.copy(estado = nuevoEstado) else it }
        if (pedidoSeleccionado?.id == pedidoId) {
            if (nuevoEstado == "entregado") {
                pedidoSeleccionado = null // cerrar mapa al entregar
            } else {
                pedidoSeleccionado = pedidoSeleccionado?.copy(estado = nuevoEstado)
            }
        }
        val actualizados = sqliteHelper.obtenerPedidos().map {
            if (it.id == pedidoId) it.copy(estado = nuevoEstado) else it
        }
        sqliteHelper.guardarPedidos(actualizados)
    }

    fun updateStatus(pedido: PedidoItem, nuevoEstado: String) {
        coroutineScope.launch {
            isSubmittingStatus = true
            // Actualizar UI inmediatamente para que el botón reaccione
            applyEstadoLocally(pedido.id, nuevoEstado)
            try {
                val t = TokenManager.token ?: return@launch
                val response = CletaApi.retrofitService.updateOrderStatus(
                    "Bearer $t",
                    pedido.id,
                    UpdateStatusRequest(nuevoEstado)
                )
                if (response.success) {
                    refreshData()
                } else {
                    Log.w("CletaEats", "updateStatus rechazado por API, encolando: ${response}")
                    val updateReq = com.cletaeats.database.UpdateStatusPayload(pedido.id, nuevoEstado)
                    com.cletaeats.database.SyncManager.guardarYSincronizar(
                        "UPDATE_ORDER_STATUS", com.google.gson.Gson().toJson(updateReq)
                    )
                }
            } catch (e: Exception) {
                Log.e("CletaEats", "Error al actualizar estado del pedido: ${e.message}")
                val updateReq = com.cletaeats.database.UpdateStatusPayload(pedido.id, nuevoEstado)
                com.cletaeats.database.SyncManager.guardarYSincronizar(
                    "UPDATE_ORDER_STATUS", com.google.gson.Gson().toJson(updateReq)
                )
            } finally {
                isSubmittingStatus = false
            }
        }
    }

    fun acceptOrder(pedido: PedidoItem) {
        coroutineScope.launch {
            val estadosActivos = setOf("aceptado", "camino", "en_camino", "en camino", "preparando", "preparacion")
            val tieneAsignado = pedidos.any { (it.estado?.lowercase() ?: "") in estadosActivos }
            if (tieneAsignado) {
                Log.e("CletaEats", "El repartidor ya tiene un pedido activo. No puede asignar otro.")
                return@launch
            }

            isSubmittingStatus = true
            try {
                val t = TokenManager.token ?: return@launch
                // Primero asignamos el pedido a este repartidor
                val responseAsignar = CletaApi.retrofitService.asignarPedido("Bearer $t", pedido.id)
                applyEstadoLocally(pedido.id, "aceptado")
                if (responseAsignar.success) {
                    CletaApi.retrofitService.updateOrderStatus("Bearer $t", pedido.id, UpdateStatusRequest("aceptado"))
                    Log.d("CletaEats", "Pedido ${pedido.id} aceptado en servidor.")
                    refreshData()
                } else {
                    Log.w("CletaEats", "asignarPedido rechazado, encolando: ${responseAsignar.error}")
                    val jsonUpdate = com.google.gson.Gson().toJson(
                        com.cletaeats.database.UpdateStatusPayload(pedido.id, "aceptado")
                    )
                    com.cletaeats.database.SyncManager.guardarYSincronizar("ASSIGN_ORDER", pedido.id.toString())
                    com.cletaeats.database.SyncManager.guardarAccionPendiente("UPDATE_ORDER_STATUS", jsonUpdate)
                }
            } catch (e: Exception) {
                Log.e("CletaEats", "Error al asignar pedido: ${e.message}")
                applyEstadoLocally(pedido.id, "aceptado")
                val jsonUpdate = com.google.gson.Gson().toJson(
                    com.cletaeats.database.UpdateStatusPayload(pedido.id, "aceptado")
                )
                com.cletaeats.database.SyncManager.guardarYSincronizar("ASSIGN_ORDER", pedido.id.toString())
                com.cletaeats.database.SyncManager.guardarAccionPendiente("UPDATE_ORDER_STATUS", jsonUpdate)
            } finally {
                isSubmittingStatus = false
            }
        }
    }

    if (pedidoSeleccionado != null) {
        RepartidorTrackingMapScreen(
            pedido = pedidoSeleccionado!!,
            isSubmitting = isSubmittingStatus,
            onBack = {
                pedidoSeleccionado = null
                activeTab = RepartidorActiveTab.HISTORIAL
            },
            onUpdateStatus = { ped, est -> updateStatus(ped, est) }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("🛵 CLETAEATS - REPARTIDOR", fontWeight = FontWeight.Bold, color = Color.White) },
                    actions = {
                        IconButton(onClick = onLogout) {
                            Icon(Icons.Default.Logout, "Logout", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = BrownDark)
                )
            },
            bottomBar = {
                RepartidorBottomBar(
                    activeTab = activeTab,
                    onTabSelect = { activeTab = it }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                ConnectionStatusBanner(networkOnline)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    if (isLoading) {
                        Box(Modifier.fillMaxSize(), Alignment.Center) {
                            CircularProgressIndicator(color = BrownDark)
                        }
                    } else {
                        val estadosActivos = setOf("aceptado", "camino", "en_camino", "en camino", "preparando", "preparacion")
                        val pedidoActivo = pedidos.firstOrNull { (it.estado?.lowercase() ?: "") in estadosActivos }
                        val tieneActivo = pedidoActivo != null

                        when (activeTab) {
                            RepartidorActiveTab.INICIO -> RepartidorInicioTab(
                                pedidos = pedidos,
                                isRefreshing = isRefreshing,
                                tieneActivo = tieneActivo,
                                pedidoActivo = pedidoActivo,
                                onAcceptOrder = { acceptOrder(it) },
                                onRefresh = { isRefreshing = true; refreshData() },
                                onVerActivo = { pedidoSeleccionado = pedidoActivo }
                            )
                            RepartidorActiveTab.HISTORIAL -> RepartidorHistorialTab(
                                pedidos = pedidos,
                                isSubmitting = isSubmittingStatus,
                                onUpdateStatus = { ped, est -> updateStatus(ped, est) },
                                onPedidoSelect = { pedido: PedidoItem -> pedidoSeleccionado = pedido }
                            )
                            RepartidorActiveTab.PERFIL -> RepartidorPerfilTab(
                                pedidos = pedidos,
                                isOnline = isOnline,
                                onOnlineToggle = { isOnline = it }
                            )
                        }
                    }
                }
            }
        }
    }
}
