package org.n1.av2.script.access

import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.platform.connection.ServerActions
import org.n1.av2.platform.iam.user.CurrentUserService
import org.n1.av2.platform.iam.user.ROLE_USER_MANAGER
import org.n1.av2.platform.iam.user.UserAndHackerService
import org.n1.av2.platform.util.createId
import org.n1.av2.script.type.ScriptTypeService
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Service
import java.math.BigDecimal
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
) {

    lateinit var userAndHackerService: UserAndHackerService

    fun findScriptAccessForUser(userId: String): List<ScriptAccess> {
        return scriptAccessRepository.findByOwnerUserId(userId)
    }

    class ScriptAccessUi(
        val id: String,
        val type: ScriptTypeUi,
        val receiveForFree: Int,
        val price: BigDecimal?,
    )

    class ScriptTypeUi(
        val id: String,
        val name: String,
        val ram: Int,
        val effects: List<String>
    )

    fun sendScriptAccess(userId: String) {
        val scriptAccesses = findScriptAccessForUser(userId)
        val scriptAccessUis = scriptAccesses.map { scriptAccess ->
            val type = scriptTypeService.getById(scriptAccess.typeId)
            val uiType = ScriptTypeUi(
                id = type.id,
                name = type.name,
                ram = type.ram,
                effects = type.effects.map { it.playerDescription }
            )
            ScriptAccessUi(
                id = scriptAccess.id,
                type = uiType,
                receiveForFree = scriptAccess.receiveForFree,
                price = scriptAccess.price,
            )
        }
        connectionService.reply(ServerActions.SERVER_RECEIVE_SCRIPT_ACCESS, scriptAccessUis)
    }

    fun addScriptAccess(typeId: String, userId: String) {
        validateCanManageAccess()
        val id = createId("access", scriptAccessRepository::findById)
        val type = scriptTypeService.getById(typeId)

        val scriptAccess = ScriptAccess(
            id = id,
            ownerUserId = userId,
            typeId = typeId,
            receiveForFree = 0,
            price = type.defaultPrice,
            used = false,
        )
        scriptAccessRepository.save(scriptAccess)
        sendScriptAccess(userId)
    }

    fun deleteAccess(accessId: String) {
        validateCanManageAccess()
        val access= scriptAccessRepository.findById(accessId).orElseThrow { error("Access not found with id: $accessId, maybe it was already deleted?") }

        scriptAccessRepository.delete(access)
        sendScriptAccess(access.ownerUserId)
    }

    private fun validateCanManageAccess() {
        val userManager = currentUserService.userEntity.type.authorities.contains(ROLE_USER_MANAGER)
        if (!userManager) {
            error("You are not allowed to manage script access.")
        }
    }

    fun editAccess(accessId: String, receiveForFree: Int, price: BigDecimal?) {
        validateCanManageAccess()
        val access= scriptAccessRepository.findById(accessId).orElseThrow { error("Access not found with id: $accessId, maybe it was already deleted?") }

        if (price != null && price < BigDecimal.ZERO) {
            connectionService.replyError("Price cannot be negative.")
            sendScriptAccess(access.ownerUserId)
            return
        }

        if (price == BigDecimal.ZERO) {
            connectionService.replyError("You have set the price to 0, this means the script is free and hackers can buy unlimited number of copies. " +
                "If you want to make it so that hackers cannot buy this script, clear the price field instead.")
            sendScriptAccess(access.ownerUserId)
            return
        }

        val editedAccess = access.copy( receiveForFree = receiveForFree, price = price)
        scriptAccessRepository.save(editedAccess)
        sendScriptAccess(access.ownerUserId)
    }

}
