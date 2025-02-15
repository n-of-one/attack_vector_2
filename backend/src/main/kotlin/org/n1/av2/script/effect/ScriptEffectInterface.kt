package org.n1.av2.script.effect

import org.n1.av2.hacker.hackerstate.HackerState
import org.n1.av2.script.type.ScriptEffect


enum class TerminalLockState {
    LOCK, UNLOCK
}

interface ScriptEffectInterface {
    val name: String
    val defaultValue: String?
    val gmDescription: String
    fun playerDescription(effect: ScriptEffect): String
    fun validate(effect: ScriptEffect): String?
    fun checkCanExecute(effect: ScriptEffect, tokens: List<String>, hackerSate: HackerState): String?
    fun execute(effect: ScriptEffect, strings: List<String>, state: HackerState): TerminalLockState
}
