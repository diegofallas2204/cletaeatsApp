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
import com.cletaeats.ui.components.RepartidorActiveTab
import com.cletaeats.ui.components.RepartidorBottomBar
import com.cletaeats.ui.theme.BrownDark
import com.cletaeats.ui.theme.Cream
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepartidorHomeScreen(onLogout: () -> Unit) {
    var activeTab by remember { mutableStateOf(RepartidorActiveTab.INICIO) }
    var pedidos by remember { mutableStateOf<List<PedidoItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isRefreshing by remember { mutableStateOf(false) }
    var isSubmittingStatus by remember { mutableStateOf(false) }
    var isOnline by remember { mutableStateOf(true) }
    
    val coroutineScope = rememberCoroutineScope()

    fun refreshData() {
        coroutineScope.launch {
            try {
                val t = TokenManager.token ?: return@launch
                val response = CletaApi.retrofitService.getRepartidorPedidos("Bearer $t")
                if (response.success) {
                    pedidos = response.data ?: emptyList()
                }
            } catch (e: Exception) {
                Log.e("CletaEats", "Error cargando pedidos repartidor: ${e.message}")
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

    fun updateStatus(pedido: PedidoItem, nuevoEstado: String) {
        coroutineScope.launch {
            isSubmittingStatus = true
            try {
                val t = TokenManager.token ?: return@launch
                val response = CletaApi.retrofitService.updateOrderStatus(
                    "Bearer $t",
                    pedido.id,
                    UpdateStatusRequest(nuevoEstado)
                )
                if (response.success) {
                    refreshData()
                    if (nuevoEstado == "aceptado") {
                        activeTab = RepartidorActiveTab.HISTORIAL
                    }
                }
            } catch (e: Exception) {
                Log.e("CletaEats", "Error al actualizar estado del pedido: ${e.message}")
            } finally {
                isSubmittingStatus = false
            }
        }
    }

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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator(color = BrownDark)
                }
            } else {
                when (activeTab) {
                    RepartidorActiveTab.INICIO -> RepartidorInicioTab(
                        pedidos = pedidos,
                        isRefreshing = isRefreshing,
                        onAcceptOrder = { updateStatus(it, "aceptado") },
                        onRefresh = { isRefreshing = true; refreshData() }
                    )
                    RepartidorActiveTab.HISTORIAL -> RepartidorHistorialTab(
                        pedidos = pedidos,
                        isSubmitting = isSubmittingStatus,
                        onUpdateStatus = { ped, est -> updateStatus(ped, est) }
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
