package com.cletaeats.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Icecream
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.LocalPizza
import androidx.compose.material.icons.filled.RamenDining
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SetMeal
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cletaeats.network.RestauranteItem
import com.cletaeats.ui.components.CategoryItem
import com.cletaeats.ui.components.RestaurantGridItem
import com.cletaeats.ui.theme.Cream

@Composable
fun ClienteInicioTab(
    restaurantes: List<RestauranteItem>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedCategory: String?,
    onCategorySelect: (String?) -> Unit,
    onRestaurantSelect: (RestauranteItem) -> Unit
) {

    val categorias: List<Pair<String, ImageVector>> = listOf(

        "Pizza" to Icons.Default.LocalPizza,

        "Burger" to Icons.Default.Fastfood,

        "Pasta" to Icons.Default.RamenDining,

        "Ensalada" to Icons.Default.LocalDining,

        "Sushi" to Icons.Default.SetMeal,

        "Café" to Icons.Default.Coffee,

        "Postres" to Icons.Default.Icecream,

        "Tacos" to Icons.Default.Fastfood,

        "Pollo" to Icons.Default.SetMeal,

        "China" to Icons.Default.RamenDining,

        "Mariscos" to Icons.Default.SetMeal,

        "Bebidas" to Icons.Default.LocalDrink
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
    ) {

        OutlinedTextField(
            value = searchQuery,

            onValueChange = onSearchQueryChange,

            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),

            placeholder = {
                Text("¿Qué se te antoja hoy?")
            },

            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Buscar"
                )
            },

            shape = RoundedCornerShape(12.dp),

            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),

            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            item {

                Text(
                    text = "Categorías",

                    modifier = Modifier.padding(horizontal = 16.dp),

                    fontWeight = FontWeight.Bold,

                    style = MaterialTheme.typography.titleMedium
                )

                LazyRow(
                    modifier = Modifier.padding(
                        vertical = 8.dp,
                        horizontal = 16.dp
                    ),

                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    items(categorias) { (nombre, icono) ->

                        CategoryItem(
                            name = nombre,
                            icon = icono,
                            isSelected = selectedCategory == nombre
                        ) {

                            onCategorySelect(
                                if (selectedCategory == nombre) {
                                    null
                                } else {
                                    nombre
                                }
                            )
                        }
                    }
                }
            }

            item {

                val filtered = restaurantes.filter {

                    (selectedCategory == null ||
                            it.tipoComida?.contains(
                                selectedCategory,
                                ignoreCase = true
                            ) == true)

                            &&

                            it.nombre.contains(
                                searchQuery,
                                ignoreCase = true
                            )
                }

                Column(
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {

                    filtered.chunked(2).forEach { fila ->

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),

                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {

                            fila.forEach { rest ->

                                RestaurantGridItem(
                                    rest = rest,

                                    modifier = Modifier.weight(1f)
                                ) {

                                    onRestaurantSelect(rest)
                                }
                            }

                            if (fila.size == 1) {

                                Spacer(
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            item {

                Spacer(
                    modifier = Modifier.height(80.dp)
                )
            }
        }
    }
}