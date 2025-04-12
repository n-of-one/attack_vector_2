package org.n1.av2.script.effect.positive.ice

import org.n1.av2.hacker.hackerstate.HackerStateRunning
import org.n1.av2.layer.ice.common.IceLayer
import org.n1.av2.layer.ice.wordsearch.WordSearchIceStatusRepo
import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.script.effect.ScriptEffectInterface
import org.n1.av2.script.effect.ScriptExecution
import org.n1.av2.script.effect.helper.IceEffectHelper
import org.n1.av2.script.type.ScriptEffect
import org.n1.av2.site.entity.enums.LayerType
import org.springframework.stereotype.Service

/**
 * Linked type:
 * @see org.n1.av2.script.effect.ScriptEffectType.WORD_SEARCH_NEXT_WORDS
 */
@Service
class WordSearchNextWordsEffectService(
    private val iceEffectHelper: IceEffectHelper,
    private val connectionService: ConnectionService,
    private val wordSearchIceStatusRepo: WordSearchIceStatusRepo,
) : ScriptEffectInterface {


    override val name = "Word search show next words"
    override val defaultValue = "5"
    override val gmDescription = "Show next X words in word search ICE."

    override fun playerDescription(effect: ScriptEffect) = "Show next ${effect.value} words in Jaal ICE (word search)."

    override fun validate(effect: ScriptEffect) = ScriptEffectInterface.validateIntegerGreaterThanZero(effect)

    override fun prepareExecution(effect: ScriptEffect, argumentTokens: List<String>, hackerState: HackerStateRunning): ScriptExecution {
        return iceEffectHelper.runForSpecificIceType(LayerType.WORD_SEARCH_ICE, argumentTokens, hackerState) { layer: IceLayer ->
            ScriptExecution {
                val iceStatus = wordSearchIceStatusRepo.findByLayerId(layer.id) ?: error("Failed to instantiate ICE for: ${layer.id}")
                val wordsLeft = iceStatus.words.drop(iceStatus.wordIndex)
                val wordsToShow = wordsLeft.take(effect.value!!.toInt())

                connectionService.replyTerminalReceive("The next words are:")
                wordsToShow.forEach {
                    connectionService.replyTerminalReceive("- $it")
                }
            }
        }
    }
}
