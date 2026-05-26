package com.cletaeats.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.cletaeats.network.*
import com.cletaeats.ui.theme.BrownDark
import com.cletaeats.ui.theme.Cream
import kotlinx.coroutines.launch

enum class ActiveRepartidorTab { DISPONIBLES, ACTIVO, HISTORIAL, PERFIL }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepartidorHomeScreen(onLogout: () -> Unit) {
    var activeTab by remember { mutableStateOf(ActiveRepartidorTab.DISPONIBLES) }
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
        refreshData()
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
                        activeTab = ActiveRepartidorTab.ACTIVO
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
            NavigationBar(containerColor = BrownDark) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.LocalMall, "Disponibles", tint = if (activeTab == ActiveRepartidorTab.DISPONIBLES) Cream else Color.LightGray) },
                    label = { Text("Disponibles", color = if (activeTab == ActiveRepartidorTab.DISPONIBLES) Cream else Color.LightGray) },
                    selected = activeTab == ActiveRepartidorTab.DISPONIBLES,
                    onClick = { activeTab = ActiveRepartidorTab.DISPONIBLES }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.DirectionsBike, "Activo", tint = if (activeTab == ActiveRepartidorTab.ACTIVO) Cream else Color.LightGray) },
                    label = { Text("Activo", color = if (activeTab == ActiveRepartidorTab.ACTIVO) Cream else Color.LightGray) },
                    selected = activeTab == ActiveRepartidorTab.ACTIVO,
                    onClick = { activeTab = ActiveRepartidorTab.ACTIVO }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.History, "Historial", tint = if (activeTab == ActiveRepartidorTab.HISTORIAL) Cream else Color.LightGray) },
                    label = { Text("Historial", color = if (activeTab == ActiveRepartidorTab.HISTORIAL) Cream else Color.LightGray) },
                    selected = activeTab == ActiveRepartidorTab.HISTORIAL,
                    onClick = { activeTab = ActiveRepartidorTab.HISTORIAL }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, "Perfil", tint = if (activeTab == ActiveRepartidorTab.PERFIL) Cream else Color.LightGray) },
                    label = { Text("Perfil", color = if (activeTab == ActiveRepartidorTab.PERFIL) Cream else Color.LightGray) },
                    selected = activeTab == ActiveRepartidorTab.PERFIL,
                    onClick = { activeTab = ActiveRepartidorTab.PERFIL }
                )
            }
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
                    ActiveRepartidorTab.DISPONIBLES -> RepartidorTabDisponibles(
                        pedidos = pedidos,
                        isRefreshing = isRefreshing,
                        onAcceptOrder = { updateStatus(it, "aceptado") },
                        onRefresh = { isRefreshing = true; refreshData() }
                    )
                    ActiveRepartidorTab.ACTIVO -> RepartidorTabActivo(
                        pedidos = pedidos,
                        isSubmitting = isSubmittingStatus,
                        onUpdateStatus = { ped, est -> updateStatus(ped, est) }
                    )
                    ActiveRepartidorTab.HISTORIAL -> RepartidorTabHistorial(pedidos = pedidos)
                    ActiveRepartidorTab.PERFIL -> RepartidorTabPerfil(
                        pedidos = pedidos,
                        isOnline = isOnline,
                        onOnlineToggle = { isOnline = it }
                    )
                }
            }
        }
    }
}
