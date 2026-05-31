package com.cletaeats.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cletaeats.storage.LocalTransactionCounter
import com.cletaeats.storage.StorageThreshold
import com.cletaeats.ui.theme.*

@Composable
fun DataSourceModeToggle(
    isApiMode: Boolean,
    onToggle: (Boolean) -> Unit,
    isCloudForced: Boolean,
    onCloudForceToggle: (Boolean) -> Unit,
    isOnline: Boolean,
    currentThreshold: StorageThreshold,
    onThresholdChange: (StorageThreshold) -> Unit,
    localCount: Int,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {

        HorizontalDivider()

        // ── Toggle API / LOCAL ────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isApiMode) "Modo API" else "Modo Local",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = if (isApiMode)
                        "Trabaja contra el backend y guarda copia local"
                    else
                        "Trabaja solo con memoria local (sin servidor)",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Switch(checked = isApiMode, onCheckedChange = onToggle)
        }

        HorizontalDivider()

        // ── Toggle Cloud Forzado ──────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isCloudForced) Icons.Default.Cloud else Icons.Default.CloudOff,
                    contentDescription = null,
                    tint = if (isCloudForced) CloudBlue else TextMid,
                    modifier = Modifier.size(20.dp)
                )
                Column {
                    Text(
                        text = "Forzar almacenamiento en nube",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (!isOnline) TextMid.copy(alpha = 0.5f) else TextDark
                    )
                    Text(
                        text = if (!isOnline) "Requiere conexión a internet"
                            else "Bypasea el servidor, va directo a nube",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (!isOnline) TextMid.copy(alpha = 0.5f) else TextMid
                    )
                }
            }
            Switch(
                checked = isCloudForced,
                onCheckedChange = onCloudForceToggle,
                enabled = isOnline,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = CloudBlue,
                    checkedTrackColor = CloudBlueSoft
                )
            )
        }

        // ── Selector de umbral (solo visible fuera de API mode) ───────
        if (!isApiMode) {
            HorizontalDivider()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Límite del almacenamiento local",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = TextDark
                )
                Text(
                    text = "Al llenarse, nuevos pedidos van a la nube · Actual: $localCount/${currentThreshold.limit}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMid
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StorageThreshold.entries.forEach { option ->
                        FilterChip(
                            selected = currentThreshold == option,
                            onClick = { onThresholdChange(option) },
                            label = {
                                Text(
                                    text = when (option) {
                                        StorageThreshold.PEQUENO -> "Pequeño\n(${option.limit})"
                                        StorageThreshold.MEDIANO -> "Mediano\n(${option.limit})"
                                        StorageThreshold.GRANDE  -> "Grande\n(${option.limit})"
                                    },
                                    fontSize = 11.sp
                                )
                            },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = OrangeSoft,
                                selectedLabelColor = WhiteCard
                            )
                        )
                    }
                }
            }
        }

        HorizontalDivider()
    }
}
