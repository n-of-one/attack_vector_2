package org.n1.av2.run.terminal

import org.n1.av2.hacker.hackerstate.HackerState
import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.script.Script
import org.n1.av2.script.ScriptService
import org.n1.av2.script.effect.TerminalLockState
import org.n1.av2.script.type.ScriptType
import org.n1.av2.script.type.ScriptTypeService
import org.springframework.stereotype.Service

@Service
class CommandRunScriptService(
    private val connectionService: ConnectionService,
    private val scriptService: ScriptService,
    private val scriptTypeService: ScriptTypeService,
) {

    fun processRunScript(tokens: List<String>, hackerSate: HackerState) {
        if (tokens.size < 2) {
            connectionService.replyTerminalReceive("[b]run[/] [primary]<script code>[/]      -- for example: [b]run[primary] 1234-abcd")
            return
        }
        val scriptCode = tokens[1]
        val script = scriptService.validateScriptRunnable(scriptCode) ?: return

        runScript(script, tokens, hackerSate)
    }

    fun runScript(script: Script, tokens: List<String>, hackerSate: HackerState) {
        val type = scriptTypeService.getById(script.typeId)

        val executionProblems = type.effects.mapNotNull { effect ->
            val service = scriptTypeService.effectService(effect.type)
            service.checkCanExecute(effect, tokens, hackerSate)
        }

        if (executionProblems.isNotEmpty()) {
            executionProblems.forEach { connectionService.replyTerminalReceive(it) }
            connectionService.replyTerminalSetLocked(false)
            return
        }

        val lockState = runEffects(type, tokens, hackerSate)
        scriptService.markAsUsedAndNotify(script)

        if (lockState == TerminalLockState.UNLOCK) {
            connectionService.replyTerminalSetLocked(false)
        }
    }


    private fun runEffects(type: ScriptType, tokens: List<String>, state: HackerState): TerminalLockState {
        if (type.effects.isEmpty()) {
            connectionService.replyTerminalReceive("Script executed successfully, but it has no effects.")
        }

        val unlockTerminalFromEffects = type.effects.map { effect ->
            val service = scriptTypeService.effectService(effect.type)
            service.execute(effect, tokens, state)
        }

        if (unlockTerminalFromEffects.any { it == TerminalLockState.LOCK } ) return TerminalLockState.LOCK
        return TerminalLockState.UNLOCK
    }
}
