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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cletaeats.network.ComboItem
import com.cletaeats.network.PedidoItem
import com.cletaeats.network.RestauranteItem
import com.cletaeats.ui.theme.*

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.LocalPizza
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.RamenDining
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Icecream
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SetMeal
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.LocalDrink

fun getCategoryIcon(tipoComida: String?): androidx.compose.ui.graphics.vector.ImageVector {
    if (tipoComida == null) return androidx.compose.material.icons.filled.ShoppingCart
    val lower = tipoComida.lowercase()
    if (lower.contains("pizza")) return androidx.compose.material.icons.filled.ShoppingCart
    if (lower.contains("burger") || lower.contains("hamburguesa")) return androidx.compose.material.icons.filled.ShoppingCart
    if (lower.contains("pasta") || lower.contains("tallarines") || lower.contains("italiana")) return androidx.compose.material.icons.filled.ShoppingCart
    if (lower.contains("ensalada") || lower.contains("saludable")) return androidx.compose.material.icons.filled.ShoppingCart
    if (lower.contains("sushi") || lower.contains("japonesa")) return androidx.compose.material.icons.filled.ShoppingCart
    if (lower.contains("café") || lower.contains("cafe") || lower.contains("coffee") || lower.contains("coffe")) return androidx.compose.material.icons.filled.ShoppingCart
    if (lower.contains("postre") || lower.contains("repostería") || lower.contains("reposteria") || lower.contains("cake") || lower.contains("helado") || lower.contains("dulce")) return androidx.compose.material.icons.filled.ShoppingCart
    if (lower.contains("taco") || lower.contains("mexicana")) return androidx.compose.material.icons.filled.ShoppingCart
    if (lower.contains("pollo") || lower.contains("chicken")) return androidx.compose.material.icons.filled.ShoppingCart
    if (lower.contains("china") || lower.contains("asiática") || lower.contains("asiatica") || lower.contains("cantones")) return androidx.compose.material.icons.filled.ShoppingCart
    if (lower.contains("marisco") || lower.contains("pescado") || lower.contains("ceviche")) return androidx.compose.material.icons.filled.ShoppingCart
    if (lower.contains("bebida") || lower.contains("refresco") || lower.contains("jugo") || lower.contains("batido")) return androidx.compose.material.icons.filled.ShoppingCart
    return androidx.compose.material.icons.filled.ShoppingCart
}

@Composable
fun RestaurantGridItem(rest: RestauranteItem, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier.aspectRatio(1f).clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = WhiteCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Icon(getCategoryIcon(rest.tipoComida), contentDescription = rest.tipoComida, modifier = Modifier.size(28.dp), tint = BrownDark)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    rest.nombre,
                    fontWeight = FontWeight.ExtraBold,
                    color = BrownDark,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 14.sp,
                    lineHeight = 16.sp
                )
            }
            Text(
                rest.direccion,
                style = MaterialTheme.typography.bodySmall,
                color = TextMid,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun OrderCard(
    pedido: PedidoItem,
    onTrackClick: () -> Unit = {},
    onCancelClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onTrackClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = WhiteCard),
        border = BorderStroke(1.dp, CreamDark)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("📦", fontSize = 24.sp)
            Column(modifier = Modifier.weight(1f).padding(start = 16.dp)) {
                Text("Pedido #${pedido.id}", fontWeight = FontWeight.Bold, color = BrownDark)
                Text(pedido.restauranteNombre ?: "Restaurante", style = MaterialTheme.typography.bodySmall, color = TextMid)
                
                val rawStatus = pedido.estado ?: "pendiente"
                val status = if (rawStatus == "suspendido") "cancelado" else rawStatus
                val statusColor = when (status) {
                    "entregado" -> GreenAccent
                    "cancelado" -> Color.Red
                    "camino" -> OrangeSoft
                    "preparando" -> BrownMid
                    else -> Color.Gray
                }
                Text(
                    text = "Estado: ${status.uppercase()}",
                    color = statusColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                Text("₡${pedido.total ?: 0.0}", fontWeight = FontWeight.Bold, color = BrownDark)
                
                val rawStatus2 = pedido.estado ?: "pendiente"
                val status = if (rawStatus2 == "suspendido") "cancelado" else rawStatus2
                val isCancelable = status != "entregado" && status != "cancelado"
                if (isCancelable) {
                    Spacer(modifier = Modifier.height(4.dp))
                    IconButton(
                        onClick = { onCancelClick() },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancelar Pedido",
                            tint = Color.Red,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryItem(name: String, icon: androidx.compose.ui.graphics.vector.ImageVector, isSelected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.width(85.dp).aspectRatio(0.85f).clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = if (isSelected) BrownDark else WhiteCard)
    ) {
        Column(Modifier.fillMaxSize(), Arrangement.Center, Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = name, modifier = Modifier.size(28.dp), tint = if (isSelected) Color.White else BrownDark)
            Text(name, color = if (isSelected) Color.White else TextDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}

@Composable
fun ComboCard(
    combo: ComboItem,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth().height(150.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = WhiteCard),
        border = BorderStroke(1.dp, CreamDark)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.padding(12.dp).fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Combo #${combo.numeroCombo}", color = BrownMid, style = MaterialTheme.typography.labelSmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = combo.nombre.ifBlank { "Combo" },
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 14.sp,
                        lineHeight = 16.sp,
                        color = BrownDark,
                        modifier = Modifier.padding(end = 24.dp) // Leave space for the + button
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("₡${combo.precio}", fontWeight = FontWeight.ExtraBold, color = OrangeSoft, fontSize = 14.sp)
            }
            
            // Floating "+" Add button in the bottom right
            IconButton(
                onClick = onAddClick,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .size(36.dp)
                    .background(BrownDark, RoundedCornerShape(12.dp))
            ) {
                Text("+", fontWeight = FontWeight.Bold, color = androidx.compose.ui.graphics.Color.White, fontSize = 20.sp)
            }
        }
    }
}
