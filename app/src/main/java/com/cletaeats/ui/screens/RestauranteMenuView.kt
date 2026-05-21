package com.cletaeats.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cletaeats.network.ComboItem
import com.cletaeats.network.RestauranteItem
import com.cletaeats.ui.components.ComboCard
import com.cletaeats.ui.theme.*

@Composable
fun RestaurantMenuView(
    restaurante: RestauranteItem,
    combos: List<ComboItem>,
    isLoading: Boolean,
    cart: Map<ComboItem, Int>,
    onCartChange: (Map<ComboItem, Int>) -> Unit,
    onBack: () -> Unit,
    onProceedToCart: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().background(Cream)) {
        Surface(modifier = Modifier.fillMaxWidth(), color = BrownDark, shadowElevation = 8.dp) {
            Column(modifier = Modifier.padding(16.dp)) {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Volver", tint = Color.White) }
                Text(restaurante.nombre, style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        if (isLoading) {
            Box(Modifier.weight(1f).fillMaxWidth(), Alignment.Center) { CircularProgressIndicator(color = BrownDark) }
        } else {
            LazyColumn(Modifier.weight(1f).fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(combos) { combo ->
                    val currentQty = cart[combo] ?: 0
                    ComboCard(
                        combo = combo,
                        cantidadActual = currentQty,
                        onIncrement = {
                            val newCart = cart.toMutableMap()
                            newCart[combo] = currentQty + 1
                            onCartChange(newCart)
                        },
                        onDecrement = {
                            if (currentQty > 0) {
                                val newCart = cart.toMutableMap()
                                if (currentQty == 1) newCart.remove(combo) else newCart[combo] = currentQty - 1
                                onCartChange(newCart)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        if (cart.isNotEmpty()) {
            val totalItems = cart.values.sum()
            val totalCost = cart.entries.sumOf { it.key.precio * it.value }
            Surface(
                modifier = Modifier.fillMaxWidth().clickable { onProceedToCart() },
                color = OrangeSoft,
                shadowElevation = 16.dp
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Ver Carrito", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text("$totalItems ítems", color = Color.White, style = MaterialTheme.typography.bodySmall)
                    }
                    Text("₡$totalCost", color = Color.White, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleLarge)
                }
            }
        }
    }
}
