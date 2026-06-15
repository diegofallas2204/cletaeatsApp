package com.cletaeats.ui.tracking

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cletaeats.network.CletaApi
import com.cletaeats.network.PedidoItem
import com.cletaeats.network.TokenManager
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint

sealed interface CancellationState {
    object Idle : CancellationState
    object Loading : CancellationState
    data class Success(val message: String) : CancellationState
    data class Error(val error: String) : CancellationState
}

class TrackingViewModel(
    val pedido: PedidoItem,
    restaurantDireccion: String? = null
) : ViewModel() {

    val restaurantPoint: GeoPoint = TrackingCoordinates.forRestaurante(pedido.restauranteNombre, restaurantDireccion)
    val clientePoint: GeoPoint = TrackingCoordinates.CLIENTE_POINT

    var cancellationState by mutableStateOf<CancellationState>(CancellationState.Idle)
        private set

    var routePoints by mutableStateOf<List<GeoPoint>>(emptyList())
        private set

    init {
        fetchRoute()
    }

    private fun fetchRoute() {
        viewModelScope.launch {
            routePoints = TrackingCoordinates.fetchOsrmRoute(restaurantPoint, clientePoint)
        }
    }

    fun cancelOrder(onDone: () -> Unit) {
        cancellationState = CancellationState.Loading
        viewModelScope.launch {
            try {
                val token = TokenManager.token
                if (token == null) {
                    cancellationState = CancellationState.Error("No token available")
                    return@launch
                }
                val response = CletaApi.retrofitService.cancelarPedido("Bearer $token", pedido.id)
                if (response.success) {
                    cancellationState = CancellationState.Success(response.data ?: "Pedido cancelado")
                    onDone()
                } else {
                    // El servidor respondió pero rechazó la cancelación (p. ej. el pedido ya
                    // va en camino o fue entregado). NO es un caso offline: surfacear el error
                    // en vez de marcarlo como cancelado localmente y crear divergencia de estado.
                    cancellationState = CancellationState.Error(
                        response.error ?: "No se pudo cancelar el pedido en su estado actual"
                    )
                }
            } catch (e: Exception) {
                Log.e("CletaEats", "Error cancelando pedido: ${e.message}")
                com.cletaeats.database.SyncManager.handleOfflineCancel(pedido.id)
                cancellationState = CancellationState.Success("Cancelación guardada localmente (sin conexión)")
                onDone()
            }
        }
    }
}
