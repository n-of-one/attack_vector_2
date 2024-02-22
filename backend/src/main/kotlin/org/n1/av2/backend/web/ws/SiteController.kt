package org.n1.av2.backend.web.ws

import org.n1.av2.backend.engine.UserTaskRunner
import org.n1.av2.backend.model.iam.UserPrincipal
import org.n1.av2.backend.model.ui.NotyMessage
import org.n1.av2.backend.security.ValidSiteId
import org.n1.av2.backend.service.run.RunService
import org.n1.av2.backend.service.site.SiteResetService
import org.n1.av2.backend.service.site.SiteService
import org.n1.av2.backend.service.util.StompService
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller
import org.springframework.validation.annotation.Validated

@Validated
@Controller
class SiteController(
    private val userTaskRunner: UserTaskRunner,
    private val runService: RunService,
    private val siteService: SiteService,
    private val siteResetService: SiteResetService,
    private val stompService: StompService,

) {

    @MessageMapping("/site/resetSite")
    fun resetSite(siteId: String, userPrincipal: UserPrincipal) {
        userTaskRunner.runTask(userPrincipal) {
            runService.gmRefreshSite(siteId)
            siteResetService.refreshSite(siteId, "00:00")
            stompService.replyMessage(NotyMessage.neutral("site reset"))
        }
    }

    @MessageMapping("/site/deleteRuns")
    fun deleteRuns(siteId: String, userPrincipal: UserPrincipal) {
        userTaskRunner.runTask(userPrincipal) {
            siteResetService.refreshSite(siteId, "00:00")
            val count = runService.deleteRuns(siteId)
            stompService.replyMessage(NotyMessage.neutral("Site reset and ${count} runs removed"))
        }
    }

    class UpdateHackable(val siteId: String, val hackable: Boolean)
    @MessageMapping("/site/updateHackable")
    fun updateHackable(command: UpdateHackable, userPrincipal: UserPrincipal) {
        userTaskRunner.runTask(userPrincipal) {
            siteService.updateHackable(command.siteId, command.hackable)
        }
    }

    @MessageMapping("/site/delete")
    fun deleteSite(@ValidSiteId siteId: String, principal: UserPrincipal) {
        userTaskRunner.runTask(principal) {
            siteService.checkIfAllowedToDelete(siteId, principal)
            runService.deleteRuns(siteId)
            siteService.removeSite(siteId)
        }
    }
}