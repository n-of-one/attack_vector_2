package org.n1.av2.script.access

import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.platform.connection.ServerActions
import org.n1.av2.platform.iam.user.*
import org.n1.av2.platform.inputvalidation.ValidationException
import org.n1.av2.platform.util.TimeService
import org.n1.av2.platform.util.createId
import org.n1.av2.script.common.UiEffectDescription
import org.n1.av2.script.common.toUiEffectDescriptions
import org.n1.av2.script.effect.ScriptEffectTypeLookup
import org.n1.av2.script.type.ScriptTypeId
import org.n1.av2.script.type.ScriptTypeService
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct

@Configuration
class ScriptAccessServiceInit(
    private val userAndHackerService: UserAndHackerService,
    private val scriptAccessService: ScriptAccessService,
) {

    @PostConstruct
    fun postConstruct() {
        scriptAccessService.userAndHackerService = userAndHackerService
    }
}


@Service
class ScriptAccessService(
    private val scriptAccessRepository: ScriptAccessRepository,
    private val currentUserService: CurrentUserService,
    private val connectionService: ConnectionService,
    private val scriptTypeService: ScriptTypeService,
    private val scriptEffectTypeLookup: ScriptEffectTypeLookup,
    private val timeService: TimeService,
    private val userEntityService: UserEntityService,
) {

    lateinit var userAndHackerService: UserAndHackerService

    fun findScriptAccessForUser(userId: String): List<ScriptAccess> {
        return scriptAccessRepository.findByOwnerUserId(userId)
    }

    @Suppress("unused")
    class ScriptAccessUi(
        val id: ScriptAccessId,
        val type: ScriptTypeUi,
        val receiveForFree: Int,
        val price: Int?,
        val used: Boolean,
    )

    @Suppress("unused")
    class ScriptTypeUi(
        val id: String,
        val name: String,
        val size: Int,
        val effects: List<UiEffectDescription>
    )

    fun sendScriptAccess(userId: String) {
        val scriptAccesses = findScriptAccessForUser(userId)
        val scriptAccessUis = scriptAccesses.map { scriptAccess ->
            val type = scriptTypeService.getById(scriptAccess.typeId)
            val uiType = ScriptTypeUi(
                id = type.id,
                name = type.name,
                size = type.size,
                effects = type.toUiEffectDescriptions(scriptEffectTypeLookup)
            )
            ScriptAccessUi(
                id = scriptAccess.id,
                type = uiType,
                receiveForFree = scriptAccess.receiveForFree,
                price = scriptAccess.price,
                used = !timeService.isPastReset(scriptAccess.lastUsed)
            )
        }

        connectionService.reply(ServerActions.SERVER_RECEIVE_SCRIPT_ACCESS, scriptAccessUis)
    }

    fun addScriptAccess(typeId: String, userId: String, receiveForFree: Int = 0, price: Int? = null) {
        validateCanManageAccess()
        validateDoesNotAlreadyHaveAccess(userId, typeId)
        val id = createId("access", scriptAccessRepository::findById)
        val type = scriptTypeService.getById(typeId)

        val scriptAccess = ScriptAccess(
            id = id,
            ownerUserId = userId,
            typeId = typeId,
            receiveForFree = receiveForFree,
            price = price ?: type.defaultPrice,
            lastUsed = timeService.longAgo,
        )
        scriptAccessRepository.save(scriptAccess)
        sendScriptAccess(userId)
    }

    private fun validateDoesNotAlreadyHaveAccess(userId: String, typeId: String) {
        val accesses = scriptAccessRepository.findByOwnerUserId(userId)
        if (accesses.any { it.typeId == typeId }) {
            val user = userEntityService.getById(userId)
            val scriptType = scriptTypeService.getById(typeId)
            throw ValidationException("${user.name} already has access to ${scriptType.name}.")
        }
    }

    fun deleteAccess(accessId: ScriptAccessId) {
        validateCanManageAccess()
        val access = scriptAccessRepository.findById(accessId).orElseThrow { error("Access not found with id: $accessId, maybe it was already deleted?") }

        scriptAccessRepository.delete(access)
        sendScriptAccess(access.ownerUserId)
    }

    private fun validateCanManageAccess() {
        val userManager = currentUserService.userEntity.type.authorities.contains(ROLE_USER_MANAGER)
        if (!userManager) {
            error("You are not allowed to manage script access.")
        }
    }

    fun editAccess(accessId: ScriptAccessId, receiveForFree: Int, priceInput: Int?) {
        validateCanManageAccess()
        val access = scriptAccessRepository.findById(accessId).orElseThrow { error("Access not found with id: $accessId, maybe it was already deleted?") }

        if (priceInput != null && priceInput < 0) {
            connectionService.replyError("Price cannot be negative.")
            sendScriptAccess(access.ownerUserId)
            return
        }

        val price = if (priceInput == 0) null else priceInput

        val editedAccess = access.copy(receiveForFree = receiveForFree, price = price)
        scriptAccessRepository.save(editedAccess)
        sendScriptAccess(access.ownerUserId)
    }

    fun markUsed(access: ScriptAccess) {
        val updatedAccess = access.copy(lastUsed = timeService.now())
        scriptAccessRepository.save(updatedAccess)
    }

    fun findAll(): List<ScriptAccess> {
        return scriptAccessRepository.findAll().toList()
    }

    fun findByTypeId(typeId: ScriptTypeId): List<ScriptAccess> {
        return scriptAccessRepository.findByTypeId(typeId)
    }

    fun getById(id: ScriptAccessId): ScriptAccess {
        return scriptAccessRepository.findById(id).orElseThrow { error("Script access not found with id: $id") }
    }

    fun deleteByTypeId(scriptTypeId: ScriptTypeId) {
        val accesses = findByTypeId(scriptTypeId)
        scriptAccessRepository.deleteAll(accesses)
    }

    fun copyScriptAccess(fromUserId: String, toUserId: String) {

        val existingAccess = scriptAccessRepository.findByOwnerUserId(toUserId)

        val from = userEntityService.getById(fromUserId)
        val to = userEntityService.getById(toUserId)
        copyScriptAccess(from, to)

        if (existingAccess.isNotEmpty()) {
            connectionService.replyError("${to.name} already has some script access, nothing was overwritten.")
        }
        connectionService.replyNeutral("Copied script access from ${from.name} to ${to.name}.")
    }


    fun copyScriptAccess(from: UserEntity, to: UserEntity) {
        findScriptAccessForUser(from.id).forEach { access ->
            try {
                addScriptAccess(access.typeId, to.id, access.receiveForFree, access.price)
            }
            catch (validationException: ValidationException) {
                connectionService.replyError(validationException.message!!)
            }
        }
    }

    fun deleteAccessForUser(userId: String) {
        findScriptAccessForUser(userId).forEach { access ->
            deleteAccess(access.id)
        }
    }
}
