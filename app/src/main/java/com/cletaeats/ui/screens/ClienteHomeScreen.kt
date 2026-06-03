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
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.ui.unit.dp
import com.cletaeats.network.*
import com.cletaeats.storage.CloudPedidoStorage
import com.cletaeats.storage.LocalTransactionCounter
import com.cletaeats.storage.StorageOrchestrator
import com.cletaeats.storage.isCloud
import com.cletaeats.ui.components.*
import com.cletaeats.ui.theme.*
import com.cletaeats.ui.tracking.*
import com.cletaeats.utils.LocalCacheManager
import com.cletaeats.utils.LocalOrderUtils
import com.cletaeats.utils.OrderUtils
import com.cletaeats.utils.PedidoMergeUtils
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

    // Persistencia de valoraciones: Map<pedidoId, rating> guardado en SharedPreferences
    val valoracionesPrefs = remember {
        context.getSharedPreferences("cletaeats_valoraciones", android.content.Context.MODE_PRIVATE)
    }
    fun cargarValoraciones(): Map<Int, Int> {
        val json = valoracionesPrefs.getString("mapa", null) ?: return emptyMap()
        return try {
            val type = object : com.google.gson.reflect.TypeToken<Map<Int, Int>>() {}.type
            com.google.gson.Gson().fromJson(json, type) ?: emptyMap()
        } catch (e: Exception) { emptyMap() }
    }
    fun guardarValoracion(pedidoId: Int, rating: Int, valoraciones: Map<Int, Int>): Map<Int, Int> {
        val nuevo = valoraciones + (pedidoId to rating)
        valoracionesPrefs.edit().putString("mapa", com.google.gson.Gson().toJson(nuevo)).apply()
        return nuevo
    }

    var pedidosValorados by remember { mutableStateOf(cargarValoraciones()) }

    var orderToTrack by remember { mutableStateOf<PedidoItem?>(null) }
    var orderToCancel by remember { mutableStateOf<PedidoItem?>(null) }
    var latestCreatedOrder by remember { mutableStateOf<PedidoItem?>(null) }
    var pedidoAValorar by remember { mutableStateOf<PedidoItem?>(null) }
    var isSubmittingRating by remember { mutableStateOf(false) }

    val connectionState by connectivityState()
    val networkOnline = connectionState is ConnectionState.Available

    // ── Storage orchestrator state ────────────────────────────────────
    var showCloudScreen by remember { mutableStateOf(false) }
    // Todos los modos como State de Compose — init() los resetea a defaults en cada arranque
    var isApiMode by remember { mutableStateOf(com.cletaeats.database.SyncManager.isApiMode) }
    var isCloudForced by remember { mutableStateOf(StorageOrchestrator.isCloudForced) }
    var isDiskExpansionMode by remember { mutableStateOf(StorageOrchestrator.isDiskExpansionMode) }
    var currentThreshold by remember { mutableStateOf(LocalTransactionCounter.threshold) }
    var cloudPedidos by remember { mutableStateOf(CloudPedidoStorage.obtenerTodos()) }
    var isSyncingCloud by remember { mutableStateOf(false) }

    // Se recalcula en cada recomposición; networkOnline y los State anteriores disparan recomposición
    val storageMode = StorageOrchestrator.determinarModo()

    fun refreshData() {
        coroutineScope.launch {
            try {
                val localPedidos = sqliteHelper.obtenerPedidos()
                val cloudItems = CloudPedidoStorage.obtenerTodos()

                // Intentar cargar desde API siempre (si hay conexión y token)
                var serverPedidos: List<PedidoItem> = emptyList()
                try {
                    val t = TokenManager.token ?: throw Exception("Sin token")
                    val response = CletaApi.retrofitService.getClienteHistorial("Bearer $t")
                    if (response.success) serverPedidos = response.data ?: emptyList()
                } catch (e: Exception) {
                    Log.e("CletaEats", "Historial API no disponible, usando caché: ${e.message}")
                }

                val merged = if (serverPedidos.isNotEmpty()) {
                    val restaurantesLocales = sqliteHelper.obtenerRestaurantes()
                    PedidoMergeUtils.mergeWithLocalCache(serverPedidos, localPedidos, restaurantesLocales)
                        .also { sqliteHelper.guardarPedidos(it) }
                } else {
                    localPedidos
                }

                // Mostrar todos: API/local + nube, usando badge para identificar origen
                historial = (merged + cloudItems).distinctBy { it.id }
            } catch (e: Exception) {
                Log.e("CletaEats", "Error cargando historial: ${e.message}")
                val cloudItems = CloudPedidoStorage.obtenerTodos()
                historial = (sqliteHelper.obtenerPedidos() + cloudItems).distinctBy { it.id }
            }
        }
    }

    LaunchedEffect(Unit) {
        com.cletaeats.database.SyncManager.syncCompleted.collect {
            refreshData()
        }
    }

    // Garantía de consistencia: si el carrito queda vacío por cualquier causa
    // (onBack, fallo de red, cambio de restaurante) cerrar el CartSummary y el PaymentDialog.
    LaunchedEffect(cartItems) {
        if (cartItems.isEmpty()) {
            showCartSummary = false
            showPaymentDialog = false
        }
    }

    LaunchedEffect(Unit) {
        com.cletaeats.database.SyncManager.sessionExpired.collect {
            onLogout()
        }
    }

    // ÔöÇÔöÇ Carga inicial: restaurantes con estrategia cache-first ÔöÇÔöÇÔöÇÔöÇÔöÇÔöÇÔöÇÔöÇÔöÇÔöÇÔöÇÔöÇÔöÇ
    LaunchedEffect(connectionState) {
        val isOnline = connectionState is ConnectionState.Available

        // 1. Mostrar cach├® inmediatamente si existe (evita pantalla de carga)
        val cachedRestaurantes = sqliteHelper.obtenerRestaurantes()
        if (cachedRestaurantes.isNotEmpty()) {
            restaurantes = cachedRestaurantes
            isLoading = false
            Log.d("CletaEats", "Restaurantes cargados desde cach├® SQLite (${cachedRestaurantes.size})")
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

        // Tambi├®n restaurar perfil desde cach├®
        val cachedProfile = LocalCacheManager.getUserProfile()
            ?: LocalCacheManager.getUserProfileOffline()
        if (cachedProfile != null) userProfile = cachedProfile

        // 2. Cargar historial y tarjetas siempre desde el servidor (datos din├ímicos)
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
                    val locales = sqliteHelper.obtenerTarjetas()
                    val combinadas = (nuevasTarjetas + locales).distinctBy { it.numeroTarjeta }
                    tarjetasGuardadas = combinadas
                    sqliteHelper.guardarTarjetas(combinadas)
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
                Log.w("CletaEats", "Sin conexi├│n, usando cach├® offline de restaurantes")
            }
            if (tarjetasGuardadas.isEmpty()) {
                tarjetasGuardadas = sqliteHelper.obtenerTarjetas()
            }
        }

        isLoading = false
    }

    // ÔöÇÔöÇ Combos por restaurante: tambi├®n cache-first ÔöÇÔöÇÔöÇÔöÇÔöÇÔöÇÔöÇÔöÇÔöÇÔöÇÔöÇÔöÇÔöÇÔöÇÔöÇÔöÇÔöÇÔöÇÔöÇÔöÇÔöÇÔöÇÔöÇÔöÇ
    LaunchedEffect(selectedRestaurant, connectionState) {
        if (selectedRestaurant != null) {
            val restauranteId = selectedRestaurant!!.id
            val isOnline = connectionState is ConnectionState.Available

            // 1. Mostrar cach├® si existe
            val cachedCombos = sqliteHelper.obtenerCombos(restauranteId)
            if (cachedCombos.isNotEmpty()) {
                menuCombos = cachedCombos
                cartItems = emptyList()
                isMenuLoading = false
                Log.d("CletaEats", "Combos de restaurante $restauranteId desde cach├®")
            } else {
                isMenuLoading = true
                menuCombos = emptyList()
                cartItems = emptyList()
            }

            // 2. Actualizar desde el servidor si hay conexi├│n
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
                    // Si falla y no hab├¡a cach├® v├ílido, intentar cach├® vencido
                    if (menuCombos.isEmpty()) {
                        menuCombos = sqliteHelper.obtenerCombos(restauranteId)
                    }
                }
            } else if (menuCombos.isEmpty()) {
                // Sin conexi├│n: intentar con cach├® vencido
                menuCombos = sqliteHelper.obtenerCombos(restauranteId)
                Log.w("CletaEats", "Sin conexi├│n, usando cach├® offline de combos")
            }

            isMenuLoading = false
        }
    }

    if (showCloudScreen) {
        CloudStorageScreen(
            pedidos = cloudPedidos,
            canSync = networkOnline,
            isSyncing = isSyncingCloud,
            onBack = { showCloudScreen = false },
            onSync = {
                isSyncingCloud = true
                coroutineScope.launch {
                    try {
                        com.cletaeats.database.SyncManager.sincronizar()
                    } finally {
                        isSyncingCloud = false
                    }
                }
            }
        )
    } else if (orderToTrack != null) {
        val trackingVm = remember(orderToTrack) { TrackingViewModel(orderToTrack!!) }
        OrderTrackingMapScreen(viewModel = trackingVm, onBack = { orderToTrack = null; refreshData() }, onOrderCancelled = { orderToTrack = null; refreshData() })
    } else if (showOrderTracking) {
        OrderTrackingScreen(onBack = { showOrderTracking = false; orderToTrack = latestCreatedOrder; selectedRestaurant = null })
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.DeliveryDining, contentDescription = null, tint = OrangeSoft, modifier = Modifier.size(24.dp))
                            Text("CLETAEATS", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    },
                    actions = {
                        // Ícono nube — badge con el contador si hay pedidos en cloud
                        IconButton(onClick = { cloudPedidos = CloudPedidoStorage.obtenerTodos(); showCloudScreen = true }) {
                            BadgedBox(
                                badge = {
                                    if (cloudPedidos.isNotEmpty()) {
                                        Badge { Text("${cloudPedidos.size}") }
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Cloud, contentDescription = "Ver nube", tint = Color.White)
                            }
                        }
                        IconButton(onClick = onLogout) { Icon(Icons.Default.Logout, "Logout", tint = Color.White) }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = BrownDark)
                )
            },
            bottomBar = {
                ClienteBottomBar(activeTab = activeTab, onTabSelect = { tab ->
                    activeTab = tab
                    selectedRestaurant = null
                    // Refrescar lista cloud al entrar al historial
                    if (tab == ActiveTab.HISTORIAL) cloudPedidos = CloudPedidoStorage.obtenerTodos()
                })
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                StorageBanner(
                    mode = storageMode,
                    isOnline = networkOnline,
                    localCount = LocalTransactionCounter.count,
                    localLimit = currentThreshold.limit
                )
                Box(modifier = Modifier.fillMaxSize()) {
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
                            onRateClick = { pedidoAValorar = it },
                            pedidosValorados = pedidosValorados,
                            filterStatus = historialFilterStatus,
                            onFilterChange = { historialFilterStatus = it },
                            cloudPedidoIds = cloudPedidos.map { it.id }.toSet()
                        )
                        ActiveTab.INICIO -> ClienteInicioTab(
                            restaurantes = restaurantes, searchQuery = searchQuery, onSearchQueryChange = { searchQuery = it },
                            selectedCategory = selectedCategory, onCategorySelect = { selectedCategory = it }, onRestaurantSelect = { selectedRestaurant = it }
                        )
                        ActiveTab.PERFIL -> ClientePerfilTab(
                            tarjetas = tarjetasGuardadas,
                            userProfile = userProfile,
                            isApiMode = isApiMode,
                            isCloudForced = isCloudForced,
                            onCloudForceToggle = { enabled ->
                                isCloudForced = enabled
                                StorageOrchestrator.isCloudForced = enabled
                                if (enabled) isDiskExpansionMode = false // excluyente
                            },
                            isDiskExpansionMode = isDiskExpansionMode,
                            onDiskExpansionToggle = { enabled ->
                                isDiskExpansionMode = enabled
                                StorageOrchestrator.isDiskExpansionMode = enabled
                                if (enabled) {
                                    isCloudForced = false
                                    StorageOrchestrator.isCloudForced = false
                                    currentThreshold = LocalTransactionCounter.threshold
                                }
                            },
                            isOnline = networkOnline,
                            currentThreshold = currentThreshold,
                            onThresholdChange = {
                                currentThreshold = it
                                LocalTransactionCounter.threshold = it
                            },
                            localCount = LocalTransactionCounter.count,
                            cloudCount = cloudPedidos.size,
                            onViewCloud = { cloudPedidos = CloudPedidoStorage.obtenerTodos(); showCloudScreen = true },
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
                                    } catch (e: Exception) {
                                        Log.e("CletaEats", "Error borrando tarjeta: ${e.message}")
                                        tarjetasGuardadas = tarjetasGuardadas.filter { it.id != id }
                                        sqliteHelper.eliminarTarjeta(id)
                                        com.cletaeats.database.SyncManager.guardarYSincronizar("DELETE_CARD", id.toString())
                                    }
                                }
                            }
                        )
                    }
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

    if (showPaymentDialog) {
    val isOrderMode = cartItems.isNotEmpty() && selectedRestaurant != null && activeTab == ActiveTab.INICIO
    if (isOrderMode) {
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
                            tarjetasGuardadas = tarjetasGuardadas + nuevaTarjeta
                            sqliteHelper.guardarTarjetas(tarjetasGuardadas)
                            val json = com.google.gson.Gson().toJson(nuevaTarjeta.copy(cvv = ""))
                            com.cletaeats.database.SyncManager.guardarYSincronizar("SAVE_CARD", json)
                        }
                    } catch (e: Exception) {
                        Log.e("CletaEats", "Error guardando tarjeta: ${e.message}")
                        tarjetasGuardadas = tarjetasGuardadas + nuevaTarjeta
                        sqliteHelper.guardarTarjetas(tarjetasGuardadas)
                        val json = com.google.gson.Gson().toJson(nuevaTarjeta)
                        com.cletaeats.database.SyncManager.guardarYSincronizar("SAVE_CARD", json)
                    }
                }
            },
            onConfirm = { numeroTarjetaFinal ->
                coroutineScope.launch {
                    isSubmittingOrder = true
                    try {
                        val t = TokenManager.token ?: throw Exception("Sin token")
                        val totalCost = cartItems.sumOf { (it.combo.precio + if (it.agrandado) 1500.0 else 0.0) * it.cantidad }
                        val totalFinal = totalCost + (totalCost * 0.13) + 1500.0

                        // Los toggles dictan el modo: expansión/cloud overrides API cuando están activos
                        val usarApi = networkOnline && !isCloudForced && !isDiskExpansionMode
                        Log.d("CletaEats", "onConfirm → online=$networkOnline cloudForced=$isCloudForced expansion=$isDiskExpansionMode → usarApi=$usarApi")

                        if (usarApi) {
                            // ── Intento API ─────────────────────────────────────
                            val request = OrderUtils.createPayload(selectedRestaurant!!.id, cartItems, numeroTarjetaFinal)
                            val resp = CletaApi.retrofitService.createOrder("Bearer $t", request)
                            if (resp.success) {
                                val orderId = resp.data?.replace("Pedido creado con ID: ", "")?.trim()?.toIntOrNull() ?: 0
                                latestCreatedOrder = PedidoItem(
                                    id = orderId,
                                    restauranteNombre = selectedRestaurant?.nombre ?: "Restaurante",
                                    total = totalFinal,
                                    estado = "pendiente"
                                )
                                // Respaldo silencioso en local para continuidad si se pierde conexión
                                try {
                                    StorageOrchestrator.guardarPedidoLocal(latestCreatedOrder!!, sqliteHelper)
                                    Log.d("CletaEats", "Pedido #$orderId respaldado localmente tras éxito de API")
                                } catch (backupEx: Exception) {
                                    Log.w("CletaEats", "Backup local post-API falló (no crítico): ${backupEx.message}")
                                }
                                showPaymentDialog = false
                                refreshData()
                                showOrderTracking = true
                                cartItems = emptyList()
                                return@launch  // Éxito — salir sin pasar por fallback
                            }
                            // API respondió pero con error → caer a fallback local/cloud
                            Log.w("CletaEats", "API rechazó el pedido, guardando en fallback")
                        }

                        // ── Fallback LOCAL / CLOUD ───────────────────────────────
                        // (modo cloud/local explícito, o API caída con internet disponible)
                        val localOrderId = LocalOrderUtils.generateLocalOrderId()
                        val localOrder = PedidoItem(
                            id = localOrderId,
                            restauranteNombre = selectedRestaurant?.nombre ?: "Restaurante",
                            total = totalFinal,
                            estado = "pendiente"
                        )
                        val modoUsado = StorageOrchestrator.guardarPedidoLocal(localOrder, sqliteHelper)
                        Log.d("CletaEats", "Pedido #$localOrderId → $modoUsado")

                        if (!modoUsado.isCloud()) {
                            try {
                                val request = OrderUtils.createPayload(selectedRestaurant!!.id, cartItems, numeroTarjetaFinal)
                                com.cletaeats.database.SyncManager.guardarYSincronizar(
                                    "CREATE_ORDER",
                                    com.google.gson.Gson().toJson(PendingCreateOrderPayload(localOrderId = localOrderId, request = request))
                                )
                            } catch (ex: Exception) {
                                Log.e("CletaEats", "Error encolando para sync: ${ex.message}")
                            }
                        }
                        cloudPedidos = CloudPedidoStorage.obtenerTodos()
                        latestCreatedOrder = localOrder
                        showPaymentDialog = false
                        refreshData()
                        showOrderTracking = true
                        cartItems = emptyList()

                    } catch (e: Exception) {
                        // Fallo de red total → guardar localmente sin importar el modo
                        Log.e("CletaEats", "Error al confirmar pedido: ${e.message}")
                        val totalCost = cartItems.sumOf { (it.combo.precio + if (it.agrandado) 1500.0 else 0.0) * it.cantidad }
                        val localOrderId = LocalOrderUtils.generateLocalOrderId()
                        val localOrder = PedidoItem(
                            id = localOrderId,
                            restauranteNombre = selectedRestaurant?.nombre ?: "Restaurante",
                            total = totalCost + (totalCost * 0.13) + 1500.0,
                            estado = "pendiente"
                        )
                        StorageOrchestrator.guardarPedidoLocal(localOrder, sqliteHelper)
                        cloudPedidos = CloudPedidoStorage.obtenerTodos()
                        latestCreatedOrder = localOrder
                        showPaymentDialog = false
                        refreshData()
                        showOrderTracking = true
                        cartItems = emptyList()
                    } finally {
                        isSubmittingOrder = false
                    }
                }
            }
        )
    } else {
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
                            val json = com.google.gson.Gson().toJson(nuevaTarjeta.copy(cvv = ""))
                            com.cletaeats.database.SyncManager.guardarYSincronizar("SAVE_CARD", json)
                            showPaymentDialog = false
                        }
                    } catch (e: Exception) {
                        Log.e("CletaEats", "Error tarjeta perfil: ${e.message}")
                        tarjetasGuardadas = tarjetasGuardadas + nuevaTarjeta
                        sqliteHelper.guardarTarjetas(tarjetasGuardadas)
                        val json = com.google.gson.Gson().toJson(nuevaTarjeta)
                        com.cletaeats.database.SyncManager.guardarYSincronizar("SAVE_CARD", json)
                        showPaymentDialog = false
                    }
                }
            },
            onConfirm = { showPaymentDialog = false }
        )
    }
    } // end if (showPaymentDialog)

    if (orderToCancel != null) {
        val trackingVm = remember(orderToCancel) { TrackingViewModel(orderToCancel!!) }
        CancelOrderDialog(
            order = orderToCancel!!,
            onDismiss = { orderToCancel = null },
            onConfirm = {
                val cancelledId = orderToCancel?.id
                orderToCancel = null
                // Refleja la cancelación de inmediato en la UI sin esperar el refresh
                if (cancelledId != null) {
                    historial = historial.map {
                        if (it.id == cancelledId) it.copy(estado = "cancelado") else it
                    }
                }
                trackingVm.cancelOrder { refreshData() }
            }
        )
    }

    if (pedidoAValorar != null) {
        RatingDialog(
            pedidoId = pedidoAValorar!!.id,
            isSubmitting = isSubmittingRating,
            onDismiss = { pedidoAValorar = null },
            onConfirm = { rating, comentario ->
                coroutineScope.launch {
                    isSubmittingRating = true
                    try {
                        val t = TokenManager.token ?: return@launch
                        val resp = CletaApi.retrofitService.valorarPedido(
                            "Bearer $t",
                            pedidoAValorar!!.id,
                            com.cletaeats.network.ValoracionRequest(rating, comentario.ifBlank { null })
                        )
                        if (resp.success) {
                            val idValorado = pedidoAValorar!!.id
                            pedidosValorados = guardarValoracion(idValorado, rating, pedidosValorados)
                            pedidoAValorar = null
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("CletaEats", "Error enviando valoración: ${e.message}")
                        // Solo bloquear revaloración si el rating es válido (≥1).
                        // Con rating=0 el fallo fue antes de seleccionar — dejar al usuario reintentar.
                        val idValorado = pedidoAValorar?.id
                        if (idValorado != null && rating >= 1) {
                            pedidosValorados = guardarValoracion(idValorado, rating, pedidosValorados)
                        }
                        pedidoAValorar = null
                    } finally {
                        isSubmittingRating = false
                    }
                }
            }
        )
    }
}
