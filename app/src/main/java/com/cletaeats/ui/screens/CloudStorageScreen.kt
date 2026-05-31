package com.cletaeats.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cletaeats.network.PedidoItem
import com.cletaeats.ui.theme.CloudBlue
import com.cletaeats.ui.theme.CloudBlueSoft

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloudStorageScreen(
    pedidos: List<PedidoItem>,
    canSync: Boolean,
    isSyncing: Boolean,
    onBack: () -> Unit,
    onSync: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Cloud,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Text("Almacenamiento en Nube", color = Color.White)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                actions = {
                    if (canSync) {
                        IconButton(onClick = onSync, enabled = !isSyncing) {
                            if (isSyncing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Default.Sync, contentDescription = "Sincronizar con API", tint = Color.White)
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CloudBlue)
            )
        },
        containerColor = CloudBlueSoft
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // ── Encabezado de estado ──────────────────────────────────
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = CloudBlue.copy(alpha = 0.12f)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${pedidos.size} pedido(s) en nube simulada",
                        fontWeight = FontWeight.Bold,
                        color = CloudBlue,
                        fontSize = 14.sp
                    )
                    Text(
                        text = if (canSync) "☁ → API disponible" else "Sin conexión para sync",
                        color = CloudBlue.copy(alpha = 0.65f),
                        fontSize = 12.sp
                    )
                }
            }

            if (pedidos.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Cloud,
                            contentDescription = null,
                            tint = CloudBlue.copy(alpha = 0.25f),
                            modifier = Modifier.size(80.dp)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "La nube está vacía",
                            color = CloudBlue.copy(alpha = 0.55f),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Los pedidos aparecen aquí cuando\nla memoria local se llena",
                            color = CloudBlue.copy(alpha = 0.4f),
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(pedidos) { pedido ->
                        CloudPedidoCard(pedido = pedido)
                    }
                }
            }
        }
    }
}

@Composable
private fun CloudPedidoCard(pedido: PedidoItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Cloud,
                contentDescription = null,
                tint = CloudBlue,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Pedido #${pedido.id} · ${pedido.restauranteNombre ?: "Restaurante"}",
                    fontWeight = FontWeight.Bold,
                    color = CloudBlue,
                    fontSize = 14.sp
                )
                Text(
                    text = "CRC ${pedido.total ?: 0.0} · ${pedido.estado ?: "pendiente"}",
                    color = CloudBlue.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
                if (!pedido.fechaPedido.isNullOrBlank()) {
                    Text(
                        text = pedido.fechaPedido,
                        color = CloudBlue.copy(alpha = 0.45f),
                        fontSize = 11.sp
                    )
                }
            }
            Surface(
                color = CloudBlueSoft,
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = "☁ nube",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    color = CloudBlue,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
