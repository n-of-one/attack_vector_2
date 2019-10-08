package org.n1.av2.backend.model

/**
 * This class is used to convey to the UI how many ticks an action will take.
 * The idea is that the same ticks are used in the backend to schedule an event on the timed event queue, as are used in the FE animations.
 *
 * This class acts as a usability improvement to be able to nicer express ticks timing
 */
class Ticks(vararg entries: Pair<String, Int>) : HashMap<String, Int>() {

    init {
        entries.forEach {
            this[it.first] = it.second
        }
    }

    val total: Int
    get() {
        return this.values.sum()
    }

}