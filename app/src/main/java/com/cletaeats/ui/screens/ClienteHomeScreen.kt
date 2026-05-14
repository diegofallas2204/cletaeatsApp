package com.cletaeats.ui.screens

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cletaeats.network.CletaApi
import com.cletaeats.network.PedidoItem
import com.cletaeats.network.RestauranteItem
import com.cletaeats.network.ComboItem
import com.cletaeats.network.MetodoPago
import com.cletaeats.network.TokenManager
import com.cletaeats.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClienteHomeScreen(onLogout: () -> Unit) {
    val userRole = TokenManager.rol ?: "cliente"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🍜 CLETAEATS", fontWeight = FontWeight.Bold, color = Color.White) },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Logout",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BrownDark)
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when (userRole.lowercase()) {
                "admin" -> AdminContent()
                "repartidor" -> RepartidorContent()
                else -> ClienteContent() 
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ClienteContent() {
    var restaurantes by remember { mutableStateOf<List<RestauranteItem>>(emptyList()) }
    var historial by remember { mutableStateOf<List<PedidoItem>>(emptyList()) }
    var menuCombos by remember { mutableStateOf<List<ComboItem>>(emptyList()) }
    
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var selectedRestaurant by remember { mutableStateOf<RestauranteItem?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var isMenuLoading by remember { mutableStateOf(false) }

    // Checkout States
    var selectedCombo by remember { mutableStateOf<ComboItem?>(null) }
    var showCheckoutDialog by remember { mutableStateOf(false) }
    var showPaymentDialog by remember { mutableStateOf(false) }
    var showOrderTracking by remember { mutableStateOf(false) }
    var extraNotes by remember { mutableStateOf("") }
    var isAgrandado by remember { mutableStateOf(false) }
    var isSubmittingOrder by remember { mutableStateOf(false) }
    var tarjetasGuardadas by remember { mutableStateOf<List<MetodoPago>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()

    val categorias = listOf(
        "Pizza" to "🍕", "Burger" to "🍔", "Pasta" to "🍝",
        "Ensalada" to "🥗", "Sushi" to "🍣", "Café" to "☕",
        "Postres" to "🍰", "Tacos" to "🌮", "Pollo" to "🍗",
        "China" to "🥡", "Mariscos" to "🍤", "Bebidas" to "🥤"
    )

    LaunchedEffect(Unit) {
        try {
            val token = TokenManager.token
            if (token == null) {
                Log.e("CletaEats", "Token is null, cannot fetch data")
                return@LaunchedEffect
            }
            
        val fullToken = "Bearer $token"

        // 1. Cargar Restaurantes (Público)
        try {
            val restResponse = CletaApi.retrofitService.getRestaurantes()
            if (restResponse.success) {
                restaurantes = restResponse.data ?: emptyList()
            } else {
                Log.e("CletaEats", "Error de API Restaurantes: ${restResponse.error}")
            }
        } catch (e: Exception) {
            Log.e("CletaEats", "Fallo al cargar restaurantes: ${e.message}")
        }

        // 2. Cargar Historial (Privado)
        try {
            val histResponse = CletaApi.retrofitService.getClienteHistorial(fullToken)
            if (histResponse.success) {
                historial = histResponse.data ?: emptyList()
            } else {
                Log.e("CletaEats", "Error de API Historial: ${histResponse.error}")
            }
        } catch (e: Exception) {
            Log.e("CletaEats", "Fallo al cargar historial (posible 401): ${e.message}")
        }

        // 3. Cargar Tarjetas (Privado)
        try {
            val tarjetasResp = CletaApi.retrofitService.getTarjetas(fullToken)
            if (tarjetasResp.success) {
                tarjetasGuardadas = tarjetasResp.data ?: emptyList()
            }
        } catch (e: Exception) {
            Log.e("CletaEats", "Fallo al cargar tarjetas: ${e.message}")
        }
        } finally {
            isLoading = false
        }
    }

    // Cargar combos cuando se selecciona un restaurante
    LaunchedEffect(selectedRestaurant) {
        if (selectedRestaurant != null) {
            isMenuLoading = true
            try {
                val token = TokenManager.token ?: ""
                val response = CletaApi.retrofitService.getCombosByRestaurant(token, selectedRestaurant!!.id)
                menuCombos = response.data ?: emptyList()
            } catch (e: Exception) {
                Log.e("CletaEats", "Error cargando menú: ${e.message}")
            } finally {
                isMenuLoading = false
            }
        }
    }

    if (showOrderTracking) {
        OrderTrackingScreen(onBack = { 
            showOrderTracking = false
            selectedRestaurant = null
            // Recargar historial al volver a la pantalla principal
            coroutineScope.launch {
                val token = TokenManager.token
                if (token != null) {
                    try {
                        val histResponse = CletaApi.retrofitService.getClienteHistorial("Bearer $token")
                        if (histResponse.success) {
                            historial = histResponse.data ?: emptyList()
                        }
                    } catch (e: Exception) {
                        Log.e("CletaEats", "Fallo al recargar historial", e)
                    }
                }
            }
        })
    } else if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = BrownDark)
        }
    } else if (selectedRestaurant != null) {
        // --- VISTA DE MENÚ DEL RESTAURANTE ---
        RestaurantMenuView(
            restaurante = selectedRestaurant!!,
            combos = menuCombos,
            isLoading = isMenuLoading,
            onBack = { selectedRestaurant = null },
            onComboSelected = { combo ->
                selectedCombo = combo
                showCheckoutDialog = true
            }
        )
    } else {
        // --- VISTA PRINCIPAL (GRID CATEGORÍAS + FILTRO/HISTORIAL) ---
        Column(modifier = Modifier.fillMaxSize().background(Cream)) {
            // Buscador
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                placeholder = { Text("¿Qué se te antoja hoy?") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrownDark,
                    unfocusedBorderColor = BrownLight,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. GRID DE CATEGORÍAS (Siempre visible arriba)
                item {
                    Text(
                        if (selectedCategory == null) "Explorar Categorías" else "Filtrando por: $selectedCategory",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 16.dp),
                        fontWeight = FontWeight.Bold,
                        color = BrownDark
                    )
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        categorias.forEach { (nombre, icono) ->
                            val isSelected = selectedCategory == nombre
                            CategoryItem(
                                nombre, icono, 
                                isSelected = isSelected,
                                modifier = Modifier.width(85.dp),
                                onClick = {
                                    selectedCategory = if (isSelected) null else nombre
                                }
                            )
                        }
                    }
                }

                // 2. RESTAURANTES
                val filteredRestaurantes = restaurantes.filter { 
                    val matchCat = selectedCategory == null || it.tipoComida?.contains(selectedCategory!!, true) == true
                    val matchSearch = it.nombre.contains(searchQuery, true)
                    matchCat && matchSearch
                }

                stickyHeader {
                    SectionHeader(
                        title = if (selectedCategory == null && searchQuery.isEmpty()) "Restaurantes Recomendados" else "Resultados de Búsqueda",
                        onClear = if (selectedCategory != null || searchQuery.isNotEmpty()) { { 
                            selectedCategory = null 
                            searchQuery = ""
                        } } else null
                    )
                }

                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        filteredRestaurantes.chunked(2).forEach { fila ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                fila.forEach { rest ->
                                    RestaurantGridItem(rest, modifier = Modifier.weight(1f)) {
                                        selectedRestaurant = rest
                                    }
                                }
                                if (fila.size == 1) Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                        if (filteredRestaurantes.isEmpty()) {
                            Text(
                                "No hay restaurantes disponibles en esta sección.",
                                modifier = Modifier.fillMaxWidth().padding(32.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                color = Color.Gray
                            )
                        }
                    }
                }

                // 3. HISTORIAL DE PEDIDOS
                if (selectedCategory == null && searchQuery.isEmpty()) {
                    stickyHeader {
                        SectionHeader("Tu Historial Reciente", icon = Icons.Default.History)
                    }

                    items(historial) { pedido ->
                        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                            OrderCard(pedido)
                        }
                    }

                    if (historial.isEmpty()) {
                        item {
                            Text("Aún no tienes pedidos registrados.", modifier = Modifier.fillMaxWidth().padding(32.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center, color = Color.Gray)
                        }
                    }
                }
                
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }

    if (showCheckoutDialog && selectedCombo != null) {
        CheckoutDialog(
            combo = selectedCombo!!,
            notes = extraNotes,
            isAgrandado = isAgrandado,
            onNotesChange = { extraNotes = it },
            onAgrandadoChange = { isAgrandado = it },
            onDismiss = { showCheckoutDialog = false },
            onConfirm = {
                showCheckoutDialog = false
                showPaymentDialog = true
            }
        )
    }

    if (showPaymentDialog && selectedCombo != null && selectedRestaurant != null) {
        PaymentDialog(
            isSubmitting = isSubmittingOrder,
            tarjetas = tarjetasGuardadas,
            onDismiss = { showPaymentDialog = false },
            onSaveCard = { nuevaTarjeta ->
                coroutineScope.launch {
                    try {
                        val token = TokenManager.token
                        if (token != null) {
                            val resp = CletaApi.retrofitService.guardarTarjeta("Bearer $token", nuevaTarjeta)
                            if (resp.success && resp.data != null) {
                                tarjetasGuardadas = tarjetasGuardadas + resp.data
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("CletaEats", "Error al guardar tarjeta", e)
                    }
                }
            },
            onConfirm = { paymentMethod ->
                coroutineScope.launch {
                    isSubmittingOrder = true
                    try {
                        val token = TokenManager.token
                        if (token != null) {
                            val request = com.cletaeats.network.CreateOrderPayload(
                                pedido = com.cletaeats.network.OrderRequest(
                                    restauranteId = selectedRestaurant!!.id,
                                    detalles = listOf(
                                        com.cletaeats.network.OrderItem(
                                            comboId = selectedCombo!!.id,
                                            cantidad = 1,
                                            agrandado = isAgrandado,
                                            notas = "$paymentMethod - $extraNotes"
                                        )
                                    )
                                )
                            )
                            val resp = CletaApi.retrofitService.createOrder("Bearer $token", request)
                            if (resp.success) {
                                showPaymentDialog = false
                                showOrderTracking = true
                                extraNotes = ""
                                isAgrandado = false
                            } else {
                                Log.e("CletaEats", "Error al crear pedido: ${resp.error}")
                                showPaymentDialog = false
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("CletaEats", "Fallo al crear pedido", e)
                        showPaymentDialog = false
                    } finally {
                        isSubmittingOrder = false
                    }
                }
            }
        )
    }
}

@Composable
fun RestaurantMenuView(
    restaurante: RestauranteItem,
    combos: List<ComboItem>,
    isLoading: Boolean,
    onBack: () -> Unit,
    onComboSelected: (ComboItem) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().background(Cream)) {
        // Cabecera del Restaurante
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = BrownDark,
            shadowElevation = 8.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                IconButton(onClick = onBack) {
                    Icon(androidx.compose.material.icons.Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
                }
                Text(restaurante.nombre, style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.Bold)
                Text(restaurante.tipoComida ?: "Restaurante", color = Cream, style = MaterialTheme.typography.bodyMedium)
            }
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BrownDark)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text("Nuestro Menú", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = BrownDark)
                }

                // Grid de Combos (2 columnas)
                items(combos.chunked(2)) { fila ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        fila.forEach { combo ->
                            ComboCard(
                                combo = combo,
                                modifier = Modifier.weight(1f).clickable { onComboSelected(combo) }
                            )
                        }
                        if (fila.size == 1) Spacer(modifier = Modifier.weight(1f))
                    }
                }

                if (combos.isEmpty()) {
                    item {
                        Text("Este restaurante no tiene combos disponibles por ahora.", color = Color.Gray, modifier = Modifier.padding(32.dp))
                    }
                }
                
                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector? = null, onClear: (() -> Unit)? = null) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = CreamDark,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = BrownDark)
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = BrownDark, modifier = Modifier.weight(1f))
            if (onClear != null) {
                TextButton(onClick = onClear) {
                    Text("Limpiar", color = BrownMid)
                }
            }
        }
    }
}

@Composable
fun CategoryItem(name: String, icon: String, isSelected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier
            .aspectRatio(0.85f)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) BrownDark else WhiteCard
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 10.dp else 4.dp),
        border = if (isSelected) null else BorderStroke(1.5.dp, CreamDark.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(50.dp),
                shape = RoundedCornerShape(16.dp),
                color = if (isSelected) Color.White.copy(alpha = 0.2f) else Cream.copy(alpha = 0.3f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(icon, fontSize = 28.sp)
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                name, 
                style = MaterialTheme.typography.labelMedium, 
                color = if (isSelected) Color.White else TextDark,
                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}

@Composable
fun RestaurantGridItem(rest: RestauranteItem, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier
            .aspectRatio(1f)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = WhiteCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        border = BorderStroke(1.dp, CreamDark.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(OrangeSoft.copy(alpha = 0.15f), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🏪", fontSize = 22.sp)
                }
                
                Surface(
                    color = BrownLight.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        rest.tipoComida ?: "Varios", 
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = BrownMid, 
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                rest.nombre, 
                style = MaterialTheme.typography.titleMedium, 
                fontWeight = FontWeight.ExtraBold, 
                maxLines = 1,
                color = BrownDark
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                rest.direccion, 
                style = MaterialTheme.typography.bodySmall, 
                color = TextMid, 
                maxLines = 2,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
fun RepartidorContent() {
    var disponibles by remember { mutableStateOf<List<PedidoItem>>(emptyList()) }
    var realizados by remember { mutableStateOf<List<PedidoItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            val token = TokenManager.token ?: ""
            val resp = CletaApi.retrofitService.getRepartidorPedidos(token)
            val allPedidos = resp.data ?: emptyList()
            disponibles = allPedidos.filter { it.estado == "preparado" || it.estado == "asignado" || it.estado == "pendiente" }
            realizados = allPedidos.filter { it.estado == "entregado" }
        } catch (e: Exception) {
            Log.e("CletaEats", "Error cargando datos de repartidor: ${e.message}", e)
        } finally {
            isLoading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BrownDark)
            }
        } else {
            Column {
                SectionTitle("Pedidos Pendientes")
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.height(140.dp)
                ) {
                    items(disponibles) { pedido ->
                        AvailableOrderCard(pedido)
                    }
                }

                SectionTitle("Entregas Realizadas")
            }

            LazyColumn(
                modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(realizados) { pedido ->
                    OrderCard(pedido)
                }
                if (realizados.isEmpty()) {
                    item { Text("Aún no has realizado entregas", color = Color.Gray, modifier = Modifier.padding(16.dp)) }
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
fun AdminContent() {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        SectionTitle("Panel de Control")
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { SummaryCard("Ventas", "₡450k", BrownMid) }
            item { SummaryCard("Restaurantes", "24", BrownDark) }
            item { SummaryCard("Clientes", "150", GreenAccent) }
        }
        
        SectionTitle("Acciones Rápidas")
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = WhiteCard)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Gestionar Usuarios", color = BrownDark, fontWeight = FontWeight.Bold)
                Text("Ver reportes de ventas", color = TextMid)
            }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(16.dp),
        fontWeight = FontWeight.ExtraBold
    )
}

@Composable
fun RestaurantCard(name: String, description: String) {
    Card(
        modifier = Modifier.size(width = 200.dp, height = 120.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = WhiteCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp).fillMaxSize(), verticalArrangement = Arrangement.Center) {
            Box(modifier = Modifier.size(32.dp).background(OrangeSoft, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                Text("🏪", fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(name, style = MaterialTheme.typography.titleMedium, maxLines = 1)
            Text(description, style = MaterialTheme.typography.bodySmall, color = TextMid, maxLines = 1)
        }
    }
}

@Composable
fun ComboCard(combo: ComboItem, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(130.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = WhiteCard),
        border = BorderStroke(1.dp, CreamDark)
    ) {
        Column(modifier = Modifier.padding(12.dp).fillMaxSize()) {
            Text("Combo #${combo.numeroCombo}", color = BrownMid, style = MaterialTheme.typography.labelSmall)
            Text(combo.nombre, style = MaterialTheme.typography.titleMedium, maxLines = 2, modifier = Modifier.weight(1f))
            Text("₡${combo.precio}", fontWeight = FontWeight.ExtraBold, color = OrangeSoft)
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
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Pedido #${pedido.id}", style = MaterialTheme.typography.titleMedium)
                Text(pedido.restauranteNombre ?: "Restaurante", style = MaterialTheme.typography.bodySmall)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("₡${pedido.total ?: 0.0}", fontWeight = FontWeight.Bold)
                Text(pedido.estado ?: "Pendiente", color = if(pedido.estado=="entregado") GreenAccent else OrangeSoft, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun AvailableOrderCard(pedido: PedidoItem) {
    Card(
        modifier = Modifier.size(width = 220.dp, height = 130.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BrownMid),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
            Text("NUEVO PEDIDO", color = Cream, style = MaterialTheme.typography.labelSmall)
            Text("#${pedido.id}", color = Color.White, style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(4.dp))
            Text(pedido.restauranteNombre ?: "Recoger ya", color = Cream, style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(8.dp))
            Text("₡${pedido.total ?: 0.0}", color = OrangeSoft, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
fun SummaryCard(title: String, value: String, color: Color) {
    Card(
        modifier = Modifier.size(width = 160.dp, height = 100.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = WhiteCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, style = MaterialTheme.typography.labelLarge, color = color)
            Text(value, style = MaterialTheme.typography.displayLarge.copy(fontSize = 24.sp), color = color)
        }
    }
}

@Composable
fun CheckoutDialog(
    combo: ComboItem,
    notes: String,
    isAgrandado: Boolean,
    onNotesChange: (String) -> Unit,
    onAgrandadoChange: (Boolean) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Detalles del Pedido",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = BrownDark
            )
        },
        text = {
            Column {
                Text(
                    text = combo.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "₡${if (isAgrandado) combo.precio + 1500 else combo.precio}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = OrangeSoft,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().clickable { onAgrandadoChange(!isAgrandado) }.padding(vertical = 8.dp)
                ) {
                    Checkbox(
                        checked = isAgrandado,
                        onCheckedChange = onAgrandadoChange,
                        colors = CheckboxDefaults.colors(checkedColor = BrownDark)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Agrandar combo (+₡1500)", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = onNotesChange,
                    label = { Text("Notas Extra") },
                    placeholder = { Text("Alergias, sin cebolla, etc.") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrownDark,
                        unfocusedBorderColor = BrownLight
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = BrownDark)
            ) {
                Text("Continuar", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = BrownMid)
            }
        },
        containerColor = Cream
    )
}

@Composable
fun PaymentDialog(
    isSubmitting: Boolean,
    tarjetas: List<MetodoPago>,
    onDismiss: () -> Unit,
    onSaveCard: (MetodoPago) -> Unit,
    onConfirm: (String) -> Unit
) {
    var selectedPayment by remember { mutableStateOf("Efectivo") }
    var showNewCardForm by remember { mutableStateOf(false) }
    
    // New Card state
    var numeroTarjeta by remember { mutableStateOf("") }
    var fechaVencimiento by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Método de Pago",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = BrownDark
            )
        },
        text = {
            Column {
                if (!showNewCardForm) {
                    Text("Elige cómo deseas pagar tu pedido:")
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Efectivo
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedPayment = "Efectivo" }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedPayment == "Efectivo",
                            onClick = { selectedPayment = "Efectivo" },
                            colors = RadioButtonDefaults.colors(selectedColor = BrownDark)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("💵 Efectivo")
                    }

                    // Tarjetas Guardadas
                    tarjetas.forEach { tarjeta ->
                        val last4 = if (tarjeta.numeroTarjeta.length >= 4) tarjeta.numeroTarjeta.takeLast(4) else "****"
                        val cardLabel = "💳 Tarjeta terminada en $last4"
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedPayment = cardLabel }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedPayment == cardLabel,
                                onClick = { selectedPayment = cardLabel },
                                colors = RadioButtonDefaults.colors(selectedColor = BrownDark)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(cardLabel)
                        }
                    }

                    // Add new card button
                    TextButton(onClick = { showNewCardForm = true }) {
                        Text("+ Agregar nueva tarjeta", color = BrownMid)
                    }

                } else {
                    Text("Agregar Nueva Tarjeta", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = numeroTarjeta,
                        onValueChange = { numeroTarjeta = it },
                        label = { Text("Número de Tarjeta") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row {
                        OutlinedTextField(
                            value = fechaVencimiento,
                            onValueChange = { fechaVencimiento = it },
                            label = { Text("MM/AA") },
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = cvv,
                            onValueChange = { cvv = it },
                            label = { Text("CVV") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            val newCard = MetodoPago(
                                numeroTarjeta = numeroTarjeta,
                                fechaVencimiento = fechaVencimiento,
                                cvv = cvv
                            )
                            onSaveCard(newCard)
                            showNewCardForm = false
                            val last4 = if (numeroTarjeta.length >= 4) numeroTarjeta.takeLast(4) else "****"
                            selectedPayment = "💳 Tarjeta terminada en $last4"
                            numeroTarjeta = ""
                            fechaVencimiento = ""
                            cvv = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrownDark),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Guardar Tarjeta", color = Color.White)
                    }
                    TextButton(onClick = { showNewCardForm = false }, modifier = Modifier.fillMaxWidth()) {
                        Text("Cancelar", color = BrownMid)
                    }
                }
                
                if (isSubmitting) {
                    Spacer(modifier = Modifier.height(16.dp))
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally), color = BrownDark)
                }
            }
        },
        confirmButton = {
            if (!showNewCardForm) {
                Button(
                    onClick = { onConfirm(selectedPayment) },
                    enabled = !isSubmitting,
                    colors = ButtonDefaults.buttonColors(containerColor = BrownDark)
                ) {
                    Text("Confirmar Pedido", color = Color.White)
                }
            }
        },
        dismissButton = {
            if (!showNewCardForm) {
                TextButton(onClick = onDismiss, enabled = !isSubmitting) {
                    Text("Cancelar", color = BrownMid)
                }
            }
        },
        containerColor = Cream
    )
}

@Composable
fun OrderTrackingScreen(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF333333)) // Dark background for map
    ) {
        // Map Center (Simulated)
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("🗺️", fontSize = 100.sp)
            Text(
                "Mapa de Seguimiento",
                color = Color.Gray,
                fontSize = 18.sp
            )
        }

        // Back Button
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .padding(16.dp)
                .background(Color(0xAA000000), shape = RoundedCornerShape(24.dp))
                .align(Alignment.TopStart)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Volver",
                tint = Color.White
            )
        }

        // Rider Status Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.BottomCenter),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CreamDark),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    "El repartidor está en camino",
                    color = BrownDark,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Llegada estimada: 15-20 min",
                    color = TextDark,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(16.dp))
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Cream.copy(alpha = 0.5f)))
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Repartidor: Carlos Gómez",
                            color = BrownDark,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Moto Honda Roja - ABC-123",
                            color = BrownMid,
                            fontSize = 12.sp
                        )
                    }
                    TextButton(onClick = { /* Simulated Call */ }) {
                        Text("📞 LLAMAR", color = GreenAccent, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}