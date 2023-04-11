package org.n1.av2.backend.service.layerhacking.ice.wordsearch

import org.n1.av2.backend.entity.ice.WordSearchStatus
import org.n1.av2.backend.entity.ice.WordSearchStatusRepo
import org.n1.av2.backend.entity.site.layer.IceWordSearchLayer
import org.n1.av2.backend.model.ui.NotyMessage
import org.n1.av2.backend.model.ui.NotyType
import org.n1.av2.backend.model.ui.ServerActions
import org.n1.av2.backend.service.StompService
import org.n1.av2.backend.service.layerhacking.HackedUtil
import org.n1.av2.backend.util.createId
import org.springframework.stereotype.Service
import kotlin.jvm.optionals.getOrElse

@Service
class IceWordSearchService(
    private val wordSearchStatusRepo: WordSearchStatusRepo,
    private val stompService: StompService,
    val hackedUtil: HackedUtil,

    ) {

    fun createIce(layer: IceWordSearchLayer, nodeId: String, runId: String): WordSearchStatus {
        val creation = WordSearchCreator(layer.strength).create()

        val id = createId("wordSearch", wordSearchStatusRepo::findById)
        val wordSearchStatus = WordSearchStatus(
            id = id,
            runId = runId,
            nodeId = nodeId,
            layerId = layer.id,
            strength = layer.strength,
            words = creation.words,
            letters = creation.letters,
            lettersCorrect = emptyList(),
            hacked = false
        )
        wordSearchStatusRepo.save(wordSearchStatus)
        return wordSearchStatus
    }

    fun enter(iceId: String) {
        val iceStatus = wordSearchStatusRepo
            .findById(iceId)
            .getOrElse {
                stompService.replyMessage(NotyMessage(NotyType.FATAL, "Error", "No ice for ID: ${iceId}"))
                return
            }

        stompService.reply(ServerActions.SERVER_ENTER_ICE_WORD_SEARCH, iceStatus)
    }

    fun selected(iceId: String, letters: List<String>) {
        val iceStatus = wordSearchStatusRepo.findById(iceId).getOrElse { error("Ice not found for \"${iceId}\"") }
        if (iceStatus.hacked) return
        val wordSelected = determineWordSelected(letters, iceStatus.letters)
        val nexWord = iceStatus.words[iceStatus.wordIndex]

        if (wordSelected != nexWord && wordSelected.reversed() != nexWord) return

        val hacked =  (iceStatus.wordIndex == iceStatus.words.size - 1)
        val nextIndex = if (hacked) -1 else iceStatus.wordIndex + 1
        val lettersCorrect = iceStatus.lettersCorrect + letters
        val newIceStatus = iceStatus.copy(
            lettersCorrect = lettersCorrect,
            wordIndex = nextIndex,
            hacked = hacked
        )
        wordSearchStatusRepo.save(newIceStatus)

        stompService.reply(ServerActions.SERVER_ICE_WORD_SEARCH_UPDATED, newIceStatus)
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