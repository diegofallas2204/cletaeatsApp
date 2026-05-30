package com.cletaeats.ui.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cletaeats.network.ComboItem
import com.cletaeats.network.PedidoItem
import com.cletaeats.network.RestauranteItem
import com.cletaeats.ui.theme.*

private fun displayEstado(estado: String?): String = when ((estado ?: "preparacion").lowercase()) {
    "preparacion" -> "En Preparación"
    "aceptado"    -> "Aceptado"
    "camino", "en_camino", "en camino" -> "En Camino"
    "entregado"   -> "Entregado"
    "suspendido", "cancelado" -> "Cancelado"
    else          -> (estado ?: "pendiente").replaceFirstChar { it.uppercase() }
}

private fun colorEstado(estado: String?): Color = when ((estado ?: "preparacion").lowercase()) {
    "entregado"   -> GreenAccent
    "suspendido", "cancelado" -> Color.Red
    "camino", "en_camino", "en camino" -> OrangeSoft
    "aceptado", "preparacion" -> BrownMid
    else          -> Color.Gray
}

fun getCategoryIcon(tipoComida: String?): ImageVector {

    if (tipoComida == null) return Icons.Default.Fastfood

    val lower = tipoComida.lowercase()

    return when {

        lower.contains("pizza") ->
            Icons.Default.LocalPizza

        lower.contains("burger") ||
                lower.contains("hamburguesa") ->
            Icons.Default.Fastfood

        lower.contains("pasta") ||
                lower.contains("tallarines") ||
                lower.contains("italiana") ->
            Icons.Default.RamenDining

        lower.contains("ensalada") ||
                lower.contains("saludable") ->
            Icons.Default.LocalDining

        lower.contains("sushi") ||
                lower.contains("japonesa") ->
            Icons.Default.SetMeal

        lower.contains("café") ||
                lower.contains("cafe") ||
                lower.contains("coffee") ->
            Icons.Default.Coffee

        lower.contains("postre") ||
                lower.contains("repostería") ||
                lower.contains("reposteria") ||
                lower.contains("cake") ||
                lower.contains("helado") ||
                lower.contains("dulce") ->
            Icons.Default.Icecream

        lower.contains("taco") ||
                lower.contains("mexicana") ->
            Icons.Default.Fastfood

        lower.contains("pollo") ||
                lower.contains("chicken") ->
            Icons.Default.SetMeal

        lower.contains("china") ||
                lower.contains("asiática") ||
                lower.contains("asiatica") ||
                lower.contains("cantones") ->
            Icons.Default.RamenDining

        lower.contains("marisco") ||
                lower.contains("pescado") ||
                lower.contains("ceviche") ->
            Icons.Default.SetMeal

        lower.contains("bebida") ||
                lower.contains("refresco") ||
                lower.contains("jugo") ||
                lower.contains("batido") ->
            Icons.Default.LocalDrink

        else ->
            Icons.Default.Fastfood
    }
}

@Composable
fun RestaurantGridItem(
    rest: RestauranteItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {

    Card(
        modifier = modifier
            .aspectRatio(1f)
            .clickable { onClick() },

        shape = RoundedCornerShape(24.dp),

        colors = CardDefaults.cardColors(
            containerColor = WhiteCard
        ),

        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        )
    ) {

        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),

            verticalArrangement = Arrangement.SpaceBetween
        ) {

            Column {

                Icon(
                    imageVector = getCategoryIcon(rest.tipoComida),
                    contentDescription = rest.tipoComida,
                    modifier = Modifier.size(28.dp),
                    tint = BrownDark
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = rest.nombre,
                    fontWeight = FontWeight.ExtraBold,
                    color = BrownDark,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 14.sp,
                    lineHeight = 16.sp
                )
            }

            Text(
                text = rest.direccion,
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
    onCancelClick: () -> Unit = {},
    onRateClick: (() -> Unit)? = null
) {
    val estado = (pedido.estado ?: "preparacion").lowercase()
    val esEntregado = estado == "entregado"
    val esCancelado = estado == "suspendido" || estado == "cancelado"
    val esCancelable = !esEntregado && !esCancelado

    Card(
        modifier = Modifier.fillMaxWidth().clickable { if (!esEntregado) onTrackClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = WhiteCard),
        border = BorderStroke(1.dp, CreamDark)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (esEntregado) Icons.Default.CheckCircle else Icons.Default.ShoppingCart,
                    contentDescription = "Pedido",
                    tint = if (esEntregado) GreenAccent else BrownDark,
                    modifier = Modifier.size(28.dp)
                )
                Column(modifier = Modifier.weight(1f).padding(start = 16.dp)) {
                    Text("Pedido #${pedido.id}", fontWeight = FontWeight.Bold, color = BrownDark)
                    Text(pedido.restauranteNombre ?: "Restaurante", style = MaterialTheme.typography.bodySmall, color = TextMid)
                    Text(
                        text = displayEstado(pedido.estado),
                        color = colorEstado(pedido.estado),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("₡${pedido.total ?: 0.0}", fontWeight = FontWeight.Bold, color = BrownDark)
                    if (esCancelable) {
                        Spacer(Modifier.height(4.dp))
                        IconButton(onClick = onCancelClick, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, "Cancelar", tint = Color.Red, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // Botón de valorar solo para pedidos entregados y si no fue valorado aún
            if (esEntregado && onRateClick != null) {
                Spacer(Modifier.height(10.dp))
                OutlinedButton(
                    onClick = onRateClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, OrangeSoft)
                ) {
                    Icon(Icons.Default.Star, null, tint = OrangeSoft, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Valorar pedido", color = OrangeSoft, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun CategoryItem(
    name: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {

    Card(
        modifier = Modifier
            .width(85.dp)
            .aspectRatio(0.85f)
            .clickable { onClick() },

        shape = RoundedCornerShape(24.dp),

        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                BrownDark
            } else {
                WhiteCard
            }
        )
    ) {

        Column(
            modifier = Modifier.fillMaxSize(),

            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Icon(
                imageVector = icon,
                contentDescription = name,
                modifier = Modifier.size(28.dp),
                tint = if (isSelected) Color.White else BrownDark
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = name,
                color = if (isSelected) Color.White else TextDark,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
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
        modifier = modifier
            .fillMaxWidth()
            .height(150.dp),

        shape = RoundedCornerShape(16.dp),

        colors = CardDefaults.cardColors(
            containerColor = WhiteCard
        ),

        border = BorderStroke(1.dp, CreamDark)
    ) {

        Box(modifier = Modifier.fillMaxSize()) {

            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxHeight(),

                verticalArrangement = Arrangement.SpaceBetween
            ) {

                Column {

                    Text(
                        text = "Combo #${combo.numeroCombo}",
                        color = BrownMid,
                        style = MaterialTheme.typography.labelSmall
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = combo.nombre.ifBlank { "Combo" },
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 14.sp,
                        lineHeight = 16.sp,
                        color = BrownDark,
                        modifier = Modifier.padding(end = 24.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "₡${combo.precio}",
                    fontWeight = FontWeight.ExtraBold,
                    color = OrangeSoft,
                    fontSize = 14.sp
                )
            }

            IconButton(
                onClick = onAddClick,

                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .size(36.dp)
                    .background(
                        BrownDark,
                        RoundedCornerShape(12.dp)
                    )
            ) {

                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Agregar",
                    tint = Color.White
                )
            }
        }
    }
}