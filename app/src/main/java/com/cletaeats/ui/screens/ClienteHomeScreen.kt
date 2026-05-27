package com.cletaeats.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import com.cletaeats.network.*
import com.cletaeats.ui.components.*
import com.cletaeats.ui.theme.*
import com.cletaeats.ui.tracking.*
import com.cletaeats.utils.LocalCacheManager
import com.cletaeats.utils.OrderUtils
import com.cletaeats.utils.currentConnectivityState
import com.cletaeats.utils.connectivityState
import com.cletaeats.utils.ConnectionState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClienteHomeScreen(onLogout: () -> Unit) {
    var activeTab by remember { mutableStateOf(ActiveTab.INICIO) }
    var restaurantes by remember { mutableStateOf<List<RestauranteItem>>(emptyList()) }
    var historial by remember { mutableStateOf<List<PedidoItem>>(emptyList()) }
    var menuCombos by remember { mutableStateOf<List<ComboItem>>(emptyList()) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var selectedRestaurant by remember { mutableStateOf<RestauranteItem?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var isMenuLoading by remember { mutableStateOf(false) }
    var userProfile by remember { mutableStateOf<UserProfile?>(null) }
    var historialFilterStatus by remember { mutableStateOf(OrderFilterStatus.ACTIVOS) }

    var cartItems by remember { mutableStateOf<List<CartItem>>(emptyList()) }
    var showCartSummary by remember { mutableStateOf(false) }
    var showPaymentDialog by remember { mutableStateOf(false) }
    var showOrderTracking by remember { mutableStateOf(false) }
    var isSubmittingOrder by remember { mutableStateOf(false) }
    var tarjetasGuardadas by remember { mutableStateOf<List<MetodoPago>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val sqliteHelper = remember { com.cletaeats.database.CletaSQLiteHelper(context) }

    var orderToTrack by remember { mutableStateOf<PedidoItem?>(null) }
    var orderToCancel by remember { mutableStateOf<PedidoItem?>(null) }
    var latestCreatedOrder by remember { mutableStateOf<PedidoItem?>(null) }

    val connectionState by connectivityState()

    fun refreshData() {
        coroutineScope.launch {
            try {
                val t = TokenManager.token ?: return@launch
                val response = CletaApi.retrofitService.getClienteHistorial("Bearer $t")
                if (response.success) {
                    val nuevosPedidos = response.data ?: emptyList()
                    historial = nuevosPedidos
                    sqliteHelper.guardarPedidos(nuevosPedidos)
                } else {
                    historial = sqliteHelper.obtenerPedidos()
                }
            } catch (e: Exception) {
                Log.e("CletaEats", "Error cargando historial: ${e.message}")
                historial = sqliteHelper.obtenerPedidos()
            }
        }
    }

    // ── Carga inicial: restaurantes con estrategia cache-first ─────────────
    LaunchedEffect(connectionState) {
        val isOnline = connectionState is ConnectionState.Available

        // 1. Mostrar caché inmediatamente si existe (evita pantalla de carga)
        val cachedRestaurantes = sqliteHelper.obtenerRestaurantes()
        if (cachedRestaurantes.isNotEmpty()) {
            restaurantes = cachedRestaurantes
            isLoading = false
            Log.d("CletaEats", "Restaurantes cargados desde caché SQLite (${cachedRestaurantes.size})")
        } else {
            // Fallback to old cache manager
            val oldCache = LocalCacheManager.getRestaurantesOffline()
            if (!oldCache.isNullOrEmpty()) {
                restaurantes = oldCache
                sqliteHelper.guardarRestaurantes(oldCache)
                isLoading = false
            } else {
                isLoading = true
            }
        }

        // También restaurar perfil desde caché
        val cachedProfile = LocalCacheManager.getUserProfile()
            ?: LocalCacheManager.getUserProfileOffline()
        if (cachedProfile != null) userProfile = cachedProfile

        // 2. Cargar historial y tarjetas siempre desde el servidor (datos dinámicos)
        refreshData()

        // 3. Actualizar restaurantes, perfil y tarjetas desde el servidor en background
        if (isOnline) {
            try {
                val token = TokenManager.token ?: return@LaunchedEffect
                val authHeader = "Bearer $token"
                val restResp = CletaApi.retrofitService.getRestaurantes()
                if (restResp.success) {
                    val nuevos = restResp.data ?: emptyList()
                    restaurantes = nuevos
                    sqliteHelper.guardarRestaurantes(nuevos)
                    Log.d("CletaEats", "Restaurantes actualizados desde API")
                    // Duplicar todos los combos localmente
                    nuevos.forEach { rest ->
                        try {
                            val comboResp = CletaApi.retrofitService.getCombosByRestaurant(authHeader, rest.id)
                            if (comboResp.success) {
                                val combos = comboResp.data ?: emptyList()
                                sqliteHelper.guardarCombos(rest.id, combos)
                            }
                        } catch (e: Exception) {
                            Log.e("CletaEats", "Error descargando combos de restaurante ${rest.id}: ${e.message}")
                        }
                    }
                }
                val tarjetasResp = CletaApi.retrofitService.getTarjetas(authHeader)
                if (tarjetasResp.success) {
                    val nuevasTarjetas = tarjetasResp.data ?: emptyList()
                    tarjetasGuardadas = nuevasTarjetas
                    sqliteHelper.guardarTarjetas(nuevasTarjetas)
                }
                // Cargar perfil del usuario
                try {
                    val perfilResp = CletaApi.retrofitService.getUserPerfil(authHeader)
                    Log.d("CletaEats", "Respuesta perfil - success: ${perfilResp.success}, data: ${perfilResp.data}, error: ${perfilResp.error}")
                    if (perfilResp.success && perfilResp.data != null) {
                        userProfile = perfilResp.data
                        LocalCacheManager.saveUserProfile(perfilResp.data)
                        Log.d("CletaEats", "Perfil de usuario actualizado: ${perfilResp.data.nombre}")
                    } else {
                        Log.w("CletaEats", "Perfil response no exitoso o sin data: ${perfilResp.error}")
                    }
                } catch (ep: Exception) {
                    Log.w("CletaEats", "Endpoint de perfil no disponible: ${ep.message}", ep)
                }
            } catch (e: Exception) {
                Log.e("CletaEats", "Error carga inicial desde API: ${e.message}")
                if (restaurantes.isEmpty()) {
                    restaurantes = sqliteHelper.obtenerRestaurantes()
                }
                if (userProfile == null) {
                    userProfile = LocalCacheManager.getUserProfileOffline()
                }
                if (tarjetasGuardadas.isEmpty()) {
                    tarjetasGuardadas = sqliteHelper.obtenerTarjetas()
                }
            }
        } else {
            if (restaurantes.isEmpty()) {
                restaurantes = sqliteHelper.obtenerRestaurantes()
                Log.w("CletaEats", "Sin conexión, usando caché offline de restaurantes")
            }
            if (tarjetasGuardadas.isEmpty()) {
                tarjetasGuardadas = sqliteHelper.obtenerTarjetas()
            }
        }

        isLoading = false
    }

    // ── Combos por restaurante: también cache-first ────────────────────────
    LaunchedEffect(selectedRestaurant, connectionState) {
        if (selectedRestaurant != null) {
            val restauranteId = selectedRestaurant!!.id
            val isOnline = connectionState is ConnectionState.Available

            // 1. Mostrar caché si existe
            val cachedCombos = sqliteHelper.obtenerCombos(restauranteId)
            if (cachedCombos.isNotEmpty()) {
                menuCombos = cachedCombos
                cartItems = emptyList()
                isMenuLoading = false
                Log.d("CletaEats", "Combos de restaurante $restauranteId desde caché")
            } else {
                isMenuLoading = true
                menuCombos = emptyList()
                cartItems = emptyList()
            }

            // 2. Actualizar desde el servidor si hay conexión
            if (isOnline) {
                try {
                    val token = TokenManager.token
                    if (token != null) {
                        val response = CletaApi.retrofitService
                            .getCombosByRestaurant("Bearer $token", restauranteId)
                        if (response.success) {
                            val nuevosCombos = response.data ?: emptyList()
                            menuCombos = nuevosCombos
                            sqliteHelper.guardarCombos(restauranteId, nuevosCombos) // guardar
                            Log.d("CletaEats", "Combos de restaurante $restauranteId actualizados")
                        }
                    }
                } catch (e: Exception) {
                    Log.e("CletaEats", "Error combos desde API: ${e.message}")
                    // Si falla y no había caché válido, intentar caché vencido
                    if (menuCombos.isEmpty()) {
                        menuCombos = sqliteHelper.obtenerCombos(restauranteId)
                    }
                }
            } else if (menuCombos.isEmpty()) {
                // Sin conexión: intentar con caché vencido
                menuCombos = sqliteHelper.obtenerCombos(restauranteId)
                Log.w("CletaEats", "Sin conexión, usando caché offline de combos")
            }

            isMenuLoading = false
        }
    }

    if (orderToTrack != null) {
        val trackingVm = remember(orderToTrack) { TrackingViewModel(orderToTrack!!) }
        OrderTrackingMapScreen(viewModel = trackingVm, onBack = { orderToTrack = null; refreshData() }, onOrderCancelled = { orderToTrack = null; refreshData() })
    } else if (showOrderTracking) {
        OrderTrackingScreen(onBack = { showOrderTracking = false; orderToTrack = latestCreatedOrder; selectedRestaurant = null })
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("🍜 CLETAEATS", fontWeight = FontWeight.Bold, color = Color.White) },
                    actions = { IconButton(onClick = onLogout) { Icon(Icons.Default.Logout, "Logout", tint = Color.White) } },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = BrownDark)
                )
            },
            bottomBar = {
                ClienteBottomBar(activeTab = activeTab, onTabSelect = { tab -> activeTab = tab; selectedRestaurant = null })
            }
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                if (isLoading) {
                    Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator(color = BrownDark) }
                } else if (selectedRestaurant != null) {
                    val cartMap = cartItems.associate { it.combo to it.cantidad }
                    RestaurantMenuView(
                        restaurante = selectedRestaurant!!, combos = menuCombos, isLoading = isMenuLoading,
                        cart = cartMap,
                        onCartChange = { newMap ->
                            cartItems = newMap.map { (combo, cant) ->
                                val existing = cartItems.find { it.combo.id == combo.id }
                                existing?.copy(cantidad = cant) ?: CartItem(combo, cant)
                            }
                        },
                        onBack = { selectedRestaurant = null; cartItems = emptyList() },
                        onProceedToCart = { showCartSummary = true }
                    )
                } else {
                    when (activeTab) {
                        ActiveTab.HISTORIAL -> ClienteHistorialTab(
                            historial = historial,
                            onTrackClick = { orderToTrack = it },
                            onCancelClick = { orderToCancel = it },
                            filterStatus = historialFilterStatus,
                            onFilterChange = { historialFilterStatus = it }
                        )
                        ActiveTab.INICIO -> ClienteInicioTab(
                            restaurantes = restaurantes, searchQuery = searchQuery, onSearchQueryChange = { searchQuery = it },
                            selectedCategory = selectedCategory, onCategorySelect = { selectedCategory = it }, onRestaurantSelect = { selectedRestaurant = it }
                        )
                        ActiveTab.PERFIL -> ClientePerfilTab(
                            tarjetas = tarjetasGuardadas,
                            userProfile = userProfile,
                            onAddCardClick = { showPaymentDialog = true },
                            onDeleteCard = { id ->
                                coroutineScope.launch {
                                    try {
                                        val t = TokenManager.token ?: return@launch
                                        val resp = CletaApi.retrofitService.deleteTarjeta("Bearer $t", id)
                                        if (resp.success) {
                                            tarjetasGuardadas = tarjetasGuardadas.filter { it.id != id }
                                            sqliteHelper.eliminarTarjeta(id)
                                        }
                                    } catch (e: Exception) { Log.e("CletaEats", "Error borrando tarjeta: ${e.message}") }
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showCartSummary) {
        CartSummaryDialog(
            cartItems = cartItems,
            onCartItemChange = { changedItem ->
                cartItems = cartItems.map { if (it.combo.id == changedItem.combo.id) changedItem else it }
            },
            onDeleteCartItem = { item ->
                val newCart = cartItems.filter { it.combo.id != item.combo.id }
                cartItems = newCart
                if (newCart.isEmpty()) {
                    showCartSummary = false
                }
            },
            onDismiss = { showCartSummary = false },
            onConfirm = { showCartSummary = false; showPaymentDialog = true }
        )
    }

    if (showPaymentDialog && (cartItems.isNotEmpty() && selectedRestaurant != null && activeTab == ActiveTab.INICIO)) {
        PaymentDialog(
            isSubmitting = isSubmittingOrder, tarjetas = tarjetasGuardadas,
            onDismiss = { showPaymentDialog = false },
            onSaveCard = { nuevaTarjeta ->
                coroutineScope.launch {
                    try {
                        val t = TokenManager.token ?: return@launch
                        val resp = CletaApi.retrofitService.guardarTarjeta("Bearer $t", nuevaTarjeta)
                        if (resp.success && resp.data != null) {
                            tarjetasGuardadas = tarjetasGuardadas + resp.data
                            sqliteHelper.guardarTarjetas(tarjetasGuardadas)
                        } else {
                            // Fallback
                            tarjetasGuardadas = tarjetasGuardadas + nuevaTarjeta
                            sqliteHelper.guardarTarjetas(tarjetasGuardadas)
                        }
                    } catch (e: Exception) {
                        Log.e("CletaEats", "Error guardando tarjeta: ${e.message}")
                        tarjetasGuardadas = tarjetasGuardadas + nuevaTarjeta
                        sqliteHelper.guardarTarjetas(tarjetasGuardadas)
                    }
                }
            },
            onConfirm = { numeroTarjetaFinal ->
                coroutineScope.launch {
                    isSubmittingOrder = true
                    try {
                        val t = TokenManager.token ?: return@launch
                        val request = OrderUtils.createPayload(selectedRestaurant!!.id, cartItems, numeroTarjetaFinal)
                        val resp = CletaApi.retrofitService.createOrder("Bearer $t", request)
                        if (resp.success) {
                            val idStr = resp.data?.replace("Pedido creado con ID: ", "")?.trim()
                            val orderId = idStr?.toIntOrNull() ?: 0
                            val totalCost = cartItems.sumOf { (it.combo.precio + if (it.agrandado) 1500.0 else 0.0) * it.cantidad }
                            latestCreatedOrder = PedidoItem(id = orderId, restauranteNombre = selectedRestaurant?.nombre ?: "Restaurante", total = totalCost + (totalCost * 0.13) + 1500.0, estado = "pendiente")
                            showPaymentDialog = false
                            refreshData()
                            showOrderTracking = true
                            cartItems = emptyList()
                        } else {
                            throw Exception("Fallback to local")
                        }
                    } catch (e: Exception) {
                        Log.e("CletaEats", "Error confirmación pedido: ${e.message}")
                        val totalCost = cartItems.sumOf { (it.combo.precio + if (it.agrandado) 1500.0 else 0.0) * it.cantidad }
                        val localOrder = PedidoItem(
                            id = (1000..9999).random(),
                            restauranteNombre = selectedRestaurant?.nombre ?: "Restaurante",
                            total = totalCost + (totalCost * 0.13) + 1500.0,
                            estado = "pendiente"
                        )
                        sqliteHelper.guardarPedidos(sqliteHelper.obtenerPedidos() + localOrder)
                        latestCreatedOrder = localOrder
                        showPaymentDialog = false
                        refreshData()
                        showOrderTracking = true
                        cartItems = emptyList()
                    } finally { isSubmittingOrder = false }
                }
            }
        )
    }

    if (showPaymentDialog && (cartItems.isEmpty() || activeTab == ActiveTab.PERFIL)) {
        PaymentDialog(
            isSubmitting = false, tarjetas = tarjetasGuardadas,
            onDismiss = { showPaymentDialog = false },
            onSaveCard = { nuevaTarjeta ->
                coroutineScope.launch {
                    try {
                        val t = TokenManager.token ?: return@launch
                        val resp = CletaApi.retrofitService.guardarTarjeta("Bearer $t", nuevaTarjeta)
                        if (resp.success && resp.data != null) {
                            tarjetasGuardadas = tarjetasGuardadas + resp.data
                            sqliteHelper.guardarTarjetas(tarjetasGuardadas)
                            showPaymentDialog = false
                        } else {
                            tarjetasGuardadas = tarjetasGuardadas + nuevaTarjeta
                            sqliteHelper.guardarTarjetas(tarjetasGuardadas)
                            showPaymentDialog = false
                        }
                    } catch (e: Exception) {
                        Log.e("CletaEats", "Error tarjeta perfil: ${e.message}")
                        tarjetasGuardadas = tarjetasGuardadas + nuevaTarjeta
                        sqliteHelper.guardarTarjetas(tarjetasGuardadas)
                        showPaymentDialog = false
                    }
                }
            },
            onConfirm = { showPaymentDialog = false }
        )
    }

    if (orderToCancel != null) {
        val trackingVm = remember(orderToCancel) { TrackingViewModel(orderToCancel!!) }
        CancelOrderDialog(
            order = orderToCancel!!,
            onDismiss = { orderToCancel = null },
            onConfirm = { orderToCancel = null; trackingVm.cancelOrder { refreshData() } }
        )
    }
}
