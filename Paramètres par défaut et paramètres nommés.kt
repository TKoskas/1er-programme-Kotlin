
fun creerProfil(
    prenom: String,
    age: Int,
    ville: String
) {
    println("$prenom, $age ans, habite à $ville.")
}

fun main() {
    creerProfil(
        prenom = "Thomas",
        age = 24,
        ville = "Paris"
    )
}
