package org.n1.av2.script.effect

import org.n1.av2.hacker.hackerstate.HackerState
import org.n1.av2.script.type.ScriptEffect


enum class TerminalLockState {
    LOCK, UNLOCK
}

class ScriptExecution(errorMessage: String?, executionMethod: () -> TerminalLockState) {

    constructor(errorMessage: String): this(errorMessage, { TerminalLockState.UNLOCK })
    constructor(executionMethod: () -> TerminalLockState): this(null, executionMethod)

    val errorMessage: String? = errorMessage
    val executionMethod: () -> TerminalLockState = executionMethod
}

interface ScriptEffectInterface {
    val name: String
    val defaultValue: String?
    val gmDescription: String
    fun playerDescription(effect: ScriptEffect): String
    fun validate(effect: ScriptEffect): String?
    fun prepareExecution(effect: ScriptEffect, argumentTokens: List<String>, hackerState: HackerState): ScriptExecution
}
