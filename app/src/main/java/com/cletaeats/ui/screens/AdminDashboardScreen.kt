package com.cletaeats.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cletaeats.ui.theme.BrownDark
import com.cletaeats.ui.theme.Cream
import com.cletaeats.ui.theme.TextMid

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(onLogout: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panel Administrativo", fontWeight = FontWeight.Bold, color = Color.White) },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.Logout, "Logout", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BrownDark)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Cream)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.AdminPanelSettings,
                contentDescription = null,
                modifier = Modifier.size(100.dp),
                tint = BrownDark
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "CletaEats Admin Dashboard",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 22.sp,
                color = BrownDark
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Próximamente disponible con gestión de menús, combos e historial total de envíos.",
                color = TextMid,
                modifier = Modifier.padding(horizontal = 16.dp),
                fontSize = 14.sp
            )
        }
    }
}
