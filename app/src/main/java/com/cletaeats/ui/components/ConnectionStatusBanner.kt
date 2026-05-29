package com.cletaeats.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.cletaeats.ui.theme.RedAccent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun ConnectionStatusBanner(
    isOnline: Boolean,
    modifier: Modifier = Modifier
) {
    if (!isOnline) {
        Surface(
            modifier = modifier.fillMaxWidth(),
            color = RedAccent
        ) {
            Text(
                text = "Sin conexión. Los pedidos se guardarán en memoria local.",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
            )
        }
    }
}
