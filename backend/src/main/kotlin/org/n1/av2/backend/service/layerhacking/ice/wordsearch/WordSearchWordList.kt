package org.n1.av2.backend.service.layerhacking.ice.wordsearch

import org.n1.av2.backend.entity.site.enums.IceStrength
import kotlin.random.Random


class WordSearchWordList {

    companion object {
        const val MAX_ATTEMPTS = 100
    }

    private val wordsBySize: Map<Int, List<String>>

    init {
        val words = WordSearchWordList::class.java.getResourceAsStream("/ice/word_search/wordlist.txt")
            .bufferedReader()
            .readLines()
            .map { it.uppercase() }

        wordsBySize = words
            .filter { it.length in 3..12 }
            .groupBy { it.length }
    }

    fun getWords(strength: IceStrength): List<String> {
        val minLength = minLength(strength)
        val maxLength = maxLength(strength)
        val wordCount = wordCount(strength)

        val words = mutableListOf<String>()
        repeat(wordCount) {
            addWord(words, minLength, maxLength)
        }

        return words
    }

    private fun addWord(words: MutableList<String>, minLength: Int, maxLength: Int) {
        val wordSize = Random.nextInt(1 + maxLength - minLength) + minLength
        val wordsInThatSize = this.wordsBySize[wordSize] ?: error("No words of size ${wordSize}")
        repeat(MAX_ATTEMPTS) {
            val word = wordsInThatSize.random()
            if (word !in words) {
                words.add(word)
                return
            }
        }
        error("Failed to add word after ${MAX_ATTEMPTS} attempts, word size: ${wordSize}")
    }

    private fun minLength(strength: IceStrength): Int {
        return when (strength) {
            IceStrength.VERY_WEAK -> 6
            IceStrength.WEAK -> 5
            IceStrength.AVERAGE -> 4
            IceStrength.STRONG -> 3
            IceStrength.VERY_STRONG -> 3
            IceStrength.ONYX -> 3
        }
    }

    private fun maxLength(strength: IceStrength): Int {
        return when (strength) {
            IceStrength.VERY_WEAK -> 8
            IceStrength.WEAK -> 12
            IceStrength.AVERAGE -> 12
            IceStrength.STRONG -> 10
            IceStrength.VERY_STRONG -> 8
            IceStrength.ONYX -> 6
        }
    }

    private fun wordCount(strength: IceStrength): Int {
        return when (strength) {
            IceStrength.VERY_WEAK -> 5
            IceStrength.WEAK -> 7
            IceStrength.AVERAGE -> 8
            IceStrength.STRONG -> 15
            IceStrength.VERY_STRONG -> 20
            IceStrength.ONYX -> 25
        }
    }


}