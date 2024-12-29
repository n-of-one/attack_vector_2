package org.n1.av2.script

import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.platform.connection.ServerActions
import org.n1.av2.platform.engine.ScheduledTask
import org.n1.av2.platform.engine.UserTaskRunner
import org.n1.av2.platform.iam.user.CurrentUserService
import org.n1.av2.platform.iam.user.ROLE_USER_MANAGER
import org.n1.av2.platform.iam.user.UserAndHackerService
import org.n1.av2.platform.iam.user.UserAndHackerService.UiScript
import org.n1.av2.platform.util.TimeService
import org.n1.av2.platform.util.createId
import org.n1.av2.platform.util.createIdGeneric
import org.n1.av2.platform.util.toHumanTime
import org.n1.av2.script.access.ScriptAccessService
import org.n1.av2.script.type.ScriptType
import org.n1.av2.script.type.ScriptTypeId
import org.n1.av2.script.type.ScriptTypeService
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Service
import java.time.Duration
import javax.annotation.PostConstruct
import kotlin.jvm.optionals.getOrNull


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
    private val userTaskRunner: UserTaskRunner,
    private val scriptTypeService: ScriptTypeService,
) {

    lateinit var userAndHackerService: UserAndHackerService

    fun getScriptById(scriptId: ScriptId): Script {
        return scriptRepository.findById(scriptId).orElseThrow { error("Script not found with id: $scriptId") }
    }

    fun addScriptAndInformUser(typeId: ScriptTypeId, targetUserId: String) {
        addScript(typeId, targetUserId)
        userAndHackerService.sendDetailsOfSpecificUser(targetUserId)
    }

    fun addScript(typeId: ScriptTypeId, targetUserId: String) {
        val scriptId = createId("script", scriptRepository::findById)
        val code = createIdGeneric("", scriptRepository::findByCode)

        val expiry = timeService.now().plusDays(1)
            .withNano(0).withSecond(0).withMinute(0).withHour(6) // expire at 06:00 tomorrow

        val script = Script(
            id = scriptId,
            typeId = typeId,
            ownerUserId = targetUserId,
            expiry = expiry,
            code = code,
            loadStartedAt = null,
            loadTimeFinishAt = null,
            inMemory = false,
            state = ScriptState.AVAILABLE,
        )
        scriptRepository.save(script)
    }

    fun findUiScriptsForUser(userId: String): List<UiScript> {
        val scripts = scriptRepository.findByOwnerUserId(userId)

        if (scripts.none { script -> script.state != ScriptState.EXPIRED && expired(script) }) {
            return toUiScripts(scripts)
        }

        setStateToExpired(scripts)
        return toUiScripts(scriptRepository.findByOwnerUserId(userId))
    }

    private fun toUiScripts(scripts: List<Script>): List<UiScript> {
        return scripts.map { script ->
            val timeLeft = timeLeft(script)
            val type = scriptTypeService.getById(script.typeId)
            val effects = type.effects.map{ effect ->
                val effectService = scriptTypeService.effectService(effect.type)
                effectService.playerDescription(effect)
            }
            UiScript(script.id, type.name, script.code, effects, timeLeft, script.state, script.inMemory, type.ram, script.loadStartedAt, script.loadTimeFinishAt)
        }
    }

    private fun setStateToExpired(scripts: List<Script>) {
        scripts
            .filter { script -> script.state != ScriptState.EXPIRED && expired(script) }
            .forEach { script ->
                val scriptExpired = script.copy(state = ScriptState.EXPIRED)
                scriptRepository.save(scriptExpired)
            }
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

    fun deleteScript(id: ScriptId) {
        val script = getScriptById(id)
        validateCanManage(script.ownerUserId)

        scriptRepository.delete(script)
        informUserOfScriptStatus(script)
    }

    fun loadScript(id: ScriptId) {
        val script = getScriptById(id)
        validateCanManage(script.ownerUserId)

        val loadTime = Duration.ofSeconds(10)

        val scriptLoading = script.copy(state = ScriptState.LOADING, loadStartedAt = timeService.now(), loadTimeFinishAt = timeService.now() + loadTime)
        scriptRepository.save(scriptLoading)
        informUserOfScriptStatus(script)
        val identifiers = mapOf("scriptId" to script.id)
        userTaskRunner.queue("script loaded", identifiers, loadTime) { scriptLoadComplete(script.id) }
    }

    @ScheduledTask
    private fun scriptLoadComplete(scriptId: ScriptId) {
        val script = scriptRepository.findById(scriptId).getOrNull() ?: return // script has been deleted.
        if (script.state != ScriptState.LOADING) return // script has been changed to another state.
        val scriptLoaded = script.copy(state = ScriptState.LOADED, loadStartedAt = null, loadTimeFinishAt = null)
        scriptRepository.save(scriptLoaded)
        informUserOfScriptStatus(script)
    }

    private fun informUserOfScriptStatus(script: Script) {
        if (currentUserService.userId == script.ownerUserId) {
            userAndHackerService.sendDetailsOfCurrentUser() // hacker loads script
        } else {
            userAndHackerService.sendDetailsOfSpecificUser(script.ownerUserId) // GM loads script for user
        }
    }

    fun instantLoadScript(id: ScriptId) {
        val script = getScriptById(id)
        validateCanManage(script.ownerUserId)

        val scriptLoading = script.copy(state = ScriptState.LOADED, loadStartedAt = null, loadTimeFinishAt = null)
        scriptRepository.save(scriptLoading)
        userAndHackerService.sendDetailsOfSpecificUser(script.ownerUserId)
    }

    fun unloadScript(id: ScriptId) {
        val script = getScriptById(id)
        validateCanManage(script.ownerUserId)

        val scriptLoading = script.copy(state = ScriptState.AVAILABLE, loadStartedAt = null, loadTimeFinishAt = null)
        scriptRepository.save(scriptLoading)
        informUserOfScriptStatus(script)
        if (script.state == ScriptState.LOADING) {
            val identifiers = mapOf("scriptId" to script.id)
            userTaskRunner.removeTask(identifiers)
        }
    }


    fun addFreeReceiveScriptsForCurrentUser() {
        val userId = currentUserService.userId

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
                scriptAccessService.markUsed(access)
            }
    }


    private fun validateCanManage(ownerId: String) {
        val userManager = currentUserService.userEntity.type.authorities.contains(ROLE_USER_MANAGER)
        if (!userManager && ownerId != currentUserService.userId) {
            error("You are not allowed to manage this script.")
        }
    }

    fun cleanup() {
        val scripts = scriptRepository.findByOwnerUserId(currentUserService.userId)
        scripts
            .filter { script -> script.state == ScriptState.EXPIRED || expired(script) || script.state == ScriptState.USED }
            .forEach { script ->
                scriptRepository.delete(script)
            }
        userAndHackerService.sendDetailsOfCurrentUser()
    }





    fun validateScriptRunnable(scriptCode: String): Script? {
        val script = scriptRepository.findByCode(scriptCode)

        if (script == null) {
            errorScriptUnknown(scriptCode)
            return null
        }

        if (script.ownerUserId != currentUserService.userId) {
            errorScriptUnknown(scriptCode)
            return null
        }

        if (script.state == ScriptState.EXPIRED || expired(script)) {
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


    class ScriptStatisticsLine(val name: String, val owned: Int, val loaded: Int, val loading: Int, val freeReceive: Int)

    fun getStatistics() {
        val typesById = scriptTypeService.findAll().associateBy { it.id }

        val allScripts = scriptRepository.findAll()
        val usableScripts = allScripts.filter(this::usable)

        val loadedScripts = usableScripts.filter { script -> script.state == ScriptState.LOADED }
        val loadedStats: Map<String, Int> = loadedScripts.groupingBy { it.typeId }.eachCount()

        val loadingScripts = usableScripts.filter { script -> script.state == ScriptState.LOADING }
        val loadingStats: Map<String, Int> = loadingScripts.groupingBy { it.typeId }.eachCount()

        val ownedStats = usableScripts.groupingBy { it.typeId }.eachCount()

        val freeReceiveStats = scriptAccessService.findAll()
            .filter { access -> access.receiveForFree > 0 }
            .groupingBy { it.typeId }
            .fold(0) { accumulator, access -> accumulator + access.receiveForFree }

        val lines = typesById.values.map { type: ScriptType ->
            val loaded = loadedStats[type.id] ?: 0
            val loading = loadingStats[type.id] ?: 0
            val owned = ownedStats[type.id] ?: 0
            val freeReceive = freeReceiveStats[type.id] ?: 0

            ScriptStatisticsLine(type.name, owned, loaded, loading, freeReceive)
        }

        connectionService.reply(ServerActions.SERVER_SCRIPT_STATISTICS, lines)
    }

    fun usable(script: Script): Boolean {
        return script.state != ScriptState.USED && script.state != ScriptState.EXPIRED && !expired(script)
    }

    fun markAsUsedAndNotify(script: Script) {
        val copy = script.copy(state = ScriptState.USED)
        scriptRepository.save(copy)
        userAndHackerService.sendDetailsOfCurrentUser()
    }

    fun findByTypeId(typeId: ScriptTypeId): List<Script> {
        return scriptRepository.findByTypeId(typeId)
    }

}
