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
    onBack: () -> Unit,
    onComboSelected: (ComboItem) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().background(Cream)) {
        Surface(modifier = Modifier.fillMaxWidth(), color = BrownDark, shadowElevation = 8.dp) {
            Column(modifier = Modifier.padding(16.dp)) {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Volver", tint = Color.White) }
                Text(restaurante.nombre, style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        if (isLoading) {
            Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator(color = BrownDark) }
        } else {
            LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(combos.chunked(2)) { fila ->
                    Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(16.dp)) {
                        fila.forEach { combo ->
                            ComboCard(combo, Modifier.weight(1f).clickable { onComboSelected(combo) })
                        }
                    }
                }
            }
        }
    }
}