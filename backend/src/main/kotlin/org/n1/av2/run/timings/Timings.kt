package org.n1.av2.run.timings

/**
 * This class is used to convey to the UI how many ticks an action will take.
 * The idea is that the same ticks are used in the backend to schedule an event on the timed event queue, as are used in the FE animations.
 *
 * This class acts as a usability improvement to be able to nicer express ticks timing
 */
class Timings(vararg entries: Pair<String, Int>) : HashMap<String, Int>() {

    init {
        entries.forEach {
            this[it.first] = it.second
        }
    }

    val totalTicks: Int
        get() {
            return this.values.sum()
        }

    fun ticksFor(key: String): Int {
        return entries.find { it.key == key }?.value ?: error("key not found: ${key}")
    }

    val totalWithoutConnection: Int
        get() {
            val connectionTicks = this.get("connections") ?: 0
            return totalTicks - connectionTicks
        }

    val connection: Int
        get() {
            return this.get("connection") ?: error("No timining defined for: connection")
        }

    fun adjustedForSpeed(factor: Double): Timings {
        val newEntries = entries.toList().map { (key, value) -> Pair(key, (value * factor).toInt()) }.toTypedArray()
        return Timings(*newEntries)
    }
}
