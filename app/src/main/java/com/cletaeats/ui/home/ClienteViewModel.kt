package com.cletaeats.ui.home

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cletaeats.network.CletaApi
import com.cletaeats.network.TokenManager
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

class ClienteViewModel : ViewModel() {
    var cletaUiState: String by mutableStateOf("Cargando conexión con Backend...")
        private set

    init {
        testConnection()
    }

    private fun testConnection() {
        viewModelScope.launch {
            try {
                val response = CletaApi.retrofitService.getRestaurantes()
                cletaUiState = response.toString() // Muestra el JSON parseado
            } catch (e: Exception) {
                cletaUiState = "Error al conectar con Java BE: ${e.message}"
            }
        }
    }


}