package com.cletaeats.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.cletaeats.ui.theme.BrownDark
import com.cletaeats.ui.theme.Cream

enum class ActiveTab { HISTORIAL, INICIO, PERFIL }

@Composable
fun ClienteBottomBar(
    activeTab: ActiveTab,
    onTabSelect: (ActiveTab) -> Unit
) {
    NavigationBar(containerColor = BrownDark) {
        val items = listOf(
            Triple(ActiveTab.HISTORIAL, Icons.Default.History, "Historial"),
            Triple(ActiveTab.INICIO, Icons.Default.Home, "Inicio"),
            Triple(ActiveTab.PERFIL, Icons.Default.Person, "Perfil")
        )
        items.forEach { (tab, icon, label) ->
            val isSelected = activeTab == tab
            NavigationBarItem(
                icon = { Icon(icon, label, tint = if (isSelected) Cream else Color.LightGray) },
                label = { Text(label, color = if (isSelected) Cream else Color.LightGray) },
                selected = isSelected,
                onClick = { onTabSelect(tab) }
            )
        }
    }
}
