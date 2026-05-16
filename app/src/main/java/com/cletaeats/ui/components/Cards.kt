package com.cletaeats.ui.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cletaeats.network.ComboItem
import com.cletaeats.network.PedidoItem
import com.cletaeats.network.RestauranteItem
import com.cletaeats.ui.theme.*

@Composable
fun RestaurantGridItem(rest: RestauranteItem, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier.aspectRatio(1f).clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = WhiteCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("🏪", fontSize = 22.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(rest.nombre, fontWeight = FontWeight.ExtraBold, color = BrownDark, maxLines = 1)
            Text(rest.direccion, style = MaterialTheme.typography.bodySmall, color = TextMid, maxLines = 2)
        }
    }
}

@Composable
fun OrderCard(pedido: PedidoItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = WhiteCard),
        border = BorderStroke(1.dp, CreamDark)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("📦", fontSize = 24.sp)
            Column(modifier = Modifier.weight(1f).padding(start = 16.dp)) {
                Text("Pedido #${pedido.id}", fontWeight = FontWeight.Bold)
                Text(pedido.restauranteNombre ?: "Restaurante", style = MaterialTheme.typography.bodySmall)
            }
            Text("₡${pedido.total ?: 0.0}", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun CategoryItem(name: String, icon: String, isSelected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.width(85.dp).aspectRatio(0.85f).clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = if (isSelected) BrownDark else WhiteCard)
    ) {
        Column(Modifier.fillMaxSize(), Arrangement.Center, Alignment.CenterHorizontally) {
            Text(icon, fontSize = 28.sp)
            Text(name, color = if (isSelected) Color.White else TextDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}

@Composable
fun ComboCard(combo: ComboItem, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth().height(130.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = WhiteCard),
        border = BorderStroke(1.dp, CreamDark)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Combo #${combo.numeroCombo}", color = BrownMid, style = MaterialTheme.typography.labelSmall)
            Text(combo.nombre, fontWeight = FontWeight.Bold, maxLines = 2, modifier = Modifier.weight(1f))
            Text("₡${combo.precio}", fontWeight = FontWeight.ExtraBold, color = OrangeSoft)
        }
    }
}