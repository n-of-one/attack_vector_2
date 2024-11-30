package org.n1.av2.script

import org.n1.av2.hacker.hackerstate.HackerActivity
import org.n1.av2.hacker.hackerstate.HackerState
import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.platform.iam.user.CurrentUserService
import org.n1.av2.platform.iam.user.ROLE_USER_MANAGER
import org.n1.av2.platform.iam.user.UserAndHackerService
import org.n1.av2.platform.util.TimeService
import org.n1.av2.platform.util.createId
import org.n1.av2.platform.util.toHumanTime
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

    fun addScript(typeId: String,
                  targetUserId: String,
                  accessId: String? = null,
                      ) {
        val id = createId("script", scriptRepository::findById)
        val code = createId("", scriptRepository::findByCode).substringAfter("-")

        val expiry = timeService.now().plusDays(1)
            .withNano(0).withSecond(0).withMinute(0).withHour(6) // expire at 06:00 tomorrow

        val script = Script(
            id = id,
            typeId = typeId,
            ownerUserId = targetUserId,
            expiry = expiry,
            code = code,
            accessId = accessId,
            loadedByUserId = targetUserId,
        )
        scriptRepository.save(script)
        userAndHackerService.sendDetailsOfSpecificUser(targetUserId)
    }

    fun findScriptsForUser(userId: String): List<Script> {
        return scriptRepository.findByOwnerUserId(userId)
    }


    fun timeLeft(script: Script): String {
        if (script.used) return "script used"
        val timeLeft = Duration.between(timeService.now(), script.expiry)
        return toHumanTime(timeLeft) ?: "expired"
    }

    fun expired(script: Script): Boolean {
        return timeService.now().isAfter(script.expiry)
    }

    fun usable(script: Script): Boolean {
        return !script.used && !expired(script) && !script.deleted
    }

    fun deleteScript(code: String) {
        val script = scriptRepository.findByCode(code) ?: error("Script not found with code: $code, maybe it was already deleted?")
        validateCanManage(script.ownerUserId)

        scriptRepository.delete(script)
        userAndHackerService.sendDetailsOfCurrentUser()
    }


    fun refreshScriptsForCurrentUser() {
        val userId = currentUserService.userId
        val scriptAccesses = scriptAccessService.findScriptAccessForUser(userId)

        scriptAccesses.forEach { access ->
            val scriptsForThisAccess = scriptRepository.findByAccessId(access.id)
            val expiredScripts = deleteExpiredScripts(scriptsForThisAccess)
            val nonExpiredScripts = scriptsForThisAccess - expiredScripts

            val scriptsToCreate = access.receiveForFree - nonExpiredScripts.size
            (0 until scriptsToCreate).forEach { _ ->
                addScript(access.typeId, userId, access.id)
            }
        }

        val scriptsWithoutAccess = scriptRepository.findByAccessId(null)
        deleteExpiredScripts(scriptsWithoutAccess)

        userAndHackerService.sendDetailsOfCurrentUser()
    }

    private fun deleteExpiredScripts(scripts: List<Script>): List<Script> {
        val expiredScripts = scripts.filter { expired(it) }
        expiredScripts.forEach { script ->
            scriptRepository.delete(script)
        }
        return expiredScripts
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
            errorScriptNotLoaded(scriptCode)
            return null
        }

        if (script.loadedByUserId != currentUserService.userId) {
            errorScriptNotLoaded(scriptCode)
            return null
        }

        if (expired(script)) {
            connectionService.replyTerminalReceive("System has been patched against this script. (Script is expired.)")
            return null
        }

        if (script.used) {
            connectionService.replyTerminalReceive("System has been patched against this script. (Script has already been used.)")
            return null
        }

        if (script.deleted) {
            errorScriptNotLoaded(scriptCode)
            return null
        }

        return script

    }

    private fun errorScriptNotLoaded(scriptCode: String) {
        connectionService.replyTerminalReceive("No script loaded with code: $scriptCode")
    }


}
