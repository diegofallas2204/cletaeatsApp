package com.cletaeats.ui.tracking

import org.osmdroid.config.Configuration as OSMConfiguration
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.cletaeats.ui.theme.*
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderTrackingMapScreen(
    viewModel: TrackingViewModel,
    onBack: () -> Unit,
    onOrderCancelled: () -> Unit
) {
    var showCancelDialog by remember { mutableStateOf(false) }
    val routePoints = viewModel.routePoints
    val zoomed = remember { BooleanArray(1) }
    val appContext = LocalContext.current.applicationContext

    // Feedback de la cancelación: éxito o error del servidor (antes el estado se ignoraba).
    LaunchedEffect(viewModel.cancellationState) {
        when (val state = viewModel.cancellationState) {
            is CancellationState.Success ->
                Toast.makeText(appContext, state.message, Toast.LENGTH_SHORT).show()
            is CancellationState.Error ->
                Toast.makeText(appContext, state.error, Toast.LENGTH_LONG).show()
            else -> Unit
        }
    }

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
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            val context = LocalContext.current

            LaunchedEffect(Unit) {
                OSMConfiguration.getInstance().userAgentValue = context.packageName
            }

            AndroidView(
                factory = { ctx ->
                    MapView(ctx).apply {
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        controller.setZoom(14.0)
                        controller.setCenter(viewModel.restaurantPoint)
                    }
                },
                modifier = Modifier.fillMaxSize(),
                update = { mapView ->
                    mapView.onResume()
                    mapView.overlays.clear()

                    Marker(mapView).also { m ->
                        m.position = viewModel.restaurantPoint
                        m.title = viewModel.pedido.restauranteNombre ?: "Restaurante"
                        m.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        mapView.overlays.add(m)
                    }
                    Marker(mapView).also { m ->
                        m.position = viewModel.clientePoint
                        m.title = "Tu destino"
                        m.icon = TrackingCoordinates.createHouseIcon(mapView.context)
                        m.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        mapView.overlays.add(m)
                    }

                    if (routePoints.isNotEmpty()) {
                        Polyline().also { poly ->
                            poly.setPoints(routePoints)
                            poly.outlinePaint.color = android.graphics.Color.parseColor("#FF6600")
                            poly.outlinePaint.strokeWidth = 8f
                            mapView.overlays.add(0, poly)
                        }
                        if (!zoomed[0]) {
                            val bb = BoundingBox.fromGeoPoints(
                                listOf(viewModel.restaurantPoint, viewModel.clientePoint)
                            )
                            mapView.post { mapView.zoomToBoundingBox(bb, false, 120) }
                            zoomed[0] = true
                        }
                    }
                    mapView.invalidate()
                }
            )

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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        restauranteNombre, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = BrownDark,
                        maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                    Text("Pedido #${pedidoId}", color = TextMid, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Spacer(modifier = Modifier.width(12.dp))
                val (badgeColor, textColor) = when (status) {
                    "entregado" -> GreenAccent to Color.White
                    "cancelado" -> Color.Red to Color.White
                    else -> OrangeSoft to Color.White
                }
                Surface(shape = RoundedCornerShape(8.dp), color = badgeColor) {
                    Text(status.uppercase(), modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                         fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textColor, maxLines = 1)
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
                Column(modifier = Modifier.weight(1f)) {
                    Text("Destino:", fontWeight = FontWeight.Bold, color = BrownMid, fontSize = 12.sp)
                    Text(
                        "UNA Campus Benjamín Núñez", fontWeight = FontWeight.Bold, color = TextDark, fontSize = 14.sp,
                        maxLines = 2, overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
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
