package com.cletaeats.ui.tracking

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cletaeats.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderTrackingMapScreen(
    viewModel: TrackingViewModel,
    onBack: () -> Unit,
    onOrderCancelled: () -> Unit
) {
    var showCancelDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Seguimiento de Pedido", fontWeight = FontWeight.Bold, color = Color.White) },
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
                .background(Brush.verticalGradient(colors = listOf(Cream, CreamDark)))
        ) {
            // Elegant premium graphic placeholder (replaces Leaflet Webview)
            Column(
                modifier = Modifier.fillMaxSize().padding(bottom = 240.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    modifier = Modifier.size(130.dp),
                    shape = RoundedCornerShape(65.dp),
                    color = OrangeSoft.copy(alpha = 0.15f),
                    border = BorderStroke(2.dp, OrangeSoft)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.DirectionsBike,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = BrownDark
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                Text("¡Tu cleta va volando!", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = BrownDark)
                Text("Preparando y enviando con amor", fontSize = 14.sp, color = TextMid)
            }

            // Details bottom card
            OrderTrackingDetailsCard(
                restauranteNombre = viewModel.pedido.restauranteNombre ?: "Restaurante",
                pedidoId = viewModel.pedido.id,
                estado = viewModel.pedido.estado ?: "pendiente",
                modifier = Modifier.align(Alignment.BottomCenter),
                onCancelClick = { showCancelDialog = true }
            )
        }
    }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Cancelar Pedido", fontWeight = FontWeight.Bold, color = BrownDark) },
            text = { Text("¿Estás seguro de que deseas cancelar este pedido? Esta acción no se puede deshacer.", color = TextDark) },
            confirmButton = {
                Button(
                    onClick = { showCancelDialog = false; viewModel.cancelOrder { onOrderCancelled() } },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) { Text("Sí, Cancelar", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) { Text("Cancelar", color = BrownMid) }
            },
            containerColor = Cream
        )
    }
}

@Composable
private fun OrderTrackingDetailsCard(
    restauranteNombre: String,
    pedidoId: Int,
    estado: String,
    modifier: Modifier = Modifier,
    onCancelClick: () -> Unit
) {
    val status = if (estado == "suspendido") "cancelado" else estado
    Card(
        modifier = modifier.fillMaxWidth().padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = WhiteCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(restauranteNombre, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = BrownDark)
                    Text("Pedido #${pedidoId}", color = TextMid, fontSize = 14.sp)
                }
                val (badgeColor, textColor) = when (status) {
                    "entregado" -> GreenAccent to Color.White
                    "cancelado" -> Color.Red to Color.White
                    else -> OrangeSoft to Color.White
                }
                Surface(shape = RoundedCornerShape(8.dp), color = badgeColor) {
                    Text(status.uppercase(), modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textColor)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = CreamDark)
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Destino:", fontWeight = FontWeight.Bold, color = BrownMid, fontSize = 12.sp)
                    Text("UNA Campus Benjamín Núñez", fontWeight = FontWeight.Bold, color = TextDark, fontSize = 14.sp)
                }
                val etaText = when (status) {
                    "entregado" -> "Entregado"
                    "cancelado" -> "Cancelado"
                    "camino" -> "5 mins"
                    "preparando" -> "15 mins"
                    else -> "25 mins"
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Llegada aprox:", color = TextMid, fontSize = 12.sp)
                    Text(etaText, fontWeight = FontWeight.ExtraBold, color = OrangeSoft, fontSize = 16.sp)
                }
            }

            if (status != "entregado" && status != "cancelado") {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onCancelClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red, contentColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cancelar Pedido", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
