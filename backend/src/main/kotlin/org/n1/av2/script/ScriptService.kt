package org.n1.av2.script

import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.platform.connection.ServerActions
import org.n1.av2.platform.iam.user.CurrentUserService
import org.n1.av2.platform.iam.user.ROLE_USER_MANAGER
import org.n1.av2.platform.util.TimeService
import org.n1.av2.platform.util.createId
import org.n1.av2.platform.util.createIdGeneric
import org.n1.av2.script.access.ScriptAccessService
import org.n1.av2.script.ram.RamService
import org.n1.av2.script.type.ScriptType
import org.n1.av2.script.type.ScriptTypeId
import org.n1.av2.script.type.ScriptTypeService
import org.springframework.stereotype.Service

@Service
class ScriptService(
    private val scriptRepository: ScriptRepository,
    private val timeService: TimeService,
    private val currentUserService: CurrentUserService,
    private val scriptAccessService: ScriptAccessService,
    private val connectionService: ConnectionService,
    private val scriptTypeService: ScriptTypeService,
    private val ramService: RamService,
    private val scriptStatusNotifier: ScriptStatusNotifier,
) {
    fun getScriptById(scriptId: ScriptId): Script {
        return scriptRepository.findById(scriptId).orElseThrow { error("Script not found with id: $scriptId") }
    }

    fun sendScriptStatusToCurrentUser() {
        val scripts = getAndUpdateScripts(currentUserService.userId)
        scriptStatusNotifier.sendScriptStatusOfCurrentUser(scripts)
    }

    fun sendScriptStatusForUser(userId: String) {
        val scripts = getAndUpdateScripts(userId)
        scriptStatusNotifier.sendScriptStatusOfSpecificUser(userId, scripts)
    }

    fun addScriptAndInformUser(typeId: ScriptTypeId, targetUserId: String) {
        addScript(typeId, targetUserId)
        val scripts = getAndUpdateScripts(targetUserId)
        scriptStatusNotifier.sendScriptStatusOfSpecificUser(targetUserId, scripts)
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
            state = ScriptState.AVAILABLE,
        )
        scriptRepository.save(script)
    }

    fun getAndUpdateScripts(userId: String): List<Script> {
        val scripts = scriptRepository.findByOwnerUserId(userId)

        if (scripts.none { script -> script.state != ScriptState.EXPIRED && expired(script) }) {
            return scripts
        }

        setStateToExpired(scripts)
        return scriptRepository.findByOwnerUserId(userId)
    }


    private fun setStateToExpired(scripts: List<Script>) {
        scripts
            .filter { script -> script.state != ScriptState.EXPIRED && expired(script) }
            .forEach { script ->
                val scriptExpired = script.copy(state = ScriptState.EXPIRED)
                scriptRepository.save(scriptExpired)
            }
    }

    fun expired(script: Script): Boolean {
        return timeService.now().isAfter(script.expiry)
    }

    fun deleteScript(id: ScriptId) {
        val script = getScriptById(id)
        validateCanManage(script.ownerUserId)

        val type = scriptTypeService.getById(script.typeId)
        if (script.state == ScriptState.LOADED || script.state == ScriptState.USED) {
            ramService.unload(script.ownerUserId, type.size, false)
        }

        scriptRepository.delete(script)
        informUserOfScriptChanged(script)
    }

    fun loadScript(id: ScriptId, overrideLocked: Boolean = false) {
        val script = getScriptById(id)
        validateCanManage(script.ownerUserId)

        val type = scriptTypeService.getById(script.typeId)
        ramService.load(script.ownerUserId, type.size, overrideLocked)

        val updatedScript = script.copy(state = ScriptState.LOADED)
        scriptRepository.save(updatedScript)
        informUserOfScriptChanged(script)
    }

    fun unloadScript(id: ScriptId, overrideLocked: Boolean = false) {
        val script = getScriptById(id)
        validateCanManage(script.ownerUserId)
        val type = scriptTypeService.getById(script.typeId)
        ramService.unload(script.ownerUserId, type.size, overrideLocked)


        val scriptLoading = script.copy(state = ScriptState.AVAILABLE, )
        scriptRepository.save(scriptLoading)
        informUserOfScriptChanged(script)
    }

    private fun informUserOfScriptChanged(script: Script) {
        val scripts = getAndUpdateScripts(script.ownerUserId)
        if (currentUserService.userId == script.ownerUserId) {
            scriptStatusNotifier.sendScriptStatusOfCurrentUser(scripts) // hacker loads script
        } else {
            scriptStatusNotifier.sendScriptStatusOfSpecificUser(script.ownerUserId, scripts) // GM loads script for user
        }
    }

    fun addFreeReceiveScriptsForCurrentUser() {
        val userId = currentUserService.userId
        addFreeReceiveScripts(userId)

        val scripts = getAndUpdateScripts(userId)
        scriptStatusNotifier.sendScriptStatusOfCurrentUser(scripts)
    }

    fun addFreeReceiveScripts(userId: String) {
        val scriptAccesses = scriptAccessService.findScriptAccessForUser(userId)

        scriptAccesses
            .filter { access -> timeService.isPastReset(access.lastUsed)}
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

        val updatedScripts = getAndUpdateScripts(currentUserService.userId)
        scriptStatusNotifier.sendScriptStatusOfCurrentUser(updatedScripts)
    }


    fun validateScriptRunnable(scriptCode: String): Script? {
        val script = scriptRepository.findByCode(scriptCode)

        if (script == null || script.ownerUserId != currentUserService.userId) {
            connectionService.replyTerminalReceive("No script loaded with code: $scriptCode")
            return null
        }
        if (expired(script)) {
            connectionService.replyTerminalReceive("System has been patched against this script. (Script is expired.)")
            return null
        }

        if (script.state == ScriptState.LOADED) {
            return script
        }

        val message = when (script.state) {
            ScriptState.EXPIRED -> "System has been patched against this script. (Script is expired.)"
            ScriptState.USED -> "System has been patched against this script. (Script has already been used.)"
            ScriptState.AVAILABLE -> "Script has not been loaded."
            ScriptState.LOADED -> error("illegal code path")
        }

        connectionService.replyTerminalReceive(message)
        return null
    }

    class ScriptStatisticsLine(val name: String, val owned: Int, val loaded: Int, val freeReceive: Int)
    fun getStatistics() {
        val typesById = scriptTypeService.findAll().associateBy { it.id }

        val allScripts = scriptRepository.findAll()
        val usableScripts = allScripts.filter(this::usable)

        val loadedScripts = usableScripts.filter { script -> script.state == ScriptState.LOADED }
        val loadedStats: Map<String, Int> = loadedScripts.groupingBy { it.typeId }.eachCount()


        val ownedStats = usableScripts.groupingBy { it.typeId }.eachCount()

        val freeReceiveStats = scriptAccessService.findAll()
            .filter { access -> access.receiveForFree > 0 }
            .groupingBy { it.typeId }
            .fold(0) { accumulator, access -> accumulator + access.receiveForFree }

        val lines = typesById.values.map { type: ScriptType ->
            val loaded = loadedStats[type.id] ?: 0
            val owned = ownedStats[type.id] ?: 0
            val freeReceive = freeReceiveStats[type.id] ?: 0

            ScriptStatisticsLine(type.name, owned, loaded, freeReceive)
        }

        connectionService.reply(ServerActions.SERVER_SCRIPT_STATISTICS, lines)
    }

    fun usable(script: Script): Boolean {
        return script.state != ScriptState.USED && script.state != ScriptState.EXPIRED && !expired(script)
    }

    fun markAsUsedAndNotify(script: Script) {
        val copy = script.copy(state = ScriptState.USED)
        scriptRepository.save(copy)

        val scripts = getAndUpdateScripts(currentUserService.userId)
        scriptStatusNotifier.sendScriptStatusOfCurrentUser(scripts)
    }

    fun findByTypeId(typeId: ScriptTypeId): List<Script> {
        return scriptRepository.findByTypeId(typeId)
    }

    fun unloadAllScriptsForShrinkedRam(userId: String) {
        val scripts = scriptRepository.findByOwnerUserId(userId)
        scripts.forEach { script ->
            if (script.state == ScriptState.LOADED) {
                val unloadedScript = script.copy(state = ScriptState.AVAILABLE)
                scriptRepository.save(unloadedScript)
            }
        }
    }

}
