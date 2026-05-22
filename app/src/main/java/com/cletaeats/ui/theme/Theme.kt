package com.cletaeats.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = BrownDark,
    secondary = BrownMid,
    tertiary = OrangeSoft,
    background = Cream,
    surface = WhiteCard,
    onPrimary = Color.White,
    onSecondary = Color.White,
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

// Aliasing for compatibility with existing code
@Composable
fun PrototipoCETheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    CletaEatsTheme(content = content)
}