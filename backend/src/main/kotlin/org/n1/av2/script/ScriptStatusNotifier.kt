package org.n1.av2.script

import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.platform.connection.ServerActions
import org.n1.av2.platform.util.TimeService
import org.n1.av2.platform.util.toHumanTime
import org.n1.av2.script.common.UiEffectDescription
import org.n1.av2.script.common.toUiEffectDescriptions
import org.n1.av2.script.effect.ScriptEffectLookup
import org.n1.av2.script.ram.RamEntity
import org.n1.av2.script.type.ScriptType
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.ZonedDateTime

@Service
class ScriptStatusNotifier(
    private val connectionService: ConnectionService,
    private val scriptEffectLookup: ScriptEffectLookup,
    private val timeService: TimeService,
) {

    @Suppress("unused")
    class UiScript(
        val id: ScriptId,
        val name: String,
        val code: String,
        val effects: List<UiEffectDescription>,
        val timeLeft: String,
        val state: ScriptState,
        val ram: Int,
    )

    @Suppress("unused")
    class UiRam(
        val enabled: Boolean,
        val size: Int,
        val loaded: Int,
        val refreshing: Int,
        val free: Int,
        val nextRefreshAt: ZonedDateTime?,
        val lockedUntil: ZonedDateTime?,
    )

    @Suppress("unused")
    class UIScriptStatus(
        val scripts: List<UiScript>,
        val ram: UiRam,
    )

    fun sendScriptStatusOfCurrentUser(scriptsAndTypes: List<Pair<Script, ScriptType>>, ram: RamEntity) {
        val status = createUiScriptStatus(scriptsAndTypes, ram)
        connectionService.reply(ServerActions.SERVER_RECEIVE_SCRIPT_STATUS, status)
    }

    fun sendScriptStatusOfSpecificUser(userId: String, scriptsAndTypes: List<Pair<Script, ScriptType>>, ram: RamEntity) {
        val status = createUiScriptStatus(scriptsAndTypes, ram)
        connectionService.reply(ServerActions.SERVER_RECEIVE_SCRIPT_STATUS, status)
        connectionService.toUser(userId, ServerActions.SERVER_RECEIVE_SCRIPT_STATUS, status)
    }


    fun createUiScriptStatus(scriptsAndTypes: List<Pair<Script, ScriptType>>, ram: RamEntity): UIScriptStatus {
        return UIScriptStatus(
            scriptsAndTypes.map { toUiScript(it) },
            UiRam(ram.enabled, ram.size, ram.loaded, ram.refreshing, ram.free, ram.nextRefresh, ram.lockedUntil)
        )
    }

    private fun toUiScript(scriptAndType: Pair<Script, ScriptType>): UiScript {
        val (script, type) = scriptAndType
        val timeLeft = timeLeft(script)
        val effects = type.toUiEffectDescriptions(scriptEffectLookup)
        return UiScript(script.id, type.name, script.code, effects, timeLeft, script.state, type.size)
    }

    fun timeLeft(script: Script): String {
        if (script.state == ScriptState.USED) return "script used"
        if (script.state == ScriptState.EXPIRED || timeService.now().isAfter(script.expiry)) return "expired"
        val timeLeft = Duration.between(timeService.now(), script.expiry)
        return timeLeft.toHumanTime()
    }
}
