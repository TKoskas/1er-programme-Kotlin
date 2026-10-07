fun estPair(n: Int): Boolean {
    return n % 2 == 0
}

fun message(n: Int) {
    if (estPair(n)) {
        println("$n est pair")
    } else {
        println("$n est impair")
    }
}

fun main() {
    message(4)
    message(7)
}
