fun multiplier(a: Int, b: Int) = a * b

fun estPair(n: Int) = n % 2 == 0

fun afficherTable(n: Int) {
    for (i in 1..10) {
        val resultat = multiplier(n, i)
        if (estPair(resultat)) {
            println("$n x $i = $resultat (pair)")
        } else {
            println("$n x $i = $resultat (impair)")
        }
    }
}

fun main() {
    afficherTable(7)
}
