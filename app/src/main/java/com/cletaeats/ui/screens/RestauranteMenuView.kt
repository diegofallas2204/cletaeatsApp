package com.cletaeats.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.shape.RoundedCornerShape
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
    var addedCombo by remember { mutableStateOf<ComboItem?>(null) }

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
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.weight(1f).fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(combos) { combo ->
                    val currentQty = cart[combo] ?: 0
                    ComboCard(
                        combo = combo,
                        onAddClick = {
                            val newCart = cart.toMutableMap()
                            newCart[combo] = currentQty + 1
                            onCartChange(newCart)
                            addedCombo = combo
                        }
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

    if (addedCombo != null) {
        Dialog(onDismissRequest = { addedCombo = null }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = WhiteCard),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("¡Agregado al carrito!", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = BrownDark)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(addedCombo!!.nombre, style = MaterialTheme.typography.bodyLarge)
                    Text("₡${addedCombo!!.precio}", fontWeight = FontWeight.ExtraBold, color = OrangeSoft)
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { addedCombo = null },
                        colors = ButtonDefaults.buttonColors(containerColor = CreamDark, contentColor = BrownDark),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Seguir comprando")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            addedCombo = null
                            onProceedToCart()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrownDark, contentColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Ver el carrito")
                    }
                }
            }
        }
    }
}
