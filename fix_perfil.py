file = 'app/src/main/java/com/cletaeats/ui/screens/RepartidorTabPerfil.kt'
with open(file, 'r', encoding='utf-8') as f: content = f.read()

content = content.replace('Text("? 4.9", fontWeight = FontWeight.Bold, color = TextDark, fontSize = 15.sp)', '''Row(verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.material3.Icon(
                            androidx.compose.material.icons.Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = androidx.compose.ui.graphics.Color(0xFFFFD700),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("4.9", fontWeight = FontWeight.Bold, color = TextDark, fontSize = 15.sp)
                    }''')

with open(file, 'w', encoding='utf-8') as f: f.write(content)
