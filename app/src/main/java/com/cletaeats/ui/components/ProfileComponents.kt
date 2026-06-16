package com.cletaeats.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cletaeats.network.UserProfile
import com.cletaeats.ui.theme.*

/**
 * Tarjeta reutilizable de "Información Personal". Muestra los datos del [profile]
 * con el [username] de la sesión. La fila de Cédula solo aparece si el perfil la trae.
 */
@Composable
fun PersonalInfoCard(
    username: String,
    profile: UserProfile?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = WhiteCard),
        border = BorderStroke(1.dp, CreamDark)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "Información Personal",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = BrownDark,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            ProfileInfoRow(Icons.Default.Person, "Usuario", username)
            ProfileRowDivider()

            ProfileInfoRow(Icons.Default.Person, "Nombre completo", profile?.nombre ?: "—")
            ProfileRowDivider()

            if (!profile?.cedula.isNullOrBlank()) {
                ProfileInfoRow(Icons.Default.Badge, "Cédula", profile!!.cedula!!)
                ProfileRowDivider()
            }

            ProfileInfoRow(Icons.Default.Email, "Correo", profile?.email ?: "—")
            ProfileRowDivider()

            ProfileInfoRow(Icons.Default.Phone, "Teléfono", profile?.telefono ?: "—")
            ProfileRowDivider()

            ProfileInfoRow(Icons.Default.Home, "Dirección", profile?.direccion ?: "—")
        }
    }
}

@Composable
fun ProfileInfoRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = OrangeSoft,
            modifier = Modifier.size(20.dp).padding(top = 2.dp)
        )
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, fontSize = 11.sp, color = TextMid, fontWeight = FontWeight.Bold)
            Text(
                value, fontSize = 14.sp, color = TextDark, fontWeight = FontWeight.Medium,
                maxLines = 2, overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ProfileRowDivider() {
    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = CreamDark)
}
