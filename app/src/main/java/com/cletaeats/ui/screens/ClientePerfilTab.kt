package com.cletaeats.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Storage
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
import com.cletaeats.storage.StorageThreshold
import com.cletaeats.ui.components.CardBrandBadge
import com.cletaeats.ui.theme.*

@Composable
fun ClientePerfilTab(
    tarjetas: List<MetodoPago>,
    userProfile: UserProfile?,
    isApiMode: Boolean,
    isCloudForced: Boolean,
    onCloudForceToggle: (Boolean) -> Unit,
    isDiskExpansionMode: Boolean,
    onDiskExpansionToggle: (Boolean) -> Unit,
    isOnline: Boolean,
    currentThreshold: StorageThreshold,
    onThresholdChange: (StorageThreshold) -> Unit,
    localCount: Int,
    cloudCount: Int,
    onViewCloud: () -> Unit,
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
                                        // weight(1f) para que el texto largo (p.ej. "Mastercard")
                                        // no empuje el botón de borrar fuera de la fila.
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                CardBrandBadge(tarjeta.numeroTarjeta)
                                                Spacer(Modifier.width(6.dp))
                                                Text(
                                                    "**** **** **** ${tarjeta.numeroTarjeta.takeLast(4)}",
                                                    fontWeight = FontWeight.Bold,
                                                    color = TextDark,
                                                    maxLines = 1,
                                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                                )
                                            }
                                            Text(
                                                "Vence: ${tarjeta.fechaVencimiento}",
                                                fontSize = 11.sp,
                                                color = TextMid
                                            )
                                        }
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

        // ── Almacenamiento ───────────────────────────────────────────────────
        item {
            StorageSettingsCard(
                isApiMode = isApiMode,
                isCloudForced = isCloudForced,
                onCloudForceToggle = onCloudForceToggle,
                isDiskExpansionMode = isDiskExpansionMode,
                onDiskExpansionToggle = onDiskExpansionToggle,
                isOnline = isOnline,
                currentThreshold = currentThreshold,
                onThresholdChange = onThresholdChange,
                localCount = localCount,
                cloudCount = cloudCount,
                onViewCloud = onViewCloud
            )
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
private fun StorageSettingsCard(
    isApiMode: Boolean,
    isCloudForced: Boolean,
    onCloudForceToggle: (Boolean) -> Unit,
    isDiskExpansionMode: Boolean,
    onDiskExpansionToggle: (Boolean) -> Unit,
    isOnline: Boolean,
    currentThreshold: StorageThreshold,
    onThresholdChange: (StorageThreshold) -> Unit,
    localCount: Int,
    cloudCount: Int,
    onViewCloud: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = WhiteCard),
        border = BorderStroke(1.dp, CreamDark)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {

            // ── Encabezado + badge de modo actual ─────────────────────
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Storage, contentDescription = null, tint = OrangeSoft, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Almacenamiento", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = BrownDark)
                Spacer(Modifier.weight(1f))
                val (badgeColor, badgeText) = when {
                    !isOnline           -> Pair(OrangeSoft, "SIN CONEXIÓN")
                    isCloudForced       -> Pair(CloudBlue, "NUBE")
                    isDiskExpansionMode -> Pair(BrownMid, "EXPANSIÓN")
                    else                -> Pair(GreenAccent, "API")
                }
                Surface(color = badgeColor.copy(alpha = 0.15f), shape = RoundedCornerShape(6.dp)) {
                    Text(
                        badgeText,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = badgeColor
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(
                when {
                    !isOnline           -> "Sin internet · Local ilimitado"
                    isCloudForced       -> "Pedidos directo a la nube simulada"
                    isDiskExpansionMode -> "Local hasta ${currentThreshold.limit} pedidos, luego nube · se reinicia al cerrar"
                    else                -> "Pedidos al servidor remoto (modo normal)"
                },
                fontSize = 12.sp, color = TextMid
            )
            Spacer(Modifier.height(16.dp))

            // ── Toggle: Expansión de disco + cloud ────────────────────
            val expansionDisabled = !isOnline || isCloudForced
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Storage, contentDescription = null,
                        tint = if (isDiskExpansionMode) BrownMid else TextMid.copy(alpha = if (expansionDisabled) 0.4f else 1f),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            "Expansión de disco con cloud",
                            fontWeight = FontWeight.SemiBold, fontSize = 14.sp,
                            color = if (expansionDisabled) TextMid.copy(alpha = 0.4f) else TextDark
                        )
                        Text(
                            when {
                                !isOnline           -> "Requiere internet"
                                isCloudForced       -> "Incompatible con nube forzada"
                                else                -> "Local (${currentThreshold.limit} máx) → nube · se reinicia al cerrar"
                            },
                            fontSize = 12.sp,
                            color = if (expansionDisabled) TextMid.copy(alpha = 0.4f) else TextMid
                        )
                    }
                }
                Switch(
                    checked = isDiskExpansionMode,
                    onCheckedChange = onDiskExpansionToggle,
                    enabled = !expansionDisabled,
                    colors = SwitchDefaults.colors(checkedThumbColor = BrownMid, checkedTrackColor = BrownLight.copy(alpha = 0.4f))
                )
            }

            // Selector de capacidad local (solo en expansión)
            if (isDiskExpansionMode) {
                Spacer(Modifier.height(8.dp))
                Text("Capacidad local: $localCount / ${currentThreshold.limit}", fontSize = 12.sp, color = TextMid)
                Spacer(Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StorageThreshold.entries.forEach { option ->
                        FilterChip(
                            selected = currentThreshold == option,
                            onClick = { onThresholdChange(option) },
                            label = { Text("${option.limit}", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = BrownMid, selectedLabelColor = WhiteCard)
                        )
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = CreamDark)

            // ── Toggle: Nube forzada ───────────────────────────────────
            val cloudDisabled = !isOnline || isDiskExpansionMode
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Cloud, contentDescription = null,
                        tint = if (isCloudForced) CloudBlue else TextMid.copy(alpha = if (cloudDisabled) 0.4f else 1f),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            "Forzar nube simulada",
                            fontWeight = FontWeight.SemiBold, fontSize = 14.sp,
                            color = if (cloudDisabled) TextMid.copy(alpha = 0.4f) else TextDark
                        )
                        Text(
                            when {
                                !isOnline           -> "Requiere internet"
                                isDiskExpansionMode -> "Incompatible con expansión de disco"
                                else                -> "Todos los pedidos van a la nube · se reinicia al cerrar"
                            },
                            fontSize = 12.sp,
                            color = if (cloudDisabled) TextMid.copy(alpha = 0.4f) else TextMid
                        )
                    }
                }
                Switch(
                    checked = isCloudForced,
                    onCheckedChange = onCloudForceToggle,
                    enabled = !cloudDisabled,
                    colors = SwitchDefaults.colors(checkedThumbColor = CloudBlue, checkedTrackColor = CloudBlueSoft)
                )
            }

            // ── Ver nube ───────────────────────────────────────────────
            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = CreamDark)
            OutlinedButton(
                onClick = onViewCloud,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = CloudBlue),
                border = BorderStroke(1.dp, CloudBlue)
            ) {
                Icon(Icons.Default.Cloud, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    if (cloudCount > 0) "Ver nube simulada ($cloudCount pedidos)" else "Ver nube simulada",
                    fontWeight = FontWeight.Bold
                )
            }
        }
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
