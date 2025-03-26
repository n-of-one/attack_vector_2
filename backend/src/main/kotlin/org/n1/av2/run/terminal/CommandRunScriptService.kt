package org.n1.av2.run.terminal

import org.n1.av2.hacker.hackerstate.HackerState
import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.script.Script
import org.n1.av2.script.ScriptService
import org.n1.av2.script.effect.ScriptEffectTypeLookup
import org.n1.av2.script.effect.ScriptExecution
import org.n1.av2.script.effect.TerminalState
import org.n1.av2.script.ram.RamService
import org.n1.av2.script.type.ScriptType
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

        val executions: List<ScriptExecution> = prepareExecutions(type, argumentTokens, hackerSate)
        if (processErrors(executions)) return

        if (executions.isEmpty()) {
            connectionService.replyTerminalReceive("Script executed successfully, but it has no effects.")
        } else {
            executions.forEach { it.executionMethod() }

            // Manually unlock terminal in case no effect returned any text to the terminal (and thus unlocking it),
            // except when an execution specifically tells us to keep it locked.
            if (executions.none { it.terminalState == TerminalState.KEEP_LOCKED } ) {
                connectionService.replyTerminalSetLocked(false)
            }
        }


// FIXME
//        ramService.useScript(hackerSate.userId, type.size)
//        scriptService.markAsUsedAndNotify(script)

    }

    private fun prepareExecutions(type: ScriptType, argumentTokens: List<String>, hackerSate: HackerState): List<ScriptExecution> {
        return type.effects.map { effect ->
            val effectService = scriptEffectTypeLookup.getForType(effect.type)
            effectService.prepareExecution(effect, argumentTokens, hackerSate)
        }
    }

    private fun processErrors(executions: List<ScriptExecution>): Boolean {
        val errorMessages = executions.mapNotNull { it.errorMessage }.ifEmpty { return false }
        errorMessages.forEach {
            connectionService.replyTerminalReceive(it)
        }
        connectionService.replyTerminalSetLocked(false)
        return true
    }

}
