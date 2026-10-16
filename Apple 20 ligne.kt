package com.example.apple
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
data class Product(val name: String, val price: Int, val icon: String)
val catalog = listOf(Product("iPhone 17 Pro", 1329, "📱"), Product("MacBook Air", 1199, "💻"), Product("iPad Pro", 1099, "📲"),
    Product("Apple Watch", 459, "⌚"), Product("AirPods Pro", 279, "🎧"), Product("Vision Pro", 3999, "🥽"))
@OptIn(ExperimentalMaterial3Api::class)
@Composable fun AppleStore() {
    var cart by remember { mutableStateOf(listOf<Product>()) }
    Scaffold(topBar = { TopAppBar(title = { Text("Apple Store") }, actions = { Text("🛒 ${cart.size} • ${cart.sumOf { it.price }} €", Modifier.padding(16.dp)) }) }) { pad ->
        LazyVerticalGrid(GridCells.Fixed(2), Modifier.padding(pad), contentPadding = PaddingValues(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(catalog) { p -> Card { Column(Modifier.padding(16.dp)) {
                Text(p.icon, style = MaterialTheme.typography.displayMedium); Text(p.name); Text("${p.price} €")
                Button({ cart = cart + p }) { Text("Ajouter") } } } }
        }
    }
}
@Preview(showBackground = true) @Composable fun StorePreview() = MaterialTheme { AppleStore() }
