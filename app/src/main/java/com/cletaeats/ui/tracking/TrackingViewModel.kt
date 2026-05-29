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

class TrackingViewModel(val pedido: PedidoItem) : ViewModel() {

    var cancellationState by mutableStateOf<CancellationState>(CancellationState.Idle)
        private set

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
                    // Servidor rechazó la cancelación: encolar para reintento
                    com.cletaeats.database.SyncManager.handleOfflineCancel(pedido.id)
                    cancellationState = CancellationState.Success("Cancelación guardada, se enviará al servidor pronto")
                    onDone()
                }
            } catch (e: Exception) {
                com.cletaeats.database.SyncManager.handleOfflineCancel(pedido.id)
                cancellationState = CancellationState.Success("Cancelación guardada localmente (sin conexión)")
                onDone()
            }
        }
    }
}
