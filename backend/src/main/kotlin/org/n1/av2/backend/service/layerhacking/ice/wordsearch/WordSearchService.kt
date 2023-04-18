package org.n1.av2.backend.service.layerhacking.ice.wordsearch

import org.n1.av2.backend.entity.ice.WordSearchStatus
import org.n1.av2.backend.entity.ice.WordSearchStatusRepo
import org.n1.av2.backend.entity.site.enums.IceStrength
import org.n1.av2.backend.entity.site.layer.IceWordSearchLayer
import org.n1.av2.backend.model.ui.NotyMessage
import org.n1.av2.backend.model.ui.NotyType
import org.n1.av2.backend.model.ui.ServerActions
import org.n1.av2.backend.service.StompService
import org.n1.av2.backend.service.layerhacking.HackedUtil
import org.n1.av2.backend.util.createId
import org.springframework.stereotype.Service
import kotlin.jvm.optionals.getOrElse
import kotlin.system.measureTimeMillis


@Service
class WordSearchService(
    private val wordSearchStatusRepo: WordSearchStatusRepo,
    private val stompService: StompService,
    val hackedUtil: HackedUtil,
) {

    private val logger = mu.KotlinLogging.logger {}

    companion object {
        const val CREATION_ATTEMPTS = 50
    }

    fun createIce(layer: IceWordSearchLayer, nodeId: String, runId: String): WordSearchStatus {
        val creation = findBestCreation(layer.strength)

        val id = createId("wordSearch", wordSearchStatusRepo::findById)
        val wordSearchStatus = WordSearchStatus(
            id = id,
            runId = runId,
            nodeId = nodeId,
            layerId = layer.id,
            strength = layer.strength,
            words = creation.words,
            letterGrid = creation.letterGrid,
            correctPositions = emptyList(),
            hacked = false,
            solutions = creation.solutions,
            wordIndex = 0
        )
        wordSearchStatusRepo.save(wordSearchStatus)
        return wordSearchStatus
    }

    fun findBestCreation(strength: IceStrength): WordSearchCreation {
        var creation: WordSearchCreation
        measureTimeMillis {
            creation = (1..CREATION_ATTEMPTS).map {
                WordSearchCreator(strength).create()
            }
                .filterNotNull()
                .ifEmpty { error("Failed to create ice") }
                .sortedBy { it.score }
                .onEach { println(it.score) }
                .last()
        }.run {
            logger.debug("Creating word search ice ${strength} took ${this} ms")
        }
        return creation
    }

    fun enter(iceId: String) {
        val iceStatus = wordSearchStatusRepo.findById(iceId).getOrElse { error("No Word search ice for ID: ${iceId}") }
        if (iceStatus.hacked) error("This ice has already been hacked.")
        stompService.reply(ServerActions.SERVER_ENTER_ICE_WORD_SEARCH, iceStatus)
    }

    class WordSearchUpdate(
        val iceId: String,
        val wordIndex: Int,
        val lettersCorrect: List<String>, val hacked: Boolean
    )

    fun selected(iceId: String, letters: List<String>) {
        val iceStatus = wordSearchStatusRepo.findById(iceId).getOrElse { error("Ice not found for \"${iceId}\"") }
        if (iceStatus.hacked) return
        val wordSelected = determineWordSelected(letters, iceStatus.letterGrid)
        val nexWord = iceStatus.words[iceStatus.wordIndex]

        if (wordSelected != nexWord && wordSelected.reversed() != nexWord) return

        val hacked = (iceStatus.wordIndex == iceStatus.words.size - 1)
        val nextIndex = if (hacked) -1 else iceStatus.wordIndex + 1
        val lettersCorrect = iceStatus.correctPositions + letters
        val newIceStatus = iceStatus.copy(
            correctPositions = lettersCorrect,
            wordIndex = nextIndex,
            hacked = hacked
        )
        wordSearchStatusRepo.save(newIceStatus)

        val updateMessage = WordSearchUpdate(iceStatus.id, nextIndex, letters, hacked)
        stompService.toIce(iceStatus.id, ServerActions.SERVER_ICE_WORD_SEARCH_UPDATED, updateMessage)
        if (hacked) {
            hackedUtil.iceHacked(iceStatus.layerId, iceStatus.runId, 70)
        }
    }

    private fun determineWordSelected(letterCoordinates: List<String>, lettersGrid: List<List<Char>>): String {
        val builder = StringBuilder()
        letterCoordinates.forEach { letterCoordinate ->
            val (x, y) = letterCoordinate.split(":").map { it.toInt() }
            builder.append(lettersGrid[y][x])
        }
        return builder.toString()
    }
}