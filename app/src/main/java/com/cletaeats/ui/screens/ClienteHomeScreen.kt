package com.cletaeats.ui.screens

import android.util.Log
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cletaeats.network.*
import com.cletaeats.ui.components.*
import com.cletaeats.ui.theme.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
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
                        Icon(Icons.Default.Logout, "Logout", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BrownDark)
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // Simplificado para asegurar que siempre cargue el contenido del cliente por ahora
            ClienteContent()
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

    // 1. CARGA INICIAL (Restaurantes, Historial, Tarjetas)
    LaunchedEffect(Unit) {
        isLoading = true
        try {
            val token = TokenManager.token
            if (token == null) {
                Log.e("CletaEats", "Token es NULO. Redirigiendo o fallando.")
                return@LaunchedEffect
            }
            val authHeader = "Bearer $token"

            // Restaurantes
            val restResp = CletaApi.retrofitService.getRestaurantes()
            if (restResp.success) restaurantes = restResp.data ?: emptyList()

            // Historial
            val histResp = CletaApi.retrofitService.getClienteHistorial(authHeader)
            if (histResp.success) historial = histResp.data ?: emptyList()

            // Tarjetas
            val tarjetasResp = CletaApi.retrofitService.getTarjetas(authHeader)
            if (tarjetasResp.success) tarjetasGuardadas = tarjetasResp.data ?: emptyList()

        } catch (e: Exception) {
            Log.e("CletaEats", "Error en carga inicial: ${e.message}")
        } finally {
            isLoading = false
        }
    }

    // 2. CARGA DE COMBOS (Cuando se toca un restaurante)
    LaunchedEffect(selectedRestaurant) {
        if (selectedRestaurant != null) {
            isMenuLoading = true
            menuCombos = emptyList() // Limpiar menú anterior
            try {
                val token = TokenManager.token
                if (token != null) {
                    val response = CletaApi.retrofitService.getCombosByRestaurant(
                        token = "Bearer $token",
                        restauranteId = selectedRestaurant!!.id
                    )
                    if (response.success) {
                        menuCombos = response.data ?: emptyList()
                    }
                }
            } catch (e: Exception) {
                Log.e("CletaEats", "Error cargando combos: ${e.message}")
            } finally {
                isMenuLoading = false
            }
        }
    }

    // --- RENDERIZADO DE VISTAS ---
    if (showOrderTracking) {
        OrderTrackingScreen(onBack = {
            showOrderTracking = false
            selectedRestaurant = null
        })
    } else if (isLoading) {
        Box(Modifier.fillMaxSize(), Alignment.Center) {
            CircularProgressIndicator(color = BrownDark)
        }
    } else if (selectedRestaurant != null) {
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
        // VISTA PRINCIPAL
        Column(Modifier.fillMaxSize().background(Cream)) {
            // Buscador
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
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
                    LazyRow(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(categorias) { (nombre, icono) ->
                            CategoryItem(nombre, icono, selectedCategory == nombre) {
                                selectedCategory = if (selectedCategory == nombre) null else nombre
                            }
                        }
                    }
                }

                item {
                    val filtered = restaurantes.filter {
                        (selectedCategory == null || it.tipoComida?.contains(selectedCategory!!, true) == true) &&
                                it.nombre.contains(searchQuery, true)
                    }
                    Column(Modifier.padding(horizontal = 16.dp)) {
                        filtered.chunked(2).forEach { fila ->
                            Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), Arrangement.spacedBy(12.dp)) {
                                fila.forEach { rest ->
                                    RestaurantGridItem(rest, Modifier.weight(1f)) {
                                        selectedRestaurant = rest
                                    }
                                }
                                if (fila.size == 1) Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }

                if (selectedCategory == null && searchQuery.isEmpty()) {
                    stickyHeader { SectionHeader("Historial", Icons.Default.History) }
                    items(historial) { pedido ->
                        Box(Modifier.padding(horizontal = 16.dp)) { OrderCard(pedido) }
                    }
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }

    // --- LÓGICA DE DIÁLOGOS ---
    if (showCheckoutDialog && selectedCombo != null) {
        CheckoutDialog(
            combo = selectedCombo!!, notes = extraNotes, isAgrandado = isAgrandado,
            onNotesChange = { extraNotes = it }, onAgrandadoChange = { isAgrandado = it },
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
                        val t = TokenManager.token ?: return@launch
                        val resp = CletaApi.retrofitService.guardarTarjeta("Bearer $t", nuevaTarjeta)
                        if (resp.success && resp.data != null) {
                            tarjetasGuardadas = tarjetasGuardadas + resp.data
                        }
                    } catch (e: Exception) { Log.e("CletaEats", "Error tarjeta: ${e.message}") }
                }
            },
            onConfirm = { paymentMethod ->
                coroutineScope.launch {
                    isSubmittingOrder = true
                    try {
                        val t = TokenManager.token ?: return@launch

                        // Usamos el builder corregido
                        val request = OrderUtils.createPayload(
                            restaurantId = selectedRestaurant!!.id,
                            combo = selectedCombo!!
                        )

                        val resp = CletaApi.retrofitService.createOrder("Bearer $t", request)

                        if (resp.success) {
                            showPaymentDialog = false
                            showOrderTracking = true
                        } else {
                            // REGLA: Log claro con tag CletaEats
                            Log.e("CletaEats", "Error 400 o Validación: ${resp.error}")
                        }
                    } catch (e: Exception) {
                        Log.e("CletaEats", "Fallo de red: ${e.message}")
                    } finally {
                        isSubmittingOrder = false
                    }
                }
            }
        )
    }
}

@Composable
fun SectionHeader(title: String, icon: ImageVector? = null) {
    Surface(Modifier.fillMaxWidth(), color = CreamDark, shadowElevation = 2.dp) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) Icon(icon, null, tint = BrownDark)
            Spacer(Modifier.width(8.dp))
            Text(title, fontWeight = FontWeight.Bold, color = BrownDark)
        }
    }
}



object OrderUtils {
    fun createPayload(
        restaurantId: Int,
        combo: ComboItem,
        isFeriado: Boolean = false
    ): CreateOrderPayload {
        val subtotal = combo.precio
        val iva = subtotal * 0.13
        val envio = 1500.0
        val total = subtotal + iva + envio

        // Formato exacto: 2026-05-15T10:00:00
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
        val ahora = LocalDateTime.now().format(formatter)
        val entregaEstimada = LocalDateTime.now().plusMinutes(45).format(formatter)

        return CreateOrderPayload(
            pedido = OrderRequest(
                restauranteId = restaurantId,
                subtotal = subtotal,
                costoEnvio = envio,
                iva = iva,
                total = total,
                distanciaKm = 5.0,
                fechaPedido = ahora,
                fechaEntrega = entregaEstimada,
                detalles = listOf(
                    OrderItem(
                        productoId = combo.id,
                        cantidad = 1,
                        precio = combo.precio
                    )
                )
            ),
            esFeriado = isFeriado
        )
    }
}