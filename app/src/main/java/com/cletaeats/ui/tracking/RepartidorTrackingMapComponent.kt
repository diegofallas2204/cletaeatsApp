package com.cletaeats.ui.tracking

import org.osmdroid.config.Configuration as OSMConfiguration
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.cletaeats.network.PedidoItem
import com.cletaeats.ui.theme.*
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

@Composable
fun RepartidorTrackingMapComponent(
    activo: PedidoItem,
    isSubmitting: Boolean,
    onUpdateStatus: (PedidoItem, String) -> Unit,
    onBack: (() -> Unit)? = null,
    restaurantDireccion: String? = null
) {
    val context = LocalContext.current
    val restaurantPoint = remember(activo.restauranteNombre, restaurantDireccion) {
        TrackingCoordinates.forRestaurante(activo.restauranteNombre, restaurantDireccion)
    }
    val clientePoint = TrackingCoordinates.CLIENTE_POINT
    var routePoints by remember { mutableStateOf<List<GeoPoint>>(emptyList()) }
    val zoomed = remember { BooleanArray(1) }

    LaunchedEffect(Unit) {
        OSMConfiguration.getInstance().userAgentValue = context.packageName
    }

    LaunchedEffect(restaurantPoint) {
        routePoints = TrackingCoordinates.fetchOsrmRoute(restaurantPoint, clientePoint)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    controller.setZoom(14.0)
                    controller.setCenter(restaurantPoint)
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = { mapView ->
                mapView.onResume()
                mapView.overlays.clear()

                Marker(mapView).also { m ->
                    m.position = restaurantPoint
                    m.title = activo.restauranteNombre ?: "Restaurante"
                    m.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    mapView.overlays.add(m)
                }
                Marker(mapView).also { m ->
                    m.position = clientePoint
                    m.title = "Destino del cliente"
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
                        val bb = BoundingBox.fromGeoPoints(listOf(restaurantPoint, clientePoint))
                        mapView.post { mapView.zoomToBoundingBox(bb, false, 120) }
                        zoomed[0] = true
                    }
                }
                mapView.invalidate()
            }
        )

        Box(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            val estadoActual = activo.estado?.lowercase() ?: ""
            if (estadoActual == "suspendido" || estadoActual == "cancelado") {
                RepartidorPedidoCanceladoCard(activo = activo, onBack = onBack)
            } else {
                RepartidorTrackingDetailsCard(
                    activo = activo,
                    isSubmitting = isSubmitting,
                    onUpdateStatus = onUpdateStatus
                )
            }
        }
    }
}

@Composable
private fun RepartidorTrackingDetailsCard(
    activo: PedidoItem,
    isSubmitting: Boolean,
    onUpdateStatus: (PedidoItem, String) -> Unit
) {
    val estado = activo.estado?.lowercase() ?: "preparacion"
    val esEnCamino = estado == "camino" || estado == "en_camino" || estado.contains("en camino")

    Card(
        modifier = Modifier.fillMaxWidth(),
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
                    Text(activo.restauranteNombre ?: "Restaurante", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = BrownDark)
                    Text("Pedido #${activo.id}", color = TextMid, fontSize = 14.sp)
                }
                Surface(shape = RoundedCornerShape(8.dp), color = if (esEnCamino) GreenAccent else OrangeSoft) {
                    Text(estado.uppercase(), modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                         fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            if (!activo.notas.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Cream)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Indicaciones del cliente:", color = TextMid, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(activo.notas, color = TextDark, fontSize = 13.sp)
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
                Column(horizontalAlignment = Alignment.End) {
                    Text("Total Ganancia:", color = TextMid, fontSize = 12.sp)
                    Text("CRC ${activo.total ?: 0.0}", fontWeight = FontWeight.ExtraBold, color = GreenAccent, fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { if (esEnCamino) onUpdateStatus(activo, "entregado") else onUpdateStatus(activo, "camino") },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = if (esEnCamino) GreenAccent else OrangeSoft),
                shape = RoundedCornerShape(12.dp),
                enabled = !isSubmitting
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Icon(if (esEnCamino) Icons.Default.CheckCircle else Icons.Default.DirectionsBike, null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (esEnCamino) "Confirmar Entrega" else "Confirmar Retiro (En Camino)",
                        fontWeight = FontWeight.Bold, color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun RepartidorPedidoCanceladoCard(activo: PedidoItem, onBack: (() -> Unit)?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                    Text(activo.restauranteNombre ?: "Restaurante", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = BrownDark)
                    Text("Pedido #${activo.id}", color = TextMid, fontSize = 14.sp)
                }
                Surface(shape = RoundedCornerShape(8.dp), color = Color.Red.copy(alpha = 0.15f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Cancel, null, tint = Color.Red, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("CANCELADO", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Red)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Cream)
            Spacer(modifier = Modifier.height(12.dp))
            Text("El cliente canceló este pedido.", fontWeight = FontWeight.Medium, color = TextMid, fontSize = 14.sp)
            Text("Queda libre para aceptar un nuevo reparto.", color = TextMid.copy(alpha = 0.75f), fontSize = 13.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { onBack?.invoke() },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrownDark),
                shape = RoundedCornerShape(12.dp)
            ) { Text("Volver al inicio", fontWeight = FontWeight.Bold, color = Color.White) }
        }
    }
}
