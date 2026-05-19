package com.cletaeats.ui.tracking

import android.annotation.SuppressLint
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.cletaeats.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderTrackingMapScreen(
    viewModel: TrackingViewModel,
    onBack: () -> Unit,
    onOrderCancelled: () -> Unit
) {
    var showCancelDialog by remember { mutableStateOf(false) }

    val routeJson = remember(viewModel.routePoints) {
        "[" + viewModel.routePoints.joinToString(",") { "[${it.latitude}, ${it.longitude}]" } + "]"
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Leaflet WebView Map
            LeafletMapView(
                restaurantName = viewModel.pedido.restauranteNombre ?: "Restaurante",
                restaurantLat = viewModel.restaurantLocation.latitude,
                restaurantLng = viewModel.restaurantLocation.longitude,
                clientLat = viewModel.clientLocation.latitude,
                clientLng = viewModel.clientLocation.longitude,
                routeJson = routeJson
            )

            // Details and control card at the bottom
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Cream),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    val rawStatus = viewModel.pedido.estado ?: "pendiente"
                    val status = if (rawStatus == "suspendido") "cancelado" else rawStatus

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = viewModel.pedido.restauranteNombre ?: "Restaurante",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                color = BrownDark
                            )
                            Text(
                                text = "Pedido #${viewModel.pedido.id}",
                                color = TextMid,
                                fontSize = 14.sp
                            )
                        }

                        // Status Badge
                        val (badgeColor, textColor) = when (status) {
                            "entregado" -> GreenAccent to Color.White
                            "cancelado" -> Color.Red to Color.White
                            else -> OrangeSoft to Color.White
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = badgeColor
                        ) {
                            Text(
                                text = status.uppercase(),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
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
                            Text(
                                text = "Destino:",
                                fontWeight = FontWeight.Bold,
                                color = BrownMid,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "UNA Campus Benjamín Núñez",
                                fontWeight = FontWeight.Bold,
                                color = TextDark,
                                fontSize = 14.sp
                            )
                        }

                        // Mock ETA
                        val etaText = when (status) {
                            "entregado" -> "Entregado"
                            "cancelado" -> "Cancelado"
                            "camino" -> "5 mins"
                            "preparando" -> "15 mins"
                            else -> "25 mins"
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Llegada aprox:",
                                color = TextMid,
                                fontSize = 12.sp
                            )
                            Text(
                                text = etaText,
                                fontWeight = FontWeight.ExtraBold,
                                color = OrangeSoft,
                                fontSize = 16.sp
                            )
                        }
                    }

                    // Cancel button if active
                    val isCancelable = status != "entregado" && status != "cancelado"
                    if (isCancelable) {
                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { showCancelDialog = true },
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
    }

    // Cancellation Dialog
    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Cancelar Pedido", fontWeight = FontWeight.Bold, color = BrownDark) },
            text = { Text("¿Estás seguro de que deseas cancelar este pedido? Esta acción no se puede deshacer.", color = TextDark) },
            confirmButton = {
                Button(
                    onClick = {
                        showCancelDialog = false
                        viewModel.cancelOrder {
                            onOrderCancelled()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Sí, Cancelar", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text("Cancelar", color = BrownMid)
                }
            },
            containerColor = Cream
        )
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun LeafletMapView(
    restaurantName: String,
    restaurantLat: Double,
    restaurantLng: Double,
    clientLat: Double,
    clientLng: Double,
    routeJson: String
) {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            WebView(context).apply {
                webViewClient = WebViewClient()
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.databaseEnabled = true
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                }
                settings.cacheMode = WebSettings.LOAD_NO_CACHE
                
                // Forzar renderizado por software para evitar fallos de GPU/rendernode en el emulador
                setLayerType(android.view.View.LAYER_TYPE_SOFTWARE, null)

                val html = """
                    <!DOCTYPE html>
                    <html>
                    <head>
                        <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
                        <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
                        <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
                        <style>
                            #map { height: 100vh; width: 100vw; margin: 0; padding: 0; }
                            body { margin: 0; padding: 0; }
                            .my-div-icon {
                                font-size: 28px;
                                line-height: 1;
                                text-align: center;
                            }
                        </style>
                    </head>
                    <body>
                        <div id="map"></div>
                        <script>
                            var map = L.map('map', {zoomControl: false}).setView([$restaurantLat, $restaurantLng], 12);
                            L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                                maxZoom: 19
                            }).addTo(map);
                            L.control.zoom({ position: 'topright' }).addTo(map);

                            var restIcon = L.divIcon({html: '🏪', className: 'my-div-icon', iconSize: [30, 30]});
                            var clientIcon = L.divIcon({html: '🎓', className: 'my-div-icon', iconSize: [30, 30]});
                            var bikeIcon = L.divIcon({html: '🚲', className: 'my-div-icon', iconSize: [35, 35]});

                            L.marker([$restaurantLat, $restaurantLng], {icon: restIcon}).addTo(map).bindPopup('$restaurantName').openPopup();
                            L.marker([$clientLat, $clientLng], {icon: clientIcon}).addTo(map).bindPopup('UNA Campus Benjamín Núñez');

                            var routeCoords = $routeJson;
                            var line = L.polyline(routeCoords, {color: '#8B5A2B', weight: 5, dashArray: '5, 8'}).addTo(map);
                            
                            // Fit bounds to show route
                            map.fitBounds(line.getBounds(), {padding: [50, 50]});

                            var riderMarker = L.marker([$restaurantLat, $restaurantLng], {icon: bikeIcon}).addTo(map);

                            var steps = [];
                            for (var i = 0; i < routeCoords.length - 1; i++) {
                                var start = routeCoords[i];
                                var end = routeCoords[i+1];
                                var subDiv = 40;
                                for (var j = 0; j < subDiv; j++) {
                                    var ratio = j / subDiv;
                                    var lat = start[0] + (end[0] - start[0]) * ratio;
                                    var lng = start[1] + (end[1] - start[1]) * ratio;
                                    steps.push([lat, lng]);
                                }
                            }
                            steps.push(routeCoords[routeCoords.length - 1]);

                            var currentStep = 0;
                            function animateRider() {
                                if (currentStep < steps.length) {
                                    riderMarker.setLatLng(steps[currentStep]);
                                    currentStep++;
                                    setTimeout(animateRider, 150);
                                }
                            }
                            // Start animation after a short delay
                            setTimeout(animateRider, 1000);
                        </script>
                    </body>
                    </html>
                """.trimIndent()

                loadDataWithBaseURL("https://openstreetmap.org", html, "text/html", "UTF-8", null)
            }
        },
        update = { /* No-op */ }
    )
}
