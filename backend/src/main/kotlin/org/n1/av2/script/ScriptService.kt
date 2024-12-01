package org.n1.av2.script

import org.n1.av2.hacker.hackerstate.HackerState
import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.platform.iam.user.CurrentUserService
import org.n1.av2.platform.iam.user.ROLE_USER_MANAGER
import org.n1.av2.platform.iam.user.UserAndHackerService
import org.n1.av2.platform.util.TimeService
import org.n1.av2.platform.util.createId
import org.n1.av2.platform.util.createIdGeneric
import org.n1.av2.platform.util.toHumanTime
import org.n1.av2.script.access.ScriptAccessService
import org.n1.av2.script.scripts.TripwireExtraTimeScript
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Service
import java.time.Duration
import javax.annotation.PostConstruct


@Configuration
class ScriptServiceInit(
    private val userAndHackerService: UserAndHackerService,
    private val scriptService: ScriptService,
) {

    @PostConstruct
    fun postConstruct() {
        scriptService.userAndHackerService = userAndHackerService
    }
}


@Service
class ScriptService(
    private val scriptRepository: ScriptRepository,
    private val timeService: TimeService,
    private val currentUserService: CurrentUserService,
    private val scriptAccessService: ScriptAccessService,
    private val connectionService: ConnectionService,

    private val tripwireExtraTimeScript: TripwireExtraTimeScript,
) {

    lateinit var userAndHackerService: UserAndHackerService

    fun addScript(
        typeId: String,
        targetUserId: String,
    ) {
        val id = createId("script", scriptRepository::findById)
        val code = createIdGeneric("", scriptRepository::findByCode)

        val expiry = timeService.now().plusDays(1)
            .withNano(0).withSecond(0).withMinute(0).withHour(6) // expire at 06:00 tomorrow

        val script = Script(
            id = id,
            typeId = typeId,
            ownerUserId = targetUserId,
            expiry = expiry,
            code = code,
            loadStartedAt = null,
            loadTimeFinishAt = null,
            state = ScriptState.NOT_LOADED,
        )
        scriptRepository.save(script)
        userAndHackerService.sendDetailsOfSpecificUser(targetUserId)
    }

    fun findScriptsForUser(userId: String): List<Script> {
        return scriptRepository.findByOwnerUserId(userId)
    }


    fun timeLeft(script: Script): String {
        if (script.state == ScriptState.USED) return "script used"
        if (script.state == ScriptState.EXPIRED) return "expired"
        val timeLeft = Duration.between(timeService.now(), script.expiry)
        return toHumanTime(timeLeft) ?: "expired"
    }

    fun expired(script: Script): Boolean {
        return timeService.now().isAfter(script.expiry)
    }

    fun deleteScript(code: String) {
        val script = scriptRepository.findByCode(code) ?: error("Script not found with code: $code, maybe it was already deleted?")
        validateCanManage(script.ownerUserId)

        scriptRepository.delete(script)
        userAndHackerService.sendDetailsOfCurrentUser()
    }


    fun refreshScriptsForCurrentUser() {
        val userId = currentUserService.userId

        removeExpiredScripts(userId)
        addFreeReceiveScripts(userId)
        userAndHackerService.sendDetailsOfCurrentUser()
    }

    fun addFreeReceiveScripts(userId: String) {
        val scriptAccesses = scriptAccessService.findScriptAccessForUser(userId)

        scriptAccesses
            .filterNot { access -> access.used }
            .forEach { access ->
                (0 until access.receiveForFree).forEach { _ ->
                    addScript(access.typeId, userId)
                }
            }
    }

    fun removeExpiredScripts(userId: String) {
        val scripts = scriptRepository.findByOwnerUserId(userId)
        scripts
            .filter { script -> script.state == ScriptState.EXPIRED || expired(script) }
            .forEach { script ->
                scriptRepository.delete(script)
            }
    }


    private fun validateCanManage(ownerId: String) {
        val userManager = currentUserService.userEntity.type.authorities.contains(ROLE_USER_MANAGER)
        if (!userManager && ownerId != currentUserService.userId) {
            error("You are not allowed to manage this script.")
        }
    }

    fun runScript(scriptCode: String, hackerSate: HackerState) {
        // TODO: implement
//        val script = validateScriptRunnable(scriptCode) ?: return
//
//        val used = when (script.type) {
//            ScriptType.TRIPWIRE_EXTRA_TIME -> tripwireExtraTimeScript.runScript(script, hackerSate)
//            ScriptType.DEEP_SCAN -> tripwireExtraTimeScript.runScript(script, hackerSate)
//        }
//
//        if (used) {
//            val usedScript = script.copy(used = true)
//            scriptRepository.save(usedScript)
//            userAndHackerService.sendDetailsOfCurrentUser()
//        }

    }


    private fun validateScriptRunnable(scriptCode: String): Script? {
        val script = scriptRepository.findByCode(scriptCode)
        if (script == null) {
            errorScriptUnknown(scriptCode)
            return null
        }

        if (script.ownerUserId != currentUserService.userId) {
            errorScriptUnknown(scriptCode)
            return null
        }

        if (expired(script)) {
            connectionService.replyTerminalReceive("System has been patched against this script. (Script is expired.)")
            return null
        }

        if (script.state == ScriptState.USED) {
            connectionService.replyTerminalReceive("System has been patched against this script. (Script has already been used.)")
            return null
        }

        return script

    }

    private fun errorScriptUnknown(scriptCode: String) {
        connectionService.replyTerminalReceive("No script loaded with code: $scriptCode")
    }


}
