file = 'app/src/main/java/com/cletaeats/ui/components/Cards.kt'
import re
with open(file, 'r', encoding='utf-8') as f: content = f.read()

content = content.replace('icons.filled.RestaurantMenu', 'icons.filled.ShoppingCart')
content = re.sub(r'Text\(getCategoryIcon\(rest\.tipoComida\), fontSize = (\d+)\.sp\)', r'Icon(getCategoryIcon(rest.tipoComida), contentDescription = rest.tipoComida, modifier = Modifier.size(\1.dp), tint = BrownDark)', content)

with open(file, 'w', encoding='utf-8') as f: f.write(content)

file2 = 'app/src/main/java/com/cletaeats/ui/screens/ClienteInicioTab.kt'
with open(file2, 'r', encoding='utf-8') as f: content = f.read()
content = content.replace('icons.filled.RestaurantMenu', 'icons.filled.ShoppingCart')
with open(file2, 'w', encoding='utf-8') as f: f.write(content)
