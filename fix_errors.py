file = 'app/src/main/java/com/cletaeats/ui/components/Cards.kt'
with open(file, 'r', encoding='utf-8') as f: content = f.read()
content = content.replace('icons.filled.Restaurant', 'icons.filled.RestaurantMenu')
content = content.replace('getCategoryEmoji', 'getCategoryIcon')
with open(file, 'w', encoding='utf-8') as f: f.write(content)

file2 = 'app/src/main/java/com/cletaeats/ui/screens/ClienteInicioTab.kt'
with open(file2, 'r', encoding='utf-8') as f: content = f.read()
content = content.replace('icons.filled.Restaurant', 'icons.filled.RestaurantMenu')
with open(file2, 'w', encoding='utf-8') as f: f.write(content)
