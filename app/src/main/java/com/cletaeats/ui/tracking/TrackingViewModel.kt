package com.cletaeats.ui.tracking

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cletaeats.network.CletaApi
import com.cletaeats.network.PedidoItem
import com.cletaeats.network.TokenManager
import kotlinx.coroutines.launch

sealed interface CancellationState {
    object Idle : CancellationState
    object Loading : CancellationState
    data class Success(val message: String) : CancellationState
    data class Error(val error: String) : CancellationState
}

data class LatLngMock(val latitude: Double, val longitude: Double)

class TrackingViewModel(val pedido: PedidoItem) : ViewModel() {

    var cancellationState by mutableStateOf<CancellationState>(CancellationState.Idle)
        private set

    // Fixed client home coordinates: UNA Campus Benjamín Núñez in Heredia
    val clientLocation = LatLngMock(9.9702, -84.1292)

    val restaurantLocation: LatLngMock
    val routePoints: List<LatLngMock>

    init {
        val address = pedido.restauranteNombre?.lowercase() ?: ""
        restaurantLocation = when {
            address.contains("alajuela") -> LatLngMock(10.016, -84.214)
            address.contains("cartago") -> LatLngMock(9.864, -83.919)
            address.contains("heredia") -> LatLngMock(10.002, -84.116)
            else -> LatLngMock(9.9333, -84.0833) // San José default
        }

        routePoints = when {
            address.contains("alajuela") -> listOf(
                restaurantLocation,
                LatLngMock(10.005, -84.180),
                LatLngMock(9.988, -84.150),
                clientLocation
            )
            address.contains("cartago") -> listOf(
                restaurantLocation,
                LatLngMock(9.900, -83.990),
                LatLngMock(9.935, -84.050),
                LatLngMock(9.950, -84.090),
                clientLocation
            )
            address.contains("heredia") -> listOf(
                restaurantLocation,
                LatLngMock(9.988, -84.120),
                clientLocation
            )
            else -> listOf(
                restaurantLocation,
                LatLngMock(9.945, -84.095),
                LatLngMock(9.960, -84.110),
                clientLocation
            )
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
                    cancellationState = CancellationState.Error(response.error ?: "Error al cancelar")
                }
            } catch (e: Exception) {
                cancellationState = CancellationState.Error(e.message ?: "Error desconocido")
            }
        }
    }
}
