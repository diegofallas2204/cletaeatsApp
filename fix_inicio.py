inicio_file = 'app/src/main/java/com/cletaeats/ui/screens/ClienteInicioTab.kt'
with open(inicio_file, 'r', encoding='utf-8') as f: content = f.read()

imports = '''import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.LocalPizza
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.RamenDining
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Icecream
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.SetMeal
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.LocalDrink'''

content = content.replace('import androidx.compose.material.icons.filled.Search', imports)

old_cat = '''    val categorias = listOf(
        "Pizza" to "??", "Burger" to "??", "Pasta" to "??", "Café" to "?",
        "Postres" to "??", "Tacos" to "??", "Pollo" to "??",
        "China" to "??", "Mariscos" to "??", "Bebidas" to "??"
    )'''

new_cat = '''    val categorias = listOf(
        "Pizza" to Icons.Default.LocalPizza, "Burger" to Icons.Default.Fastfood, "Pasta" to Icons.Default.RamenDining, "Café" to Icons.Default.Coffee,
        "Postres" to Icons.Default.Icecream, "Tacos" to Icons.Default.LocalDining, "Pollo" to Icons.Default.Fastfood,
        "China" to Icons.Default.RamenDining, "Mariscos" to Icons.Default.SetMeal, "Bebidas" to Icons.Default.LocalDrink
    )'''

content = content.replace(old_cat, new_cat)

# Since I may have corrupted the UTF-8 reading earlier before git checkout, let's also remove ´++ just in case, though I already checked out.
if content.startswith('\ufeff'): content = content[1:]

with open(inicio_file, 'w', encoding='utf-8') as f: f.write(content)
