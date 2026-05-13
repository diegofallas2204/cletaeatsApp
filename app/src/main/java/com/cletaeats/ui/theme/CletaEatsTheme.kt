package com.cletaeats.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = BrownDark,
    secondary = BrownMid,
    tertiary = OrangeSoft,
    background = Cream,
    surface = WhiteCard,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    onTertiary = BrownDark,
    onBackground = TextDark,
    onSurface = TextDark
)

@Composable
fun CletaEatsTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}