package com.cletaeats.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cletaeats.network.RestauranteItem
import com.cletaeats.ui.components.CategoryItem
import com.cletaeats.ui.components.RestaurantGridItem
import com.cletaeats.ui.theme.Cream
import com.cletaeats.ui.theme.CreamDark

@Composable
fun ClienteInicioTab(
    restaurantes: List<RestauranteItem>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedCategory: String?,
    onCategorySelect: (String?) -> Unit,
    onRestaurantSelect: (RestauranteItem) -> Unit
) {
    val categorias = listOf(
        "Pizza" to "🍕", "Burger" to "🍔", "Pasta" to "🍝",
        "Ensalada" to "🥗", "Sushi" to "🍣", "Café" to "☕",
        "Postres" to "🍰", "Tacos" to "🌮", "Pollo" to "🍗",
        "China" to "🥡", "Mariscos" to "🍤", "Bebidas" to "🥤"
    )

    Column(Modifier.fillMaxSize().background(Cream)) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            placeholder = { Text("¿Qué se te antoja hoy?") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )

        LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                Text("Categorías", Modifier.padding(horizontal = 16.dp), fontWeight = FontWeight.Bold)
                LazyRow(Modifier.padding(vertical = 8.dp, horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(categorias) { (nombre, icono) ->
                        CategoryItem(nombre, icono, selectedCategory == nombre) {
                            onCategorySelect(if (selectedCategory == nombre) null else nombre)
                        }
                    }
                }
            }

            item {
                val filtered = restaurantes.filter {
                    (selectedCategory == null || it.tipoComida?.contains(selectedCategory, true) == true) &&
                            it.nombre.contains(searchQuery, true)
                }
                Column(Modifier.padding(horizontal = 16.dp)) {
                    filtered.chunked(2).forEach { fila ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), Arrangement.spacedBy(12.dp)) {
                            fila.forEach { rest ->
                                RestaurantGridItem(rest, Modifier.weight(1f)) {
                                    onRestaurantSelect(rest)
                                }
                            }
                            if (fila.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}
