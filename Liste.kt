
fun main() {
    val jeux = listOf("Minecraft", "Mario", "Zelda")

    println(jeux)
}

fun main() {
    val jeux = mutableListOf("Minecraft", "Mario")

    jeux.add("Zelda")

    println(jeux)
}

fun main() {
    val jeux = listOf("Minecraft", "Mario", "Zelda")

    for (jeu in jeux) {
        println(jeu)
    }
}
