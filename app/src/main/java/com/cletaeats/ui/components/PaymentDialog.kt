package com.cletaeats.ui.components

import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.cletaeats.network.MetodoPago
import com.cletaeats.ui.theme.*

@Composable
fun PaymentDialog(
    isSubmitting: Boolean,
    tarjetas: List<MetodoPago>,
    onDismiss: () -> Unit,
    onSaveCard: (MetodoPago) -> Unit,
    onConfirm: (String) -> Unit,
    onCancelOrder: () -> Unit = {}
) {
    var selectedValue by remember(tarjetas) {
        mutableStateOf(tarjetas.firstOrNull()?.numeroTarjeta ?: "")
    }
    var showForm by remember { mutableStateOf(tarjetas.isEmpty()) }
    var num by remember { mutableStateOf("") }
    var exp by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }
    var biometricError by remember { mutableStateOf(false) }
    var biometricAttempts by remember { mutableStateOf(0) }

    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val canBiometric = remember(activity) {
        activity?.let {
            val result = BiometricManager.from(it).canAuthenticate(BIOMETRIC_WEAK)
            android.util.Log.d("CletaEats", "canAuthenticate(BIOMETRIC_WEAK) = $result")
            result == BiometricManager.BIOMETRIC_SUCCESS
        } ?: false
    }

    val isNumValid = num.length in 15..16
    val isCvvValid = cvv.length in 3..4
    val calendar = java.util.Calendar.getInstance()
    val currentYear2Digit = calendar.get(java.util.Calendar.YEAR) % 100
    val currentMonth = calendar.get(java.util.Calendar.MONTH) + 1
    val expMonth = if (exp.length == 5 && exp.contains("/")) exp.substringBefore("/").toIntOrNull() ?: 0 else 0
    val expYear = if (exp.length == 5 && exp.contains("/")) exp.substringAfter("/").toIntOrNull() ?: 0 else 0
    val isExpFormatValid = exp.length == 5 && exp.contains("/") && expMonth in 1..12
    val isNotExpired = expYear > currentYear2Digit || (expYear == currentYear2Digit && expMonth >= currentMonth)
    val isExpValid = isExpFormatValid && isNotExpired
    val isFormValid = isNumValid && isCvvValid && isExpValid

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Método de Pago", fontWeight = FontWeight.Bold, color = BrownDark) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (!showForm) {
                    tarjetas.forEach { tarjeta ->
                        Row(
                            Modifier.fillMaxWidth()
                                .clickable { selectedValue = tarjeta.numeroTarjeta }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (selectedValue == tarjeta.numeroTarjeta),
                                onClick = { selectedValue = tarjeta.numeroTarjeta }
                            )
                            CardTypeBadge(tarjeta.numeroTarjeta, modifier = Modifier.padding(start = 4.dp))
                            Text(
                                "**** ${tarjeta.numeroTarjeta.takeLast(4)}",
                                Modifier.padding(start = 6.dp),
                                color = TextDark
                            )
                        }
                    }
                    TextButton(onClick = { showForm = true; biometricError = false }) {
                        Text("+ Agregar Tarjeta", color = BrownDark, fontWeight = FontWeight.Bold)
                    }
                } else {
                    OutlinedTextField(
                        value = num,
                        onValueChange = { input ->
                            val filtered = input.filter { it.isDigit() }
                            if (filtered.length <= 16) num = filtered
                        },
                        label = { Text("Número de Tarjeta") },
                        trailingIcon = { CardTypeBadge(num) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(androidx.compose.ui.focus.FocusDirection.Down) }),
                        isError = num.isNotEmpty() && !isNumValid,
                        supportingText = if (num.isNotEmpty() && !isNumValid) { { Text("15 o 16 dígitos", color = MaterialTheme.colorScheme.error) } } else null,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                    OutlinedTextField(
                        value = exp,
                        onValueChange = { input ->
                            val clean = input.filter { it.isDigit() }
                            if (clean.length <= 4) {
                                exp = if (clean.length > 2) "${clean.substring(0, 2)}/${clean.substring(2)}" else clean
                            }
                        },
                        label = { Text("Expiración (MM/AA)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(androidx.compose.ui.focus.FocusDirection.Down) }),
                        isError = exp.isNotEmpty() && !isExpValid,
                        supportingText = if (exp.isNotEmpty()) {
                            if (!isExpFormatValid) { { Text("Formato MM/AA inválido", color = MaterialTheme.colorScheme.error) } }
                            else if (!isNotExpired) { { Text("La tarjeta está vencida", color = MaterialTheme.colorScheme.error) } }
                            else null
                        } else null,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                    OutlinedTextField(
                        value = cvv,
                        onValueChange = { input ->
                            val filtered = input.filter { it.isDigit() }
                            if (filtered.length <= 4) cvv = filtered
                        },
                        label = { Text("CVV") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { keyboardController?.hide(); focusManager.clearFocus() }),
                        isError = cvv.isNotEmpty() && !isCvvValid,
                        supportingText = if (cvv.isNotEmpty() && !isCvvValid) { { Text("3 o 4 dígitos", color = MaterialTheme.colorScheme.error) } } else null,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                    Button(
                        onClick = {
                            if (isFormValid) {
                                val nueva = MetodoPago(numeroTarjeta = num, fechaVencimiento = exp, cvv = cvv)
                                onSaveCard(nueva)
                                selectedValue = num
                                showForm = false
                                num = ""; exp = ""; cvv = ""
                            }
                        },
                        enabled = isFormValid,
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrownDark, contentColor = Color.White,
                            disabledContainerColor = Color.LightGray, disabledContentColor = Color.DarkGray
                        )
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "Guardar y Seleccionar", modifier = Modifier.size(24.dp))
                    }
                }
                if (biometricAttempts in 1..2) {
                    Text(
                        "Huella no reconocida. Intento $biometricAttempts/3.",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                } else if (biometricAttempts >= 3) {
                    Text(
                        "3 intentos fallidos. El pedido será cancelado.",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                } else if (biometricError) {
                    Text(
                        "Autenticación cancelada.",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            val isConfirmEnabled = selectedValue.isNotEmpty() && !isSubmitting && biometricAttempts < 3
            Button(
                onClick = {
                    Log.d("CletaEats", "Intentando confirmar pago con tarjeta ${selectedValue.takeLast(4)}")
                    biometricError = false
                    if (selectedValue.isNotEmpty()) {
                        if (canBiometric && activity != null) {
                            launchBiometric(
                                activity = activity,
                                lastFour = selectedValue.takeLast(4),
                                onSuccess = {
                                    biometricAttempts = 0
                                    onConfirm(selectedValue)
                                },
                                onFailed = {
                                    biometricAttempts++
                                    Log.w("CletaEats", "Biométrico fallido: intento $biometricAttempts/3")
                                    if (biometricAttempts >= 3) onCancelOrder()
                                },
                                onError = { biometricError = true }
                            )
                        } else {
                            onConfirm(selectedValue)
                        }
                    }
                },
                enabled = isConfirmEnabled,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrownDark, contentColor = Color.White,
                    disabledContainerColor = Color.LightGray, disabledContentColor = Color.DarkGray
                )
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                } else {
                    Icon(Icons.Default.CheckCircle, contentDescription = "Pagar y Finalizar", modifier = Modifier.size(24.dp))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cerrar", color = BrownMid, fontWeight = FontWeight.Bold) }
        },
        containerColor = Cream
    )
}

@Composable
private fun CardTypeBadge(num: String, modifier: Modifier = Modifier) {
    val isMc = (num.length >= 2 && num.take(2).toIntOrNull() in 51..55) ||
               (num.length >= 4 && num.take(4).toIntOrNull()?.let { it in 2221..2720 } == true)
    when {
        num.startsWith("4") ->
            Surface(modifier, shape = RoundedCornerShape(4.dp), color = BlueAccent) {
                Text("VISA", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 9.sp,
                     modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp))
            }
        isMc ->
            Surface(modifier, shape = RoundedCornerShape(4.dp), color = OrangeSoft) {
                Text("MC", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 9.sp,
                     modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp))
            }
        else -> Icon(Icons.Default.CreditCard, contentDescription = null, tint = TextMid, modifier = modifier)
    }
}

private fun launchBiometric(
    activity: FragmentActivity,
    lastFour: String,
    onSuccess: () -> Unit,
    onFailed: () -> Unit,
    onError: () -> Unit
) {
    val executor = ContextCompat.getMainExecutor(activity)
    val prompt = BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) = onSuccess()
        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) = onError()
        override fun onAuthenticationFailed() = onFailed()
    })
    prompt.authenticate(
        BiometricPrompt.PromptInfo.Builder()
            .setTitle("Confirmar pago")
            .setSubtitle("**** $lastFour")
            .setDescription("Autoriza el pago con tu huella")
            .setNegativeButtonText("Cancelar")
            .build()
    )
}
