package com.cletaeats.ui.tracking

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.cletaeats.network.PedidoItem
import com.cletaeats.ui.theme.BrownDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepartidorTrackingMapScreen(
    pedido: PedidoItem,
    isSubmitting: Boolean,
    onBack: () -> Unit,
    onUpdateStatus: (PedidoItem, String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Seguimiento de Reparto", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BrownDark)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            RepartidorTrackingMapComponent(
                activo = pedido,
                isSubmitting = isSubmitting,
                onUpdateStatus = onUpdateStatus
            )
        }
    }
}