package com.cletaeats.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cletaeats.network.MetodoPago
import com.cletaeats.network.SessionManager
import com.cletaeats.network.UserProfile
import com.cletaeats.ui.theme.*

@Composable
fun ClientePerfilTab(
    tarjetas: List<MetodoPago>,
    userProfile: UserProfile?,
    onAddCardClick: () -> Unit,
    onDeleteCard: (Int) -> Unit
) {
    var showDeleteConfirmDialog by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<MetodoPago?>(null) }
    val username = SessionManager.username ?: "Usuario"
    val rol      = SessionManager.rol ?: "cliente"
    val initials = (userProfile?.nombre ?: username).take(1).uppercase()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── Avatar + nombre ──────────────────────────────────────────────────
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = WhiteCard),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        modifier = Modifier.size(88.dp),
                        shape = CircleShape,
                        color = OrangeSoft.copy(alpha = 0.15f),
                        border = BorderStroke(2.dp, OrangeSoft)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = initials,
                                fontSize = 36.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = BrownDark
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = userProfile?.nombre ?: username,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = BrownDark
                    )
                    Text(
                        text = "@$username",
                        fontSize = 14.sp,
                        color = TextMid,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = BrownDark,
                    ) {
                        Text(
                            text = rol.uppercase(),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // ── Información personal ─────────────────────────────────────────────
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = WhiteCard),
                border = BorderStroke(1.dp, CreamDark)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "Información Personal",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = BrownDark,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    ProfileInfoRow(
                        icon  = Icons.Default.Person,
                        label = "Usuario",
                        value = username
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = CreamDark)

                    ProfileInfoRow(
                        icon  = Icons.Default.Person,
                        label = "Nombre completo",
                        value = userProfile?.nombre ?: "—"
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = CreamDark)

                    ProfileInfoRow(
                        icon  = Icons.Default.Email,
                        label = "Correo",
                        value = userProfile?.email ?: "—"
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = CreamDark)

                    ProfileInfoRow(
                        icon  = Icons.Default.Phone,
                        label = "Teléfono",
                        value = userProfile?.telefono ?: "—"
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = CreamDark)

                    ProfileInfoRow(
                        icon  = Icons.Default.Home,
                        label = "Dirección",
                        value = userProfile?.direccion ?: "—"
                    )
                }
            }
        }

        // ── Métodos de pago ──────────────────────────────────────────────────
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = WhiteCard),
                border = BorderStroke(1.dp, CreamDark)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Métodos de Pago",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = BrownDark
                        )
                        TextButton(onClick = onAddCardClick) {
                            Text("+ Agregar", color = OrangeSoft, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (tarjetas.isEmpty()) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No hay tarjetas de pago guardadas.",
                                color = TextMid,
                                fontSize = 14.sp
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            tarjetas.forEach { tarjeta ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Cream.copy(alpha = 0.3f)),
                                    border = BorderStroke(1.dp, CreamDark)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CreditCard,
                                            contentDescription = null,
                                            tint = BrownMid
                                        )
                                        Spacer(Modifier.width(12.dp))
                                        Column {
                                            val isVisa = tarjeta.numeroTarjeta.startsWith("4")
                                            val isMastercard = tarjeta.numeroTarjeta.startsWith("5")
                                            val cardBrand = if (isVisa) "VISA" else if (isMastercard) "Mastercard" else "Tarjeta"
                                            Text(
                                                "$cardBrand **** **** **** ${tarjeta.numeroTarjeta.takeLast(4)}",
                                                fontWeight = FontWeight.Bold,
                                                color = TextDark
                                            )
                                            Text(
                                                "Vence: ${tarjeta.fechaVencimiento}",
                                                fontSize = 11.sp,
                                                color = TextMid
                                            )
                                        }
                                        Spacer(Modifier.weight(1f))
                                        IconButton(onClick = { showDeleteConfirmDialog = tarjeta }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(Modifier.height(80.dp)) }
    }

    if (showDeleteConfirmDialog != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = null },
            title = { Text("Eliminar Tarjeta") },
            text = { Text("¿Deseas eliminar la tarjeta terminada en ${showDeleteConfirmDialog?.numeroTarjeta?.takeLast(4)}?") },
            confirmButton = {
                TextButton(onClick = { 
                    showDeleteConfirmDialog?.id?.let { onDeleteCard(it) }
                    showDeleteConfirmDialog = null 
                }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun ProfileInfoRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = OrangeSoft,
            modifier = Modifier.size(20.dp).padding(top = 2.dp)
        )
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, fontSize = 11.sp, color = TextMid, fontWeight = FontWeight.Bold)
            Text(value, fontSize = 14.sp, color = TextDark, fontWeight = FontWeight.Medium)
        }
    }
}
