package com.cletaeats.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cletaeats.ui.theme.BlueAccent
import com.cletaeats.ui.theme.OrangeSoft
import com.cletaeats.ui.theme.TextMid

/**
 * Marca de tarjeta. Fuente única de verdad para detectar VISA/Mastercard en
 * todas las pantallas (perfil cliente, perfil repartidor y diálogo de pago),
 * que antes usaban lógicas distintas e inconsistentes.
 */
enum class CardBrand(val label: String) {
    VISA("VISA"),
    MASTERCARD("Mastercard"),
    UNKNOWN("Tarjeta")
}

/**
 * Detecta la marca con el PAN completo (rangos reales de BIN) y también con el
 * PAN enmascarado (cuando solo se conoce el primer dígito, p.ej. "5****1234").
 *
 *  - VISA: empieza con 4.
 *  - Mastercard: 51-55 o 2221-2720; fallback a primer dígito 5/2 si está enmascarado.
 */
fun cardBrandOf(numero: String?): CardBrand {
    val n = numero?.trim().orEmpty()
    if (n.isEmpty()) return CardBrand.UNKNOWN
    if (n.startsWith("4")) return CardBrand.VISA

    val two = n.take(2).toIntOrNull()
    val four = n.take(4).toIntOrNull()
    if ((two != null && two in 51..55) || (four != null && four in 2221..2720)) {
        return CardBrand.MASTERCARD
    }
    // Fallback para PAN enmascarado (solo el primer dígito disponible).
    if (n.startsWith("5") || n.startsWith("2")) return CardBrand.MASTERCARD
    return CardBrand.UNKNOWN
}

/** Tag de color con la marca de la tarjeta (VISA azul, MC naranja). */
@Composable
fun CardBrandBadge(numero: String?, modifier: Modifier = Modifier) {
    when (cardBrandOf(numero)) {
        CardBrand.VISA -> Surface(modifier, shape = RoundedCornerShape(4.dp), color = BlueAccent) {
            Text(
                "VISA", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 9.sp,
                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
            )
        }
        CardBrand.MASTERCARD -> Surface(modifier, shape = RoundedCornerShape(4.dp), color = OrangeSoft) {
            Text(
                "MC", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 9.sp,
                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
            )
        }
        CardBrand.UNKNOWN -> Icon(
            Icons.Default.CreditCard, contentDescription = null, tint = TextMid, modifier = modifier
        )
    }
}
