package org.n1.av2.run.terminal.generic

import org.n1.av2.hacker.hackerstate.HackerStateRunning
import org.n1.av2.hacker.skill.SkillService
import org.n1.av2.hacker.skill.SkillType
import org.n1.av2.platform.config.ConfigItem
import org.n1.av2.platform.config.ConfigService
import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.run.terminal.MISSING_SKILL_RESPONSE
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
class CommandScriptService(
    private val connectionService: ConnectionService,
    private val scriptService: ScriptService,
    private val scriptTypeService: ScriptTypeService,
    private val scriptEffectTypeLookup: ScriptEffectTypeLookup,
    private val ramService: RamService,
    private val skillService: SkillService,
    private val configService: ConfigService,
) {

    fun processDownloadScript(arguments: List<String>) {
        if (!configService.getAsBoolean(ConfigItem.HACKER_SCRIPT_LOAD_DURING_RUN)) {
            connectionService.replyTerminalReceive("Downloading scripts during a run is not supported.")
            return
        }
        if (!checkHasScriptsSkill()) return

        if (arguments.isEmpty()) {
            connectionService.replyTerminalReceive("Missing [primary]<script code>[/] for example /download-script [primary]1234-abcd[/].")
            return
        }
        val scriptCode = arguments.first()
        scriptService.downloadScript(scriptCode, true)
    }

    fun processRunScript(arguments: List<String>, hackerState: HackerStateRunning) {
        if (!checkHasScriptsSkill()) return

        if (arguments.isEmpty()) {
            connectionService.replyTerminalReceive("[b]run[/] [primary]<script code>[/]      -- for example: [b]run[primary] 1234-abcd")
            return
        }
        val scriptCode = arguments.first()
        val script = scriptService.validateScriptRunnable(scriptCode) ?: return

        runScript(script, arguments.drop(1), hackerState)
    }

    private fun checkHasScriptsSkill(): Boolean {
        val hasScriptSkill = skillService.currentUserHasSkill(SkillType.SCRIPT_RAM)
        if (!hasScriptSkill) {
            connectionService.replyTerminalReceive(MISSING_SKILL_RESPONSE)
            return false
        }
        return true
    }


    fun runScript(script: Script, scriptArguments: List<String>, hackerState: HackerStateRunning) {
        val type = scriptTypeService.getById(script.typeId)

        val executions: List<ScriptExecution> = prepareExecutions(type, scriptArguments, hackerState)
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

        ramService.useScript(hackerState.userId, type.size)
        scriptService.markAsUsedAndNotify(script)
    }

    private fun prepareExecutions(type: ScriptType, argumentTokens: List<String>, hackerState: HackerStateRunning): List<ScriptExecution> {
        return type.effects.map { effect ->
            val effectService = scriptEffectTypeLookup.getForType(effect.type)
            effectService.prepareExecution(effect, argumentTokens, hackerState)
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
