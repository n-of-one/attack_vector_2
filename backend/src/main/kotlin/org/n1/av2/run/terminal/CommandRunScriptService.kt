package org.n1.av2.run.terminal

import org.n1.av2.hacker.hackerstate.HackerState
import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.script.Script
import org.n1.av2.script.ScriptService
import org.n1.av2.script.effect.ScriptEffectTypeLookup
import org.n1.av2.script.effect.ScriptExecution
import org.n1.av2.script.effect.TerminalLockState
import org.n1.av2.script.ram.RamService
import org.n1.av2.script.type.ScriptTypeService
import org.springframework.stereotype.Service

@Service
class CommandRunScriptService(
    private val connectionService: ConnectionService,
    private val scriptService: ScriptService,
    private val scriptTypeService: ScriptTypeService,
    private val scriptEffectTypeLookup: ScriptEffectTypeLookup,
    private val ramService: RamService,
) {

    fun processRunScript(tokens: List<String>, hackerSate: HackerState) {
        if (tokens.size < 2) {
            connectionService.replyTerminalReceive("[b]run[/] [primary]<script code>[/]      -- for example: [b]run[primary] 1234-abcd")
            return
        }
        val scriptCode = tokens[1]
        val script = scriptService.validateScriptRunnable(scriptCode) ?: return

        runScript(script, tokens.drop(2), hackerSate)
    }

    fun runScript(script: Script, argumentTokens: List<String>, hackerSate: HackerState) {
        val type = scriptTypeService.getById(script.typeId)

        val executions: List<ScriptExecution> = type.effects.map { effect ->
            val effectService = scriptEffectTypeLookup.getForType(effect.type)
            effectService.prepareExecution(effect, argumentTokens, hackerSate)
        }

        val errorMessages = executions.mapNotNull { it.errorMessage }
        if (errorMessages.isNotEmpty()) {
            errorMessages.forEach {
                connectionService.replyTerminalReceive(it)
            }
            connectionService.replyTerminalSetLocked(false)
            return
        }

        val lockStates: List<TerminalLockState> = executions.map { it.executionMethod }.map { it() }
        if (lockStates.isEmpty()) {
            connectionService.replyTerminalReceive("Script executed successfully, but it has no effects.")
        }

// FIXME
//        ramService.useScript(hackerSate.userId, type.size)
//        scriptService.markAsUsedAndNotify(script)

        val lockedState = lockStates.all { it == TerminalLockState.LOCK }
        connectionService.replyTerminalSetLocked(lockedState)
    }
}
