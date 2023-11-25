import java.math.BigDecimal

fun deterministicShuffle(numbers: MutableList<Int>, seed: Int) {
    val size = numbers.size
    val fib = mutableListOf(BigDecimal.ONE, BigDecimal.ONE)
    while (fib.size < size) {
        val next = fib.last() + fib[fib.size - 2]
        println(next)
        fib.add(next)
    }

    val indices = mutableListOf<Int>()
    for (i in 0 until size) {
        val index = (BigDecimal(i) + fib[(i + seed) % size]) .remainder( size.toBigDecimal())

        indices.add(index.toInt())
    }
    for (i in 0 until size) {
        val j = indices[i]
        val temp = numbers[i]
        numbers[i] = numbers[j]
        numbers[j] = temp
    }
}

fun main() {
    val numbers = (1..10).toMutableList()
    deterministicShuffle(numbers, 12345) // Replace 12345 with any integer seed

    println("---")

    for (x in numbers) {
        println(x)
    }
}

