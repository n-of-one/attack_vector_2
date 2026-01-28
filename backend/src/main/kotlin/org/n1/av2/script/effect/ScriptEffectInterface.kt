package org.n1.av2.script.effect

import org.n1.av2.hacker.hackerstate.HackerStateRunning
import org.n1.av2.platform.util.validateDuration
import org.n1.av2.script.effect.TerminalState.UNLOCK_AFTER_SCRIPT
import org.n1.av2.script.type.ScriptEffect

const val scriptCannotInteractWithThisLayer = "Script cannot interact with this layer."

enum class TerminalState {
    UNLOCK_AFTER_SCRIPT,
    KEEP_LOCKED
}

class ScriptExecution (val errorMessage: String?, val executionMethod: () -> Unit, val terminalState: TerminalState = UNLOCK_AFTER_SCRIPT) {
    constructor(errorMessage: String): this(errorMessage, { })
    constructor(executionMethod: () -> Unit): this(null, executionMethod)
    constructor(terminalState: TerminalState, executionMethod: () -> Unit): this(null, executionMethod, terminalState)
}

interface ScriptEffectInterface {
    val name: String
    val defaultValue: String?
    val gmDescription: String
    fun playerDescription(effect: ScriptEffect): String
    fun validate(effect: ScriptEffect): String?
    fun prepareExecution(effect: ScriptEffect, argumentTokens: List<String>, hackerState: HackerStateRunning): ScriptExecution

    companion object {

        fun validateDuration(effect: ScriptEffect): String? {
            if (effect.value == null) return "Duration is required."
            return effect.value.validateDuration()
        }

        fun validateIntegerGreaterThanZero(effect: ScriptEffect): String? {
            if (effect.value == null) return "value is required."
            val value = effect.value.toIntOrNull()
            if (value == null || value <=0) return "Value must be a number greater than zero."
            return null
        }

        fun validateNonEmptyText(effect: ScriptEffect): String? {
            if (effect.value == null) return "Text is required."
            if (effect.value.isBlank()) return "Text cannot be empty."
            return null
        }


    }
}


