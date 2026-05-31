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
    isOnline: Boolean,
    localCount: Int,
    localLimit: Int,
    modifier: Modifier = Modifier
) {
    val config = when (mode) {
        StorageMode.API -> return  // Sin banner en modo normal
        StorageMode.LOCAL -> Triple(
            OrangeSoft,
            Icons.Default.WifiOff,
            if (!isOnline) "Sin conexión · Almacenamiento local ilimitado"
            else "Sin servidor · Local ilimitado"
        )
        StorageMode.DISK_EXPANSION -> Triple(
            BrownMid,
            Icons.Default.Storage,
            "Expansión de disco · $localCount/$localLimit pedidos locales"
        )
        StorageMode.CLOUD_OVERFLOW -> Triple(
            CloudBlue,
            Icons.Default.Cloud,
            "Disco lleno ($localCount/$localLimit) · Desbordando a nube"
        )
        StorageMode.CLOUD_FORCED -> Triple(
            CloudBlue,
            Icons.Default.Cloud,
            "Nube forzada · Los pedidos van directo a la nube"
        )
    }

    val (bgColor, icon, text) = config

    Surface(modifier = modifier.fillMaxWidth(), color = bgColor) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            Text(text, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}
