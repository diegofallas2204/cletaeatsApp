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

    // 1. CARGA INICIAL
    LaunchedEffect(Unit) {
        isLoading = true
        try {
            val token = TokenManager.token ?: return@LaunchedEffect
            val authHeader = "Bearer $token"

            val restResp = CletaApi.retrofitService.getRestaurantes()
            if (restResp.success) restaurantes = restResp.data ?: emptyList()

            val histResp = CletaApi.retrofitService.getClienteHistorial(authHeader)
            if (histResp.success) historial = histResp.data ?: emptyList()

            val tarjetasResp = CletaApi.retrofitService.getTarjetas(authHeader)
            if (tarjetasResp.success) tarjetasGuardadas = tarjetasResp.data ?: emptyList()

        } catch (e: Exception) {
            Log.e("CletaEats", "Error en carga inicial: ${e.message}")
        } finally {
            isLoading = false
        }
    }

    // 2. CARGA DE COMBOS
    LaunchedEffect(selectedRestaurant) {
        if (selectedRestaurant != null) {
            isMenuLoading = true
            menuCombos = emptyList()
            try {
                val token = TokenManager.token
                if (token != null) {
                    val response = CletaApi.retrofitService.getCombosByRestaurant(
                        token = "Bearer $token",
                        restauranteId = selectedRestaurant!!.id
                    )
                    if (response.success) menuCombos = response.data ?: emptyList()
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
        Column(Modifier.fillMaxSize().background(Cream)) {
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

    // --- DIÁLOGOS ---
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
                        if (resp.success && resp.data != null) tarjetasGuardadas = tarjetasGuardadas + resp.data
                    } catch (e: Exception) { Log.e("CletaEats", "Error tarjeta: ${e.message}") }
                }
            },
            // Dentro de ClienteContent -> PaymentDialog
            // Busca el bloque del PaymentDialog y reemplaza el onConfirm:
            // Busca el PaymentDialog y asegúrate de que el onConfirm esté así:
            onConfirm = { numeroTarjetaFinal ->
                Log.d("CletaEats", "ON_CONFIRM RECIBIDO: $numeroTarjetaFinal")

                // VALIDACIÓN DE SEGURIDAD PRE-REQUEST
                if (selectedRestaurant == null) {
                    Log.e("CletaEats", "ERROR: Restaurante es nulo")
                    return@PaymentDialog
                }
                if (selectedCombo == null) {
                    Log.e("CletaEats", "ERROR: Combo es nulo")
                    return@PaymentDialog
                }

                coroutineScope.launch {
                    isSubmittingOrder = true
                    try {
                        val t = TokenManager.token ?: run {
                            Log.e("CletaEats", "ERROR: Token es nulo")
                            return@launch
                        }

                        Log.d("CletaEats", "Construyendo Payload para restaurante: ${selectedRestaurant?.id}")

                        val request = OrderUtils.createPayload(
                            restaurantId = selectedRestaurant!!.id,
                            combo = selectedCombo!!,
                            tarjetaSeleccionada = numeroTarjetaFinal,
                            notas = extraNotes,
                            isAgrandado = isAgrandado
                        )

                        val resp = CletaApi.retrofitService.createOrder("Bearer $t", request)

                        if (resp.success) {
                            Log.d("CletaEats", "PEDIDO CREADO CON ÉXITO")
                            showPaymentDialog = false
                            showOrderTracking = true
                            // Limpieza
                            extraNotes = ""
                            selectedCombo = null
                            isAgrandado = false
                        } else {
                            Log.e("CletaEats", "Respuesta del Backend fallida: ${resp.error}")
                        }
                    } catch (e: Exception) {
                        Log.e("CletaEats", "FALLO CRÍTICO EN REQUEST: ${e.message}")
                        e.printStackTrace()
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
        tarjetaSeleccionada: String,
        notas: String? = null,
        isAgrandado: Boolean = false,
        isFeriado: Boolean = false
    ): CreateOrderPayload {
        val subtotal = combo.precio
        val iva = subtotal * 0.13
        val envio = 1500.0
        val total = subtotal + iva + envio

        val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
        val ahora = LocalDateTime.now().format(formatter)

        return CreateOrderPayload(
            pedido = OrderRequest(
                restauranteId = restaurantId,
                subtotal = subtotal,
                costoEnvio = envio,
                iva = iva,
                total = total,
                distanciaKm = 5.0,
                fechaPedido = ahora,
                fechaEntrega = null,
                numeroTarjeta = tarjetaSeleccionada, // ASIGNACIÓN CORRECTA
                detalles = listOf(
                    OrderItem(
                        comboId = combo.id,
                        cantidad = 1,
                        precio = combo.precio,
                        notas = notas,
                        agrandado = isAgrandado
                    )
                )
            ),
            esFeriado = isFeriado
        )
    }
}