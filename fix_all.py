import re, codecs

# Fix Cards.kt
file = 'app/src/main/java/com/cletaeats/ui/components/Cards.kt'
with codecs.open(file, 'r', encoding='utf-8') as f: content = f.read()

content = re.sub(r'fun getCategoryEmoji\(tipoComida: String\?\): String \{', 'fun getCategoryIcon(tipoComida: String?): androidx.compose.ui.graphics.vector.ImageVector {', content)
content = re.sub(r'import androidx\.compose\.material\.icons\.filled\.Close', 'import androidx.compose.material.icons.filled.Close\nimport androidx.compose.material.icons.filled.Star\n', content)
content = re.sub(r'return \"[^\"]+\"', 'return androidx.compose.material.icons.filled.Star', content)
content = re.sub(r'Text\(getCategoryEmoji\(rest\.tipoComida\), fontSize = (\d+)\.sp\)', r'Icon(getCategoryIcon(rest.tipoComida), contentDescription = rest.tipoComida, modifier = Modifier.size(\1.dp), tint = BrownDark)', content)
content = re.sub(r'Text\(getCategoryIcon\(rest\.tipoComida\), fontSize = (\d+)\.sp\)', r'Icon(getCategoryIcon(rest.tipoComida), contentDescription = rest.tipoComida, modifier = Modifier.size(\1.dp), tint = BrownDark)', content)
content = re.sub(r'fun CategoryItem\(name: String, icon: String', 'fun CategoryItem(name: String, icon: androidx.compose.ui.graphics.vector.ImageVector', content)
content = re.sub(r'Text\(icon, fontSize = 28\.sp\)', 'Icon(icon, contentDescription = name, modifier = Modifier.size(28.dp), tint = if (isSelected) Color.White else BrownDark)', content)

with codecs.open(file, 'w', encoding='utf-8') as f: f.write(content)

# Fix ClienteInicioTab.kt
file2 = 'app/src/main/java/com/cletaeats/ui/screens/ClienteInicioTab.kt'
with codecs.open(file2, 'r', encoding='utf-8') as f: content2 = f.read()

content2 = re.sub(r'import androidx\.compose\.material\.icons\.filled\.Search', 'import androidx.compose.material.icons.filled.Search\nimport androidx.compose.material.icons.filled.Star\n', content2)

content2 = re.sub(r'val categorias = listOf\([^)]+\)', '''val categorias = listOf(
        "Pizza" to Icons.Default.Star, "Burger" to Icons.Default.Star, "Pasta" to Icons.Default.Star, "Café" to Icons.Default.Star,
        "Postres" to Icons.Default.Star, "Tacos" to Icons.Default.Star, "Pollo" to Icons.Default.Star,
        "China" to Icons.Default.Star, "Mariscos" to Icons.Default.Star, "Bebidas" to Icons.Default.Star
    )''', content2, flags=re.DOTALL)

with codecs.open(file2, 'w', encoding='utf-8') as f: f.write(content2)

