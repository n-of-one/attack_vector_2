package org.n1.av2.site

import org.n1.av2.frontend.model.NotyMessage
import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.platform.connection.ServerActions
import org.n1.av2.platform.engine.UserTaskRunner
import org.n1.av2.platform.iam.UserPrincipal
import org.n1.av2.platform.inputvalidation.ValidSiteId
import org.n1.av2.platform.util.ServerFatal
import org.n1.av2.run.RunService
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller
import org.springframework.validation.annotation.Validated

@Validated
@Controller
class SiteWsController(
    private val userTaskRunner: UserTaskRunner,
    private val runService: RunService,
    private val siteService: SiteService,
    private val siteResetService: SiteResetService,
    private val connectionService: ConnectionService,
) {

    @MessageMapping("/site/resetSite")
    fun resetSite(siteId: String, userPrincipal: UserPrincipal) {
        userTaskRunner.runTask("/site/resetSite", userPrincipal) {
            runService.gmRefreshSite(siteId)
            siteResetService.refreshSite(siteId)
            connectionService.replyMessage(NotyMessage.neutral("site reset"))
        }
    }

    @MessageMapping("/site/deleteRuns")
    fun deleteRuns(siteId: String, userPrincipal: UserPrincipal) {
        userTaskRunner.runTask("/site/deleteRuns", userPrincipal) {
            siteResetService.refreshSite(siteId)
            val count = runService.deleteRuns(siteId)
            connectionService.replyMessage(NotyMessage.neutral("Site reset and ${count} runs removed"))
        }
    }

    class UpdateHackable(val siteId: String, val hackable: Boolean)
    @MessageMapping("/site/updateHackable")
    fun updateHackable(command: UpdateHackable, userPrincipal: UserPrincipal) {
        userTaskRunner.runTask("/site/updateHackable", userPrincipal) {
            siteService.updateHackable(command.siteId, command.hackable)
        }
    }

    @MessageMapping("/site/delete")
    fun deleteSite(@ValidSiteId siteId: String, principal: UserPrincipal) {
        userTaskRunner.runTask("/site/delete", principal) {
            siteService.checkIfAllowedToDelete(siteId, principal)
            connectionService.toSite(
                siteId,
                ServerActions.SERVER_ERROR,
                ServerFatal(false, "Site removed by ${principal.userEntity.name}, please close browser window.")
            )

            runService.deleteRuns(siteId)
            siteService.removeSite(siteId)
        }
    }

    @MessageMapping("/site/makeCopy")
    fun copySite(@ValidSiteId siteId: String, principal: UserPrincipal) {
        userTaskRunner.runTask("/site/makeCopy", principal) {
            siteService.checkSiteOwnership(siteId, principal)
            siteService.copySite(siteId)
        }
    }
}
