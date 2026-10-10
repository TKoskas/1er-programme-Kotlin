
fun main() {
    println("Bonjour !")
    println("J'apprends Kotlin.")
    
    val age = 24
    println("J'ai $age ans.")
     jour()
}


fun jour() {
    val jour = 2

    when (jour) {
        1 -> println("Lundi")
        2 -> println("Mardi")
        3 -> println("Mercredi")
    }
}
