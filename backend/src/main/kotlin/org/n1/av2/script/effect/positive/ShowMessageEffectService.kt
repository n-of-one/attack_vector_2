package org.n1.av2.script.effect.positive

import org.n1.av2.hacker.hackerstate.HackerStateRunning
import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.script.effect.ScriptEffectInterface
import org.n1.av2.script.effect.ScriptExecution
import org.n1.av2.script.type.ScriptEffect
import org.springframework.stereotype.Service

/**
 * Linked type:
 * @see org.n1.av2.script.effect.ScriptEffectType.SHOW_MESSAGE
 */
@Service
class ShowMessageEffectService(
    private val connectionService: ConnectionService,
    ) : ScriptEffectInterface {

    override val name = "Show Text"
    override val defaultValue = ""
    override val gmDescription = "Show a message to the hacker when run."

    override fun playerDescription(effect: ScriptEffect) = "Unknown effect"

    override fun validate(effect: ScriptEffect) = ScriptEffectInterface.validateNonEmptyText(effect)

    override fun prepareExecution(effect: ScriptEffect, argumentTokens: List<String>, hackerState: HackerStateRunning): ScriptExecution {
        return ScriptExecution {
            connectionService.replyTerminalReceive(effect.value!!)
        }
    }

}
