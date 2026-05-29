import re

cards_file = 'app/src/main/java/com/cletaeats/ui/components/Cards.kt'
with open(cards_file, 'r', encoding='utf-8') as f: content = f.read()

content = content.replace('fun getCategoryEmoji(tipoComida: String?): String {', 'fun getCategoryIcon(tipoComida: String?): androidx.compose.ui.graphics.vector.ImageVector {')

imports = '''import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalPizza
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.RamenDining
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Icecream
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.SetMeal
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.LocalDrink'''

content = content.replace('import androidx.compose.material.icons.filled.Close', imports)

content = content.replace('return \"??\"', 'return androidx.compose.material.icons.filled.LocalPizza')
content = content.replace('return \"??\"', 'return androidx.compose.material.icons.filled.Fastfood')
content = content.replace('return \"??\"', 'return androidx.compose.material.icons.filled.RamenDining')
content = content.replace('return \"?\"', 'return androidx.compose.material.icons.filled.Coffee')
content = content.replace('return \"??\"', 'return androidx.compose.material.icons.filled.Icecream')
content = content.replace('return \"??\"', 'return androidx.compose.material.icons.filled.LocalDining')
content = content.replace('return \"??\"', 'return androidx.compose.material.icons.filled.Fastfood')
content = content.replace('return \"??\"', 'return androidx.compose.material.icons.filled.RamenDining')
content = content.replace('return \"??\"', 'return androidx.compose.material.icons.filled.SetMeal')
content = content.replace('return \"??\"', 'return androidx.compose.material.icons.filled.LocalDrink')
content = content.replace('return \"??\"', 'return androidx.compose.material.icons.filled.LocalDining')
content = content.replace('return \"??\"', 'return androidx.compose.material.icons.filled.SetMeal')
content = content.replace('return \"??\"', 'return androidx.compose.material.icons.filled.Restaurant')
content = content.replace('return \"??\"', 'return androidx.compose.material.icons.filled.Restaurant')

content = re.sub(r'return \"[^\"]+\"', 'return androidx.compose.material.icons.filled.Restaurant', content)

content = content.replace('Text(getCategoryEmoji(rest.tipoComida), fontSize = 48.sp)', 'Icon(getCategoryIcon(rest.tipoComida), contentDescription = rest.tipoComida, modifier = Modifier.size(48.dp), tint = BrownDark)')
content = content.replace('Text(getCategoryEmoji(rest.tipoComida), fontSize = 56.sp)', 'Icon(getCategoryIcon(rest.tipoComida), contentDescription = rest.tipoComida, modifier = Modifier.size(56.dp), tint = BrownDark)')

content = content.replace('fun CategoryItem(name: String, icon: String', 'fun CategoryItem(name: String, icon: androidx.compose.ui.graphics.vector.ImageVector')
content = content.replace('Text(icon, fontSize = 28.sp)', 'Icon(icon, contentDescription = name, modifier = Modifier.size(28.dp), tint = if (isSelected) Color.White else BrownDark)')

with open(cards_file, 'w', encoding='utf-8') as f: f.write(content)
