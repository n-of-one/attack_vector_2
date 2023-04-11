package org.n1.av2.backend.service.layerhacking.ice.wordsearch

import org.n1.av2.backend.entity.site.enums.IceStrength
import kotlin.random.Random

class WordSearchCreation(
    val words: List<String>,
    val letters: List<List<Char>>
)
const val MAX_ATTEMPTS = 1000

class WordSearchCreator(private val strength: IceStrength) {

    private val wordSearchWordList = WordSearchWordList()

    private val letterGrid: MutableList<MutableList<Char>>
    private val usedGrid: MutableList<MutableList<Boolean>>

    private val sizeX = sizeX(strength)
    private val sizeY = sizeY(strength)


    init {
        letterGrid = createGrid { randomLetter() }
        usedGrid = createGrid { false }
    }

    fun create(): WordSearchCreation {
        val words = wordSearchWordList.getWords(strength)
        addHeader()

        words.forEach { word ->
            fitWord(word)
        }

        return WordSearchCreation(words, letterGrid)
    }

    private fun fitWord(word: String) {
        repeat(MAX_ATTEMPTS) {
            val x = Random.nextInt(sizeX)
            val y = Random.nextInt(sizeY)

            if (attemptFitWord(word, x, y)) {
                return
            }
        }
        error("Failed to fit word: ${word} after ${MAX_ATTEMPTS}")
    }

    private fun attemptFitWord(word: String, x: Int, y: Int): Boolean {
        if (usedGrid[y][x]) {
            return false
        }

        var direction = Random.nextInt(8)
        repeat(8) {
            if (attemptFitWordInDirection(word, x, y, direction)) {
                return true
            }
            direction = (direction + 1) % 8
        }
        return false
    }

    private fun attemptFitWordInDirection(word: String, x: Int, y: Int, direction: Int): Boolean {

        val directionVector = directionVector(direction)
        val dx = directionVector.first
        val dy = directionVector.second

        if (outOfBound(x, y, dx, dy, word.length)) return false

        var currentX = x
        var currentY = y
        for (letter in word) {
            if (usedGrid[currentY][currentX] && letterGrid[currentY][currentX] != letter) {
                return false
            }
            currentX += dx
            currentY += dy
        }

        currentX = x
        currentY = y
        for (letter in word) {
            usedGrid[currentY][currentX] = true
            letterGrid[currentY][currentX] = letter
            currentX += dx
            currentY += dy
        }

        return true
    }

    private fun outOfBound(x: Int, y: Int, dx: Int, dy: Int, length: Int): Boolean {
        val endX = x + dx * length
        val endY = y + dy * length
        return endX < 0 || endX >= letterGrid[0].size || endY < 0 || endY >= letterGrid.size
    }

    private fun <T> createGrid(creatorFunction: () -> T): MutableList<MutableList<T>> {
        val letterGrid = mutableListOf<MutableList<T>>()
        for (y in 0..sizeY) {
            val row = mutableListOf<T>()
            for (x in 0..sizeX) {
                row.add(creatorFunction())
            }
            letterGrid.add(row)
        }

        return letterGrid
    }

    private fun directionVector(direction: Int): Pair<Int, Int> {
        return when (direction) {
            0 -> Pair(1, 0)
            1 -> Pair(1, 1)
            2 -> Pair(0, 1)
            3 -> Pair(-1, 1)
            4 -> Pair(-1, 0)
            5 -> Pair(-1, -1)
            6 -> Pair(0, -1)
            7 -> Pair(1, -1)
            else -> error("Unknown direction: $direction")
        }
    }

    private fun randomLetter(): Char {
        return Char('A'.code + Random.nextInt(26))
    }

    private fun sizeX(strength: IceStrength): Int {
        return when (strength) {
            IceStrength.VERY_WEAK -> 10
            IceStrength.WEAK -> 15
            IceStrength.AVERAGE -> 20
            IceStrength.STRONG -> 30
            IceStrength.VERY_STRONG -> 40
            IceStrength.IMPENETRABLE -> 50
            else -> error("Unknown ice strength: $strength")
        }
    }

    private fun sizeY(strength: IceStrength): Int {
        return when (strength) {
            IceStrength.VERY_WEAK -> 10
            IceStrength.WEAK -> 15
            IceStrength.AVERAGE -> 20
            IceStrength.STRONG -> 22
            IceStrength.VERY_STRONG -> 24
            IceStrength.IMPENETRABLE -> 28
            else -> error("Unknown ice strength: $strength")
        }
    }

    private fun addHeader() {
        var x = Random.nextInt(sizeX)
        var y = 0
        "____inject{@authorize(92), @c_override(\"header\", \"trusted/1\"), @remote_core_dump_analyze()}___".forEach { letter ->
            letterGrid[y][x] = letter
            usedGrid[y][x] = true
            x++
            if (x >= sizeX) {
                x = 0
                y++
            }
        }
    }
}
