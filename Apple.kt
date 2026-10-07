@file:OptIn(ExperimentalMaterial3Api::class)

/*
 * ============================================================================
 *  APPLE STORE (démo) — Application Android complète en UN SEUL fichier
 *  Kotlin + Jetpack Compose + Material 3
 * ============================================================================
 *
 *  FONCTIONNALITÉS
 *   - Catalogue (iPhone, iPad, Mac, Watch, AirPods, Accessoires)
 *   - Recherche + filtre par catégorie
 *   - Fiche produit : choix de la couleur et du stockage, prix dynamique
 *   - Favoris
 *   - Panier : quantités, suppression, code promo (APPLE10 = -10 %), livraison
 *   - Paiement : adresse + carte / Apple Pay / PayPal (simulé)
 *   - Confirmation de commande + historique des commandes
 *   - Thème clair / sombre automatique
 *
 *  MISE EN PLACE (Android Studio)
 *   1. Nouveau projet "Empty Activity" (Compose), package : com.example.applestore
 *   2. Remplacez le contenu de MainActivity.kt par ce fichier
 *      (ou ajoutez-le et supprimez l'ancienne MainActivity).
 *   3. Dépendances (app/build.gradle.kts) — déjà présentes dans le modèle Compose,
 *      sinon ajoutez :
 *
 *      implementation(platform("androidx.compose:compose-bom:2024.09.00"))
 *      implementation("androidx.compose.ui:ui")
 *      implementation("androidx.compose.material3:material3")
 *      implementation("androidx.compose.material:material-icons-core")
 *      implementation("androidx.activity:activity-compose:1.9.2")
 *      implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")
 *
 *   4. AndroidManifest.xml : l'activité ".MainActivity" avec le filtre MAIN/LAUNCHER
 *      (déjà généré par le modèle).
 *
 *  NOTE : les données produits sont des exemples fictifs et le paiement est
 *  simulé. Pour une vraie boutique, branchez une API (Retrofit) et un vrai
 *  prestataire de paiement (Google Pay, Stripe...).
 * ============================================================================
 */

package com.example.applestore

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ============================================================================
//  MODÈLES
// ============================================================================

enum class Category(val label: String, val emoji: String) {
    IPHONE("iPhone", "📱"),
    IPAD("iPad", "📲"),
    MAC("Mac", "💻"),
    WATCH("Watch", "⌚"),
    AUDIO("Audio", "🎧"),
    ACCESSORIES("Accessoires", "🔌")
}

data class ColorOpt(val name: String, val color: Color)

data class StorageOpt(val label: String, val extra: Double)

data class Product(
    val id: Int,
    val name: String,
    val category: Category,
    val basePrice: Double,
    val tagline: String,
    val description: String,
    val emoji: String,
    val colors: List<ColorOpt>,
    val storages: List<StorageOpt>,
    val specs: List<String>
)

data class CartItem(
    val product: Product,
    val color: ColorOpt,
    val storage: StorageOpt,
    val qty: Int
) {
    val key: String get() = "${product.id}-${color.name}-${storage.label}"
    val unitPrice: Double get() = product.basePrice + storage.extra
}

data class Order(
    val id: String,
    val items: List<CartItem>,
    val total: Double,
    val date: Long,
    val customer: String,
    val address: String,
    val payment: String
)

enum class Tab(val label: String) { SHOP("Boutique"), FAVS("Favoris"), CART("Panier"), ORDERS("Commandes") }

fun Double.eur(): String = NumberFormat.getCurrencyInstance(Locale.FRANCE).format(this)

// ============================================================================
//  CATALOGUE (données d'exemple)
// ============================================================================

object Catalog {
    private val storage128to1T = listOf(
        StorageOpt("128 Go", 0.0), StorageOpt("256 Go", 130.0),
        StorageOpt("512 Go", 360.0), StorageOpt("1 To", 590.0)
    )
    private val storage256to1T = listOf(
        StorageOpt("256 Go", 0.0), StorageOpt("512 Go", 230.0), StorageOpt("1 To", 460.0)
    )
    private val single = listOf(StorageOpt("Standard", 0.0))

    private val titane = listOf(
        ColorOpt("Titane naturel", Color(0xFFB8B1A6)),
        ColorOpt("Titane noir", Color(0xFF3B3B3D)),
        ColorOpt("Titane blanc", Color(0xFFE3E3DF)),
        ColorOpt("Titane désert", Color(0xFFBFA48F))
    )
    private val pastel = listOf(
        ColorOpt("Noir", Color(0xFF2B2B2D)),
        ColorOpt("Blanc", Color(0xFFF2F2F0)),
        ColorOpt("Bleu", Color(0xFF9DB7D1)),
        ColorOpt("Rose", Color(0xFFE7C3CB))
    )
    private val mac = listOf(
        ColorOpt("Gris sidéral", Color(0xFF6E6E73)),
        ColorOpt("Argent", Color(0xFFD9D9DB)),
        ColorOpt("Minuit", Color(0xFF2E3642))
    )
    private val whiteBlack = listOf(
        ColorOpt("Blanc", Color(0xFFF5F5F7)),
        ColorOpt("Noir", Color(0xFF1D1D1F))
    )

    val products: List<Product> = listOf(
        Product(
            1, "iPhone 17 Pro", Category.IPHONE, 1229.0,
            "Le plus puissant. Le plus pro.",
            "Boîtier en titane, puce dernière génération, système photo professionnel avec zoom optique avancé et autonomie sur toute la journée.",
            "📱", titane, storage256to1T,
            listOf("Écran Super Retina XDR 6,3\"", "Puce A-series Pro", "Triple capteur photo 48 Mpx", "USB-C", "Autonomie jusqu'à 27 h")
        ),
        Product(
            2, "iPhone 17", Category.IPHONE, 969.0,
            "Tout simplement magique.",
            "Un écran fluide 120 Hz, un double appareil photo performant et une puce rapide pour tous vos usages quotidiens.",
            "📱", pastel, storage128to1T.take(3),
            listOf("Écran Super Retina XDR 6,3\"", "Double capteur photo 48 Mpx", "USB-C", "Face ID", "Résistant à l'eau IP68")
        ),
        Product(
            3, "iPad Air", Category.IPAD, 719.0,
            "Léger. Puissant. Coloré.",
            "Un iPad fin et polyvalent avec une puce M-series, compatible Apple Pencil Pro et Magic Keyboard.",
            "📲", pastel, storage128to1T.take(3),
            listOf("Écran Liquid Retina 11\"", "Puce M-series", "Wi-Fi 6E", "Touch ID", "Compatible Apple Pencil Pro")
        ),
        Product(
            4, "iPad Pro", Category.IPAD, 1219.0,
            "Un écran OLED. Une puissance folle.",
            "Le summum de l'iPad : écran Ultra Retina XDR double OLED, design ultra-fin et performances dignes d'un ordinateur.",
            "📲", whiteBlack, storage256to1T,
            listOf("Écran Ultra Retina XDR 13\"", "Puce M-series", "Face ID", "Thunderbolt / USB 4", "4 haut-parleurs")
        ),
        Product(
            5, "MacBook Air 13\"", Category.MAC, 1199.0,
            "Étonnamment fin. Incroyablement rapide.",
            "Sans ventilateur donc silencieux, jusqu'à 18 heures d'autonomie et un écran Liquid Retina éclatant.",
            "💻", listOf(mac[1], mac[2], ColorOpt("Lumière stellaire", Color(0xFFE9DFCF))),
            listOf(StorageOpt("256 Go", 0.0), StorageOpt("512 Go", 230.0), StorageOpt("1 To", 460.0)),
            listOf("Écran Liquid Retina 13,6\"", "Puce M-series", "16 Go de mémoire unifiée", "MagSafe", "Autonomie jusqu'à 18 h")
        ),
        Product(
            6, "MacBook Pro 14\"", Category.MAC, 1999.0,
            "Pro, jusqu'au bout.",
            "Écran mini-LED ProMotion, ports pro (HDMI, SDXC, Thunderbolt) et performances soutenues pour les créatifs.",
            "💻", mac.take(2), listOf(StorageOpt("512 Go", 0.0), StorageOpt("1 To", 230.0), StorageOpt("2 To", 690.0)),
            listOf("Écran Liquid Retina XDR 14,2\"", "Puce M-series Pro", "24 Go de mémoire unifiée", "3 ports Thunderbolt", "Autonomie jusqu'à 22 h")
        ),
        Product(
            7, "iMac 24\"", Category.MAC, 1599.0,
            "Tout-en-un. Tout en couleurs.",
            "Un écran 4,5K éclatant, une caméra 1080p et six haut-parleurs dans un design de 11,5 mm d'épaisseur.",
            "🖥️", pastel, listOf(StorageOpt("256 Go", 0.0), StorageOpt("512 Go", 230.0)),
            listOf("Écran Retina 4,5K 24\"", "Puce M-series", "Caméra FaceTime HD 1080p", "Magic Keyboard inclus")
        ),
        Product(
            8, "Apple Watch Series 11", Category.WATCH, 459.0,
            "Votre santé en un coup d'œil.",
            "Suivi du sommeil, ECG, oxygène sanguin et détection des chutes. Écran toujours actif et charge rapide.",
            "⌚", listOf(ColorOpt("Minuit", Color(0xFF2B2E36)), ColorOpt("Lumière stellaire", Color(0xFFE6DCCB)), ColorOpt("Rose", Color(0xFFE7C3CB))),
            listOf(StorageOpt("42 mm", 0.0), StorageOpt("46 mm", 30.0)),
            listOf("Écran Retina toujours actif", "ECG et oxygène sanguin", "GPS", "Étanche 50 m", "Charge rapide")
        ),
        Product(
            9, "Apple Watch Ultra", Category.WATCH, 899.0,
            "Aventure sans limite.",
            "Boîtier en titane de 49 mm, GPS double fréquence, bouton Action et jusqu'à 36 heures d'autonomie.",
            "⌚", listOf(ColorOpt("Titane naturel", Color(0xFFB8B1A6)), ColorOpt("Titane noir", Color(0xFF3B3B3D))), single,
            listOf("Écran Retina 49 mm", "GPS double fréquence", "Étanche 100 m", "Sirène 86 dB", "Jusqu'à 36 h d'autonomie")
        ),
        Product(
            10, "AirPods Pro", Category.AUDIO, 279.0,
            "Réduction de bruit active.",
            "Un son spatial immersif, une réduction de bruit adaptative et un boîtier de charge MagSafe.",
            "🎧", listOf(ColorOpt("Blanc", Color(0xFFF5F5F7))), single,
            listOf("Réduction active du bruit", "Audio spatial personnalisé", "Boîtier MagSafe USB-C", "Jusqu'à 6 h d'écoute")
        ),
        Product(
            11, "AirPods Max", Category.AUDIO, 579.0,
            "Le son haute fidélité, en grand.",
            "Casque circum-aural avec coussinets en mousse à mémoire de forme, arceau en maille tricotée et 20 h d'autonomie.",
            "🎧", listOf(
                ColorOpt("Minuit", Color(0xFF2B2E36)), ColorOpt("Bleu", Color(0xFF9DB7D1)),
                ColorOpt("Mauve", Color(0xFFC9BFDB)), ColorOpt("Orange", Color(0xFFE9A77B))
            ), single,
            listOf("Réduction active du bruit", "Audio spatial", "Jusqu'à 20 h d'autonomie", "Connecteur USB-C")
        ),
        Product(
            12, "AirTag (x4)", Category.ACCESSORIES, 119.0,
            "Ne perdez plus rien.",
            "Retrouvez vos clés, votre sac ou vos bagages avec l'app Localiser. Lot de quatre.",
            "📍", listOf(ColorOpt("Argent", Color(0xFFD9D9DB))), single,
            listOf("Localisation précise", "Pile remplaçable", "Résistant à l'eau IP67", "Haut-parleur intégré")
        ),
        Product(
            13, "Chargeur MagSafe", Category.ACCESSORIES, 45.0,
            "Charge sans fil, bien alignée.",
            "Des aimants parfaitement alignés pour une charge sans fil jusqu'à 25 W avec un câble USB-C tressé de 1 m.",
            "🔌", listOf(ColorOpt("Blanc", Color(0xFFF5F5F7))), single,
            listOf("Charge sans fil jusqu'à 25 W", "Câble USB-C 1 m", "Compatible iPhone et AirPods")
        ),
        Product(
            14, "Apple Pencil Pro", Category.ACCESSORIES, 149.0,
            "Pensé pour créer.",
            "Retour haptique, geste de pincement, roulis du barillet et fonction Localiser intégrée.",
            "✏️", listOf(ColorOpt("Blanc", Color(0xFFF5F5F7))), single,
            listOf("Retour haptique", "Pression et inclinaison", "Recharge magnétique", "Compatible iPad Air et Pro")
        )
    )

    fun byId(id: Int): Product = products.first { it.id == id }
}

// ============================================================================
//  VIEWMODEL (état de la boutique)
// ============================================================================

class StoreViewModel : ViewModel() {
    val products: List<Product> = Catalog.products

    var query by mutableStateOf("")
    var category by mutableStateOf<Category?>(null)

    val cart = mutableStateListOf<CartItem>()
    val favorites = mutableStateListOf<Int>()
    val orders = mutableStateListOf<Order>()

    var promoInput by mutableStateOf("")
    var promoApplied by mutableStateOf(false)
    var promoError by mutableStateOf<String?>(null)

    private var orderCounter = 48200

    val filtered: List<Product>
        get() = products.filter { p ->
            (category == null || p.category == category) &&
                (query.isBlank() ||
                    p.name.contains(query, ignoreCase = true) ||
                    p.tagline.contains(query, ignoreCase = true))
        }

    val cartCount: Int get() = cart.sumOf { it.qty }
    val subtotal: Double get() = cart.sumOf { it.unitPrice * it.qty }
    val discount: Double get() = if (promoApplied) subtotal * 0.10 else 0.0
    val shipping: Double get() = if (cart.isEmpty() || subtotal - discount >= 50.0) 0.0 else 4.90
    val total: Double get() = subtotal - discount + shipping

    fun toggleFavorite(id: Int) {
        if (favorites.contains(id)) favorites.remove(id) else favorites.add(id)
    }

    fun addToCart(product: Product, color: ColorOpt, storage: StorageOpt) {
        val candidate = CartItem(product, color, storage, 1)
        val index = cart.indexOfFirst { it.key == candidate.key }
        if (index >= 0) cart[index] = cart[index].copy(qty = cart[index].qty + 1) else cart.add(candidate)
    }

    fun changeQty(key: String, delta: Int) {
        val index = cart.indexOfFirst { it.key == key }
        if (index < 0) return
        val newQty = cart[index].qty + delta
        if (newQty <= 0) cart.removeAt(index) else cart[index] = cart[index].copy(qty = newQty.coerceAtMost(10))
    }

    fun remove(key: String) {
        cart.removeAll { it.key == key }
    }

    fun applyPromo() {
        if (promoInput.trim().equals("APPLE10", ignoreCase = true)) {
            promoApplied = true
            promoError = null
        } else {
            promoApplied = false
            promoError = "Code promo invalide. Essayez APPLE10."
        }
    }

    fun removePromo() {
        promoApplied = false
        promoInput = ""
        promoError = null
    }

    fun placeOrder(customer: String, address: String, payment: String): Order {
        orderCounter++
        val order = Order(
            id = "W$orderCounter",
            items = cart.toList(),
            total = total,
            date = System.currentTimeMillis(),
            customer = customer,
            address = address,
            payment = payment
        )
        orders.add(0, order)
        cart.clear()
        removePromo()
        return order
    }
}

// ============================================================================
//  ACTIVITÉ + THÈME
// ============================================================================

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppleStoreTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AppRoot()
                }
            }
        }
    }
}

private val LightColors = lightColorScheme(
    primary = Color(0xFF0071E3),
    onPrimary = Color.White,
    background = Color(0xFFFBFBFD),
    onBackground = Color(0xFF1D1D1F),
    surface = Color.White,
    onSurface = Color(0xFF1D1D1F),
    surfaceVariant = Color(0xFFF2F2F4),
    onSurfaceVariant = Color(0xFF6E6E73),
    secondaryContainer = Color(0xFFE3EFFC),
    onSecondaryContainer = Color(0xFF0058B0),
    outline = Color(0xFFD2D2D7)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF2997FF),
    onPrimary = Color.White,
    background = Color(0xFF000000),
    onBackground = Color(0xFFF5F5F7),
    surface = Color(0xFF1C1C1E),
    onSurface = Color(0xFFF5F5F7),
    surfaceVariant = Color(0xFF2C2C2E),
    onSurfaceVariant = Color(0xFFA1A1A6),
    secondaryContainer = Color(0xFF16324F),
    onSecondaryContainer = Color(0xFF9CCBFF),
    outline = Color(0xFF3A3A3C)
)

@Composable
fun AppleStoreTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        content = content
    )
}

// ============================================================================
//  NAVIGATION RACINE
// ============================================================================

@Composable
fun AppRoot(vm: StoreViewModel = viewModel()) {
    var tab by rememberSaveable { mutableStateOf(Tab.SHOP) }
    var detailId by rememberSaveable { mutableStateOf<Int?>(null) }
    var inCheckout by rememberSaveable { mutableStateOf(false) }
    var confirmationId by rememberSaveable { mutableStateOf<String?>(null) }

    val confirmedOrder = vm.orders.firstOrNull { it.id == confirmationId }

    BackHandler(enabled = confirmedOrder != null) {
        confirmationId = null
        tab = Tab.SHOP
    }
    BackHandler(enabled = confirmedOrder == null && inCheckout) { inCheckout = false }
    BackHandler(enabled = confirmedOrder == null && !inCheckout && detailId != null) { detailId = null }

    when {
        confirmedOrder != null -> ConfirmationScreen(
            order = confirmedOrder,
            onContinue = {
                confirmationId = null
                tab = Tab.SHOP
            }
        )

        inCheckout -> CheckoutScreen(
            vm = vm,
            onBack = { inCheckout = false },
            onOrderPlaced = { order ->
                inCheckout = false
                confirmationId = order.id
            }
        )

        detailId != null -> DetailScreen(
            product = Catalog.byId(detailId!!),
            vm = vm,
            onBack = { detailId = null },
            onGoToCart = {
                detailId = null
                tab = Tab.CART
            }
        )

        else -> Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(tab.label, fontWeight = FontWeight.SemiBold) }
                )
            },
            bottomBar = {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    Tab.values().forEach { t ->
                        NavigationBarItem(
                            selected = tab == t,
                            onClick = { tab = t },
                            label = { Text(t.label, fontSize = 11.sp) },
                            icon = {
                                val icon = when (t) {
                                    Tab.SHOP -> Icons.Default.Home
                                    Tab.FAVS -> Icons.Default.Favorite
                                    Tab.CART -> Icons.Default.ShoppingCart
                                    Tab.ORDERS -> Icons.AutoMirrored.Filled.List
                                }
                                if (t == Tab.CART && vm.cartCount > 0) {
                                    BadgedBox(badge = { Badge { Text("${vm.cartCount}") } }) {
                                        Icon(icon, contentDescription = t.label)
                                    }
                                } else {
                                    Icon(icon, contentDescription = t.label)
                                }
                            }
                        )
                    }
                }
            }
        ) { padding ->
            Box(Modifier.padding(padding).fillMaxSize()) {
                when (tab) {
                    Tab.SHOP -> ShopScreen(vm, onOpen = { detailId = it })
                    Tab.FAVS -> FavoritesScreen(vm, onOpen = { detailId = it }, onBrowse = { tab = Tab.SHOP })
                    Tab.CART -> CartScreen(vm, onCheckout = { inCheckout = true }, onBrowse = { tab = Tab.SHOP })
                    Tab.ORDERS -> OrdersScreen(vm, onBrowse = { tab = Tab.SHOP })
                }
            }
        }
    }
}

// ============================================================================
//  ÉCRAN BOUTIQUE
// ============================================================================

@Composable
fun ShopScreen(vm: StoreViewModel, onOpen: (Int) -> Unit) {
    val items = vm.filtered
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            OutlinedTextField(
                value = vm.query,
                onValueChange = { vm.query = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Rechercher un produit") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (vm.query.isNotEmpty()) {
                        IconButton(onClick = { vm.query = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Effacer la recherche")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp)
            )
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = vm.category == null,
                        onClick = { vm.category = null },
                        label = { Text("Tout") }
                    )
                }
                items(Category.values().toList()) { c ->
                    FilterChip(
                        selected = vm.category == c,
                        onClick = { vm.category = if (vm.category == c) null else c },
                        label = { Text("${c.emoji} ${c.label}") }
                    )
                }
            }
        }

        if (vm.query.isBlank() && vm.category == null) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                HeroBanner(onClick = { onOpen(1) })
            }
        }

        if (items.isEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column(
                    Modifier.fillMaxWidth().padding(vertical = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Aucun résultat", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Essayez un autre mot-clé ou changez de catégorie.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        items(items, key = { it.id }) { p ->
            ProductCard(
                product = p,
                isFavorite = vm.favorites.contains(p.id),
                onFavorite = { vm.toggleFavorite(p.id) },
                onClick = { onOpen(p.id) }
            )
        }
    }
}

@Composable
fun HeroBanner(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF1D1D1F), Color(0xFF3A3A3D), Color(0xFF0071E3))))
            .clickable(onClick = onClick)
            .padding(24.dp)
    ) {
        Column {
            Text("iPhone 17 Pro", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text("Le plus puissant. Le plus pro.", color = Color(0xFFD2D2D7), fontSize = 15.sp)
            Spacer(Modifier.height(16.dp))
            Text(
                "À partir de ${1229.0.eur()}",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFF1D1D1F))
            ) { Text("Découvrir") }
        }
        Text("📱", fontSize = 84.sp, modifier = Modifier.align(Alignment.CenterEnd))
    }
}

@Composable
fun ProductCard(product: Product, isFavorite: Boolean, onFavorite: () -> Unit, onClick: () -> Unit) {
    val tint = product.colors.first().color
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.15f)
                    .background(Brush.verticalGradient(listOf(tint.copy(alpha = 0.35f), tint.copy(alpha = 0.08f)))),
                contentAlignment = Alignment.Center
            ) {
                Text(product.emoji, fontSize = 56.sp)
                IconButton(onClick = onFavorite, modifier = Modifier.align(Alignment.TopEnd)) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (isFavorite) "Retirer des favoris" else "Ajouter aux favoris",
                        tint = if (isFavorite) Color(0xFFFF375F) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Column(Modifier.padding(12.dp)) {
                Text(
                    product.name,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    product.tagline,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.height(32.dp)
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "Dès ${product.basePrice.eur()}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// ============================================================================
//  ÉCRAN FAVORIS
// ============================================================================

@Composable
fun FavoritesScreen(vm: StoreViewModel, onOpen: (Int) -> Unit, onBrowse: () -> Unit) {
    val favs = vm.products.filter { vm.favorites.contains(it.id) }
    if (favs.isEmpty()) {
        EmptyState(
            title = "Aucun favori",
            message = "Touchez le cœur d'un produit pour le retrouver ici.",
            action = "Parcourir la boutique",
            onAction = onBrowse
        )
        return
    }
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(favs, key = { it.id }) { p ->
            ProductCard(
                product = p,
                isFavorite = true,
                onFavorite = { vm.toggleFavorite(p.id) },
                onClick = { onOpen(p.id) }
            )
        }
    }
}

// ============================================================================
//  FICHE PRODUIT
// ============================================================================

@Composable
fun DetailScreen(product: Product, vm: StoreViewModel, onBack: () -> Unit, onGoToCart: () -> Unit) {
    var colorIndex by rememberSaveable(product.id) { mutableStateOf(0) }
    var storageIndex by rememberSaveable(product.id) { mutableStateOf(0) }
    val color = product.colors[colorIndex]
    val storage = product.storages[storageIndex]
    val price = product.basePrice + storage.extra

    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val isFavorite = vm.favorites.contains(product.id)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(product.name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    IconButton(onClick = { vm.toggleFavorite(product.id) }) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favori",
                            tint = if (isFavorite) Color(0xFFFF375F) else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = onGoToCart) {
                        if (vm.cartCount > 0) {
                            BadgedBox(badge = { Badge { Text("${vm.cartCount}") } }) {
                                Icon(Icons.Default.ShoppingCart, contentDescription = "Panier")
                            }
                        } else {
                            Icon(Icons.Default.ShoppingCart, contentDescription = "Panier")
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            Surface(shadowElevation = 8.dp, color = MaterialTheme.colorScheme.surface) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(price.eur(), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text("Livraison offerte", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Button(
                        onClick = {
                            vm.addToCart(product, color, storage)
                            scope.launch {
                                snackbar.currentSnackbarData?.dismiss()
                                snackbar.showSnackbar("${product.name} ajouté au panier")
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 28.dp, vertical = 14.dp)
                    ) { Text("Ajouter au panier", fontWeight = FontWeight.SemiBold) }
                }
            }
        }
    ) { padding ->
        Column(
            Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState())
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(color.color.copy(alpha = 0.45f), MaterialTheme.colorScheme.background)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(product.emoji, fontSize = 128.sp)
            }

            Column(Modifier.padding(horizontal = 20.dp)) {
                Text(product.name, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Text(
                    product.tagline,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(20.dp))

                Text("Couleur : ${color.name}", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    product.colors.forEachIndexed { i, c ->
                        val selected = i == colorIndex
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .border(
                                    width = if (selected) 3.dp else 1.dp,
                                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                    shape = CircleShape
                                )
                                .padding(5.dp)
                                .clip(CircleShape)
                                .background(c.color)
                                .clickable { colorIndex = i }
                        )
                    }
                }

                if (product.storages.size > 1) {
                    Spacer(Modifier.height(24.dp))
                    Text(
                        if (product.category == Category.WATCH) "Taille du boîtier" else "Stockage",
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(10.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(product.storages.size) { i ->
                            val s = product.storages[i]
                            FilterChip(
                                selected = i == storageIndex,
                                onClick = { storageIndex = i },
                                label = {
                                    Column(Modifier.padding(vertical = 6.dp)) {
                                        Text(s.label, fontWeight = FontWeight.Medium)
                                        Text(
                                            (product.basePrice + s.extra).eur(),
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
                Text("À propos", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))
                Text(
                    product.description,
                    lineHeight = 22.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(24.dp))
                Text("Caractéristiques", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                product.specs.forEach { spec ->
                    Row(Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(spec)
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

// ============================================================================
//  PANIER
// ============================================================================

@Composable
fun CartScreen(vm: StoreViewModel, onCheckout: () -> Unit, onBrowse: () -> Unit) {
    if (vm.cart.isEmpty()) {
        EmptyState(
            title = "Votre panier est vide",
            message = "Ajoutez un produit pour commencer votre commande.",
            action = "Parcourir la boutique",
            onAction = onBrowse
        )
        return
    }

    Column(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(vm.cart, key = { it.key }) { item ->
                CartRow(
                    item = item,
                    onPlus = { vm.changeQty(item.key, +1) },
                    onMinus = { vm.changeQty(item.key, -1) },
                    onRemove = { vm.remove(item.key) }
                )
            }

            item {
                PromoSection(vm)
            }

            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SummaryLine("Sous-total", vm.subtotal.eur())
                        if (vm.promoApplied) SummaryLine("Code APPLE10", "− ${vm.discount.eur()}", highlight = true)
                        SummaryLine("Livraison", if (vm.shipping == 0.0) "Offerte" else vm.shipping.eur())
                        HorizontalDivider()
                        SummaryLine("Total TTC", vm.total.eur(), bold = true)
                    }
                }
            }
        }

        Surface(shadowElevation = 8.dp, color = MaterialTheme.colorScheme.surface) {
            Button(
                onClick = onCheckout,
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(vertical = 14.dp)
            ) { Text("Passer la commande · ${vm.total.eur()}", fontWeight = FontWeight.SemiBold) }
        }
    }
}

@Composable
fun CartRow(item: CartItem, onPlus: () -> Unit, onMinus: () -> Unit, onRemove: () -> Unit) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(item.color.color.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) { Text(item.product.emoji, fontSize = 34.sp) }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(item.product.name, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                val variant = if (item.storage.label == "Standard") item.color.name else "${item.color.name}, ${item.storage.label}"
                Text(variant, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(4.dp))
                Text((item.unitPrice * item.qty).eur(), fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilledTonalIconButton(onClick = onMinus, modifier = Modifier.size(30.dp)) {
                        Text("−", fontSize = 16.sp)
                    }
                    Text(
                        "${item.qty}",
                        modifier = Modifier.width(36.dp),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.SemiBold
                    )
                    FilledTonalIconButton(onClick = onPlus, modifier = Modifier.size(30.dp)) {
                        Text("+", fontSize = 16.sp)
                    }
                }
            }

            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Delete, contentDescription = "Supprimer du panier")
            }
        }
    }
}

@Composable
fun PromoSection(vm: StoreViewModel) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = vm.promoInput,
                onValueChange = { vm.promoInput = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Code promo") },
                singleLine = true,
                enabled = !vm.promoApplied,
                isError = vm.promoError != null,
                shape = RoundedCornerShape(14.dp)
            )
            Spacer(Modifier.width(8.dp))
            if (vm.promoApplied) {
                OutlinedButton(onClick = { vm.removePromo() }) { Text("Retirer") }
            } else {
                Button(onClick = { vm.applyPromo() }, enabled = vm.promoInput.isNotBlank()) { Text("Appliquer") }
            }
        }
        vm.promoError?.let {
            Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp, start = 4.dp))
        }
        if (vm.promoApplied) {
            Text(
                "Code appliqué : 10 % de réduction",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
            )
        }
    }
}

@Composable
fun SummaryLine(label: String, value: String, bold: Boolean = false, highlight: Boolean = false) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
            label,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
            fontSize = if (bold) 17.sp else 15.sp,
            color = if (highlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
        Text(
            value,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Medium,
            fontSize = if (bold) 17.sp else 15.sp,
            color = if (highlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

// ============================================================================
//  PAIEMENT
// ============================================================================

@Composable
fun CheckoutScreen(vm: StoreViewModel, onBack: () -> Unit, onOrderPlaced: (Order) -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }
    var street by rememberSaveable { mutableStateOf("") }
    var zip by rememberSaveable { mutableStateOf("") }
    var city by rememberSaveable { mutableStateOf("") }
    var payment by rememberSaveable { mutableStateOf("Carte bancaire") }
    var cardNumber by rememberSaveable { mutableStateOf("") }
    var expiry by rememberSaveable { mutableStateOf("") }
    var cvv by rememberSaveable { mutableStateOf("") }
    var showErrors by rememberSaveable { mutableStateOf(false) }

    val cardDigits = cardNumber.filter { it.isDigit() }
    val errName = name.isBlank()
    val errStreet = street.isBlank()
    val errZip = zip.length != 5
    val errCity = city.isBlank()
    val isCard = payment == "Carte bancaire"
    val errCard = isCard && cardDigits.length != 16
    val errExpiry = isCard && !Regex("^(0[1-9]|1[0-2])/\\d{2}$").matches(expiry)
    val errCvv = isCard && cvv.length != 3
    val hasErrors = errName || errStreet || errZip || errCity || errCard || errExpiry || errCvv

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Paiement") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp, color = MaterialTheme.colorScheme.surface) {
                Button(
                    onClick = {
                        if (hasErrors) {
                            showErrors = true
                        } else {
                            val order = vm.placeOrder(
                                customer = name.trim(),
                                address = "${street.trim()}, $zip ${city.trim()}",
                                payment = payment
                            )
                            onOrderPlaced(order)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(vertical = 14.dp)
                ) { Text("Payer ${vm.total.eur()}", fontWeight = FontWeight.SemiBold) }
            }
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Livraison", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            FormField("Nom complet", name, { name = it }, showErrors && errName, "Saisissez votre nom.")
            FormField("Adresse", street, { street = it }, showErrors && errStreet, "Saisissez votre adresse.")
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FormField(
                    "Code postal", zip, { zip = it.filter(Char::isDigit).take(5) },
                    showErrors && errZip, "5 chiffres.",
                    keyboard = KeyboardType.Number, modifier = Modifier.weight(0.45f)
                )
                FormField(
                    "Ville", city, { city = it },
                    showErrors && errCity, "Saisissez la ville.",
                    modifier = Modifier.weight(0.55f)
                )
            }

            Spacer(Modifier.height(8.dp))
            Text("Moyen de paiement", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            listOf("Carte bancaire", "Apple Pay", "PayPal").forEach { option ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { payment = option }
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = payment == option, onClick = { payment = option })
                    Text(option)
                }
            }

            if (isCard) {
                FormField(
                    "Numéro de carte", cardNumber,
                    { input ->
                        val digits = input.filter(Char::isDigit).take(16)
                        cardNumber = digits.chunked(4).joinToString(" ")
                    },
                    showErrors && errCard, "16 chiffres.", keyboard = KeyboardType.Number
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FormField(
                        "Expiration (MM/AA)", expiry,
                        { input ->
                            val digits = input.filter(Char::isDigit).take(4)
                            expiry = if (digits.length > 2) digits.substring(0, 2) + "/" + digits.substring(2) else digits
                        },
                        showErrors && errExpiry, "Format MM/AA.",
                        keyboard = KeyboardType.Number, modifier = Modifier.weight(0.6f)
                    )
                    FormField(
                        "CVV", cvv, { cvv = it.filter(Char::isDigit).take(3) },
                        showErrors && errCvv, "3 chiffres.",
                        keyboard = KeyboardType.Number, modifier = Modifier.weight(0.4f)
                    )
                }
            }

            Text(
                "Paiement simulé : aucune donnée n'est envoyée ni enregistrée.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(8.dp))
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Récapitulatif (${vm.cartCount} article${if (vm.cartCount > 1) "s" else ""})", fontWeight = FontWeight.SemiBold)
                    SummaryLine("Sous-total", vm.subtotal.eur())
                    if (vm.promoApplied) SummaryLine("Code APPLE10", "− ${vm.discount.eur()}", highlight = true)
                    SummaryLine("Livraison", if (vm.shipping == 0.0) "Offerte" else vm.shipping.eur())
                    HorizontalDivider()
                    SummaryLine("Total TTC", vm.total.eur(), bold = true)
                }
            }
        }
    }
}

@Composable
fun FormField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    isError: Boolean,
    errorText: String,
    modifier: Modifier = Modifier,
    keyboard: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(label) },
        singleLine = true,
        isError = isError,
        supportingText = if (isError) ({ Text(errorText) }) else null,
        keyboardOptions = KeyboardOptions(keyboardType = keyboard),
        shape = RoundedCornerShape(12.dp)
    )
}

// ============================================================================
//  CONFIRMATION + COMMANDES
// ============================================================================

@Composable
fun ConfirmationScreen(order: Order, onContinue: () -> Unit) {
    Scaffold { padding ->
        Column(
            Modifier.padding(padding).fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF30D158),
                modifier = Modifier.size(88.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text("Commande confirmée", fontSize = 26.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text(
                "Merci ${order.customer.substringBefore(" ")} ! Votre commande ${order.id} sera livrée à :",
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Text(order.address, textAlign = TextAlign.Center, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(16.dp))
            Text("Total payé : ${order.total.eur()}", fontWeight = FontWeight.SemiBold)
            Text("Paiement : ${order.payment}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(32.dp))
            Button(
                onClick = onContinue,
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(horizontal = 32.dp, vertical = 14.dp)
            ) { Text("Continuer mes achats") }
        }
    }
}

@Composable
fun OrdersScreen(vm: StoreViewModel, onBrowse: () -> Unit) {
    if (vm.orders.isEmpty()) {
        EmptyState(
            title = "Aucune commande",
            message = "Vos commandes apparaîtront ici après votre premier achat.",
            action = "Parcourir la boutique",
            onAction = onBrowse
        )
        return
    }
    val format = remember { SimpleDateFormat("d MMM yyyy 'à' HH:mm", Locale.FRANCE) }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(vm.orders, key = { it.id }) { order ->
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Commande ${order.id}", fontWeight = FontWeight.Bold)
                        Text("En préparation", color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
                    }
                    Text(
                        format.format(Date(order.date)),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    HorizontalDivider(Modifier.padding(vertical = 4.dp))
                    order.items.forEach { item ->
                        Text("${item.qty} × ${item.product.name} (${item.color.name})", fontSize = 14.sp)
                    }
                    HorizontalDivider(Modifier.padding(vertical = 4.dp))
                    SummaryLine("Total", order.total.eur(), bold = true)
                }
            }
        }
    }
}

// ============================================================================
//  COMPOSANT COMMUN : ÉTAT VIDE
// ============================================================================

@Composable
fun EmptyState(title: String, message: String, action: String, onAction: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))
        Text(
            message,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(20.dp))
        TextButton(onClick = onAction) { Text(action) }
    }
}
