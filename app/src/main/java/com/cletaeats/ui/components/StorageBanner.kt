package com.cletaeats.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cletaeats.storage.StorageMode
import com.cletaeats.ui.theme.*

@Composable
fun StorageBanner(
    mode: StorageMode,
    localCount: Int,
    localLimit: Int,
    modifier: Modifier = Modifier
) {
    val config = when (mode) {
        StorageMode.API -> return   // Sin banner en modo normal
        StorageMode.LOCAL -> Triple(
            OrangeSoft,
            Icons.Default.WifiOff,
            if (localCount > 0)
                "Modo local · $localCount/$localLimit pedidos"
            else
                "Sin servidor · Guardando en local"
        )
        StorageMode.CLOUD_OVERFLOW -> Triple(
            CloudBlue,
            Icons.Default.Cloud,
            "Memoria local llena ($localCount/$localLimit) · Usando nube simulada"
        )
        StorageMode.CLOUD_FORCED -> Triple(
            CloudBlue,
            Icons.Default.Cloud,
            "Nube activada manualmente · Los pedidos van a la nube"
        )
    }

    val (bgColor, icon, text) = config

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = bgColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = text,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
    }
}
