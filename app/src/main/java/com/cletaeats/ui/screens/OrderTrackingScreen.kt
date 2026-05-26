package com.cletaeats.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cletaeats.ui.theme.*

@Composable
fun OrderTrackingScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(120.dp),
            shape = RoundedCornerShape(60.dp),
            color = BrownLight.copy(alpha = 0.2f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.DirectionsBike,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = BrownDark
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "¡Pedido en Camino!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = BrownDark
        )

        Text(
            text = "Tu comida llegará pronto a tu destino",
            style = MaterialTheme.typography.bodyLarge,
            color = TextMid
        )

        Spacer(modifier = Modifier.height(48.dp))

        TrackingStep("Pedido Recibido", "El restaurante ha aceptado tu orden", Icons.Default.CheckCircle, true)
        TrackingDivider()
        TrackingStep("Preparando", "Tu comida está en la cocina", Icons.Default.Restaurant, true)
        TrackingDivider()
        TrackingStep("En camino", "El repartidor va hacia tu casa", Icons.Default.DirectionsBike, false)

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = BrownDark, contentColor = Color.White),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Entendido", color = Color.White, modifier = Modifier.padding(8.dp))
        }
    }
}

@Composable
fun TrackingStep(title: String, subtitle: String, icon: ImageVector, isCompleted: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isCompleted) GreenAccent else Color.Gray,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                color = if (isCompleted) BrownDark else Color.Gray
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextMid
            )
        }
    }
}

@Composable
fun TrackingDivider() {
    Box(
        modifier = Modifier
            .padding(start = 13.dp)
            .height(24.dp)
            .width(2.dp)
            .background(Color.LightGray)
    )
}
