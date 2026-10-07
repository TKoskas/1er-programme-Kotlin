fun multiplier(a: Int, b: Int) = a * b

fun afficherTable(n: Int) {
    for (i in 1..10) {
        println("$n x $i = ${multiplier(n, i)}")
    }
}

fun main() {
    afficherTable(7)
    afficherTable(9)
}
