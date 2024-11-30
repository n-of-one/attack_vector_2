package org.n1.av2.script

import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.platform.iam.user.CurrentUserService
import org.n1.av2.platform.iam.user.ROLE_USER_MANAGER
import org.n1.av2.platform.iam.user.UserAndHackerService
import org.n1.av2.platform.util.TimeService
import org.n1.av2.platform.util.createId
import org.n1.av2.platform.util.toHumanTime
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.Duration
import java.time.temporal.ChronoUnit
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
) {

    lateinit var userAndHackerService: UserAndHackerService

    fun findScriptAccessForUser(userId: String): List<ScriptAccess> {
        return scriptAccessRepository.findByOwnerUserId(userId)
    }

    fun addScriptAccess(typeId: String, userId: String) {
        val id = createId("access", scriptAccessRepository::findById)

        val scriptAccess = ScriptAccess(
            id = id,
            ownerUserId = userId,
            typeId = typeId,
            receiveForFree = 0,
            price = null
        )
        scriptAccessRepository.save(scriptAccess)
        userAndHackerService.sendDetailsOfSpecificUser(userId)
    }

    fun deleteAccess(accessId: String) {
        val access= scriptAccessRepository.findById(accessId).orElseThrow { error("Access not found with id: $accessId, maybe it was already deleted?") }
        validateCanManage(access.ownerUserId)

        scriptAccessRepository.delete(access)
        userAndHackerService.sendDetailsOfSpecificUser(access.ownerUserId)
    }

    private fun validateCanManage(ownerId: String) {
        val userManager = currentUserService.userEntity.type.authorities.contains(ROLE_USER_MANAGER)
        if (!userManager && ownerId != currentUserService.userId) {
            error("You are not allowed to manage this Script access.")
        }
    }

    fun editAccess(accessId: String, receiveForFree: Int, price: BigDecimal?) {
        val access= scriptAccessRepository.findById(accessId).orElseThrow { error("Access not found with id: $accessId, maybe it was already deleted?") }
        validateCanManage(access.ownerUserId)

        if (price == BigDecimal.ZERO) {
            connectionService.replyError("You have set the price to 0, this means the script is free and hackers can buy unlimited number of copies. " +
                "If you want to make it so that hackers cannot buy this script, clear the price field instead.")
        }

        val editedAccess = access.copy( receiveForFree = receiveForFree, price = price)
        scriptAccessRepository.save(editedAccess)
        userAndHackerService.sendDetailsOfSpecificUser(access.ownerUserId)
    }

}
