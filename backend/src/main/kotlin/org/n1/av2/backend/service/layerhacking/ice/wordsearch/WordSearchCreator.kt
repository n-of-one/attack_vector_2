package org.n1.av2.backend.service.layerhacking.ice.wordsearch

import org.n1.av2.backend.entity.site.enums.IceStrength
import kotlin.random.Random



class WordSearchCreation(
    val words: List<String>,
    val letters: List<List<Char>>,
    val solutions: List<List<String>>,
    val score: Int,
)

class WordSearchCreator(private val strength: IceStrength) {

    companion object {
        const val MAX_ATTEMPTS = 1000
        const val SCORE_FOR_OVERLAP = 30
        const val SCORE_FOR_DIAGONAL = 10
    }

    private val wordSearchWordList = WordSearchWordList()

    private val letterGrid: MutableList<MutableList<Char>>
    private val usedGrid: MutableList<MutableList<Boolean>>

    private val sizeX = sizeX(strength)
    private val sizeY = sizeY(strength)

    private var diagonalSolutions = 0
    private var words: List<String> = listOf()
    private var score = 0


    init {
        letterGrid = createGrid { randomLetter() }
        usedGrid = createGrid { false }
    }

    fun create(): WordSearchCreation {
        words = wordSearchWordList.getWords(strength)
        addHeader()

        val solutions: MutableList<List<String>> = mutableListOf()

        words
            .sorted() // start with shorter words to try not to divide the grid into sectors from the start
            .forEach { word ->
                val solution = fitWord(word)
                solutions.add(solution)
            }

        return WordSearchCreation(words, letterGrid, solutions, score)
    }

    private fun fitWord(word: String): List<String> {
        repeat(MAX_ATTEMPTS) {
            val x = Random.nextInt(sizeX)
            val y = Random.nextInt(sizeY)

            val solution = attemptFitWord(word, x, y)
            if (solution != null) return solution
        }
        error("Failed to fit word: ${word} after ${MAX_ATTEMPTS}")
    }

    private fun attemptFitWord(word: String, x: Int, y: Int): List<String>? {
        if (usedGrid[y][x]) {
            return null
        }

        var direction = Random.nextInt(4)
        if (diagonalSolutions <= (this.words.size / 2)+ 1) {
            val solution = attemptFitWordInDirectionType(direction, word, x, y) { direction: Int ->
                directionVectorDiagonal(direction)
            }
            if (solution != null) {
                this.diagonalSolutions++
                this.score += SCORE_FOR_DIAGONAL
                return solution
            }
        }

        direction = Random.nextInt(4)
        val solution = attemptFitWordInDirectionType(direction, word, x, y) { direction: Int ->
            directionVectorHorizontalVertical(direction)
        }
        return solution
    }

    private fun attemptFitWordInDirectionType(
        directionStart: Int, word: String, x: Int, y: Int,
        directionFunction: (d: Int) -> Pair<Int, Int>
    ): List<String>? {
        var direction = directionStart
        repeat(4) {
            val directionVector = directionFunction(direction)
            val solution = attemptFitWordInDirection(word, x, y, directionVector.first, directionVector.second)
            if (solution != null) return solution
            direction = (direction + 1) % 4
        }
        return null
    }

    private fun attemptFitWordInDirection(word: String, x: Int, y: Int, dx: Int, dy: Int): List<String>? {
        if (outOfBound(x, y, dx, dy, word.length)) return null

        var scoreForSolution = 0

        val solution = mutableListOf<String>()
        var currentX = x
        var currentY = y
        for (letter in word) {
            if (usedGrid[currentY][currentX]) {
                if (letterGrid[currentY][currentX] != letter) return null
                scoreForSolution += SCORE_FOR_OVERLAP
            }

            currentX += dx
            currentY += dy
        }

        currentX = x
        currentY = y
        for (letter in word) {
            usedGrid[currentY][currentX] = true
            letterGrid[currentY][currentX] = letter
            solution.add("${currentX}:${currentY}")
            currentX += dx
            currentY += dy
        }

        this.score += scoreForSolution
        return solution
    }

    private fun outOfBound(x: Int, y: Int, dx: Int, dy: Int, length: Int): Boolean {
        val endX = x + dx * length
        val endY = y + dy * length
        return endX < 0 || endX >= letterGrid[0].size || endY < 0 || endY >= letterGrid.size
    }

    private fun <T> createGrid(creatorFunction: () -> T): MutableList<MutableList<T>> {
        val letterGrid = mutableListOf<MutableList<T>>()
        for (y in 1..sizeY) {
            val row = mutableListOf<T>()
            for (x in 1..sizeX) {
                row.add(creatorFunction())
            }
            letterGrid.add(row)
        }

        return letterGrid
    }

    private fun directionVectorDiagonal(direction: Int): Pair<Int, Int> {
        return when (direction) {
            0 -> Pair(1, 1)
            1 -> Pair(-1, 1)
            2 -> Pair(-1, -1)
            3 -> Pair(1, -1)
            else -> error("Unknown direction: $direction")
        }
    }

    private fun directionVectorHorizontalVertical(direction: Int): Pair<Int, Int> {
        return when (direction) {
            0 -> Pair(1, 0)
            1 -> Pair(0, 1)
            2 -> Pair(-1, 0)
            3 -> Pair(0, -1)
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
        var y = 2
        "____inject{@authorize(92), @remote_core_dump_analyze()}___".forEach { letter ->
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
