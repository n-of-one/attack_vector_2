package org.n1.av2.layer.ice.wordsearch

import org.n1.av2.layer.ice.HackedUtil
import org.n1.av2.platform.config.ConfigItem
import org.n1.av2.platform.config.ConfigService
import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.platform.connection.ServerActions
import org.n1.av2.platform.util.createId
import org.n1.av2.run.RunService
import org.n1.av2.site.entity.enums.IceStrength
import org.springframework.stereotype.Service
import kotlin.jvm.optionals.getOrElse
import kotlin.system.measureTimeMillis


@Service
class WordSearchService(
    private val wordSearchIceStatusRepo: WordSearchIceStatusRepo,
    private val connectionService: ConnectionService,
    private val hackedUtil: HackedUtil,
    private val runService: RunService,
    private val configService: ConfigService,
    ) {

    private val logger = mu.KotlinLogging.logger {}

    companion object {
        const val CREATION_ATTEMPTS = 50
    }

    fun findOrCreateIceByLayerId(layer: WordSearchIceLayer): WordSearchIceStatus {
        return wordSearchIceStatusRepo.findByLayerId(layer.id) ?: createIce(layer)
    }

    fun createIce(layer: WordSearchIceLayer): WordSearchIceStatus {
        val creation = findBestCreation(layer.strength)

        val id = createId("wordSearch", wordSearchIceStatusRepo::findById)
        val wordSearchIceStatus = WordSearchIceStatus(
            id = id,
            layerId = layer.id,
            strength = layer.strength,
            words = creation.words,
            letterGrid = creation.letterGrid,
            correctPositions = emptyList(),
            hacked = false,
            solutions = creation.solutions,
            wordIndex = 0
        )
        wordSearchIceStatusRepo.save(wordSearchIceStatus)
        return wordSearchIceStatus
    }

    fun findBestCreation(strength: IceStrength): WordSearchCreation {
        var creation: WordSearchCreation
        measureTimeMillis {
            creation = (1..CREATION_ATTEMPTS).mapNotNull {
                WordSearchCreator(strength).create()
            }
                .ifEmpty { error("Failed to create ice") }
                .sortedBy { it.score }
                .onEach { println(it.score) }
                .last()
        }.run {
            logger.debug { "Creating word search ice ${strength} took ${this} ms" }
        }
        return creation
    }

    @Suppress("unused")
    class EnterWordSearch(
        val layerId: String,
        val strength: IceStrength,
        val letterGrid: List<List<Char>>,
        val words: List<String>,
        val correctPositions: List<String>,
        val solutions: List<List<String>>,
        val wordIndex: Int,
        val quickPlaying: Boolean,
    )

    fun enter(iceId: String) {
        val iceStatus = wordSearchIceStatusRepo.findById(iceId).getOrElse { error("No Word search ice for ID: ${iceId}") }
        val enterWordSearch = EnterWordSearch(
            iceStatus.layerId,
            iceStatus.strength,
            iceStatus.letterGrid,
            iceStatus.words,
            iceStatus.correctPositions,
            iceStatus.solutions,
            iceStatus.wordIndex,
            configService.getAsBoolean(ConfigItem.DEV_QUICK_PLAYING)
        )
        connectionService.reply(ServerActions.SERVER_WORD_SEARCH_ENTER, enterWordSearch)
        runService.enterNetworkedApp(iceId)
    }

    @Suppress("unused")
    class WordSearchUpdate(val iceId: String, val wordIndex: Int, val lettersCorrect: List<String>)

    fun selected(iceId: String, letters: List<String>) {
        val iceStatus = wordSearchIceStatusRepo.findById(iceId).getOrElse { error("Ice not found for \"${iceId}\"") }
        if (iceStatus.hacked) return
        val wordSelected = determineWordSelected(letters, iceStatus.letterGrid)
        val nexWord = iceStatus.words[iceStatus.wordIndex]

        if (wordSelected != nexWord && wordSelected.reversed() != nexWord) return

        val hacked = (iceStatus.wordIndex == iceStatus.words.size - 1)
        val nextIndex = iceStatus.wordIndex + 1
        val lettersCorrect = iceStatus.correctPositions + letters
        val newIceStatus = iceStatus.copy(
            correctPositions = lettersCorrect,
            wordIndex = nextIndex,
            hacked = hacked
        )
        wordSearchIceStatusRepo.save(newIceStatus)

        val updateMessage = WordSearchUpdate(iceStatus.id, nextIndex, letters)
        connectionService.toIce(iceStatus.id, ServerActions.SERVER_WORD_SEARCH_UPDATED, updateMessage)

        if (hacked) {
            hackedUtil.iceHacked(iceId, iceStatus.layerId, 70)
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

    fun deleteByLayerId(layerId: String) {
        val iceStatus = wordSearchIceStatusRepo.findByLayerId(layerId) ?: return
        wordSearchIceStatusRepo.delete(iceStatus)
    }
}
