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
import androidx.compose.ui.text.font.FontWeight
import com.cletaeats.network.*
import com.cletaeats.ui.components.*
import com.cletaeats.ui.theme.*
import com.cletaeats.ui.tracking.*
import com.cletaeats.utils.OrderUtils
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

    var cartItems by remember { mutableStateOf<List<CartItem>>(emptyList()) }
    var showCartSummary by remember { mutableStateOf(false) }
    var showPaymentDialog by remember { mutableStateOf(false) }
    var showOrderTracking by remember { mutableStateOf(false) }
    var isSubmittingOrder by remember { mutableStateOf(false) }
    var tarjetasGuardadas by remember { mutableStateOf<List<MetodoPago>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()

    var orderToTrack by remember { mutableStateOf<PedidoItem?>(null) }
    var orderToCancel by remember { mutableStateOf<PedidoItem?>(null) }
    var latestCreatedOrder by remember { mutableStateOf<PedidoItem?>(null) }

    fun refreshData() {
        coroutineScope.launch {
            try {
                val t = TokenManager.token ?: return@launch
                val response = CletaApi.retrofitService.getClienteHistorial("Bearer $t")
                if (response.success) historial = response.data ?: emptyList()
            } catch (e: Exception) {
                Log.e("CletaEats", "Error cargando historial: ${e.message}")
            }
        }
    }

    LaunchedEffect(Unit) {
        isLoading = true
        refreshData()
        try {
            val token = TokenManager.token ?: return@LaunchedEffect
            val authHeader = "Bearer $token"
            val restResp = CletaApi.retrofitService.getRestaurantes()
            if (restResp.success) restaurantes = restResp.data ?: emptyList()
            val tarjetasResp = CletaApi.retrofitService.getTarjetas(authHeader)
            if (tarjetasResp.success) tarjetasGuardadas = tarjetasResp.data ?: emptyList()
        } catch (e: Exception) {
            Log.e("CletaEats", "Error carga inicial: ${e.message}")
        } finally {
            isLoading = false
        }
    }

    LaunchedEffect(selectedRestaurant) {
        if (selectedRestaurant != null) {
            isMenuLoading = true
            menuCombos = emptyList()
            cartItems = emptyList()
            try {
                val token = TokenManager.token
                if (token != null) {
                    val response = CletaApi.retrofitService.getCombosByRestaurant("Bearer $token", selectedRestaurant!!.id)
                    if (response.success) menuCombos = response.data ?: emptyList()
                }
            } catch (e: Exception) {
                Log.e("CletaEats", "Error combos: ${e.message}")
            } finally {
                isMenuLoading = false
            }
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
                        ActiveTab.HISTORIAL -> ClienteHistorialTab(historial = historial, onTrackClick = { orderToTrack = it }, onCancelClick = { orderToCancel = it })
                        ActiveTab.INICIO -> ClienteInicioTab(
                            restaurantes = restaurantes, searchQuery = searchQuery, onSearchQueryChange = { searchQuery = it },
                            selectedCategory = selectedCategory, onCategorySelect = { selectedCategory = it }, onRestaurantSelect = { selectedRestaurant = it }
                        )
                        ActiveTab.PERFIL -> ClientePerfilTab(tarjetas = tarjetasGuardadas, onAddCardClick = { showPaymentDialog = true })
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
            onDismiss = { showCartSummary = false },
            onConfirm = { showCartSummary = false; showPaymentDialog = true }
        )
    }

    if (showPaymentDialog && cartItems.isNotEmpty() && selectedRestaurant != null) {
        PaymentDialog(
            isSubmitting = isSubmittingOrder, tarjetas = tarjetasGuardadas,
            onDismiss = { showPaymentDialog = false },
            onSaveCard = { nuevaTarjeta ->
                coroutineScope.launch {
                    try {
                        val t = TokenManager.token ?: return@launch
                        val resp = CletaApi.retrofitService.guardarTarjeta("Bearer $t", nuevaTarjeta)
                        if (resp.success && resp.data != null) tarjetasGuardadas = tarjetasGuardadas + resp.data
                    } catch (e: Exception) { Log.e("CletaEats", "Error guardando tarjeta: ${e.message}") }
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
                        }
                    } catch (e: Exception) { Log.e("CletaEats", "Error confirmación pedido: ${e.message}") } finally { isSubmittingOrder = false }
                }
            }
        )
    }

    if (showPaymentDialog && cartItems.isEmpty()) {
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
                            showPaymentDialog = false
                        }
                    } catch (e: Exception) { Log.e("CletaEats", "Error tarjeta perfil: ${e.message}") }
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
