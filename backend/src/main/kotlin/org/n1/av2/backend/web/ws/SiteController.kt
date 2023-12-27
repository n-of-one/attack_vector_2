package org.n1.av2.backend.web.ws

import org.n1.av2.backend.engine.UserTaskRunner
import org.n1.av2.backend.entity.run.HackerStateEntityService
import org.n1.av2.backend.model.iam.UserPrincipal
import org.n1.av2.backend.model.ui.NotyMessage
import org.n1.av2.backend.model.validation.UserName
import org.n1.av2.backend.service.run.RunService
import org.n1.av2.backend.service.site.RunLinkService
import org.n1.av2.backend.service.site.SiteService
import org.n1.av2.backend.service.user.UserService
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
    private val stompService: StompService

) {
    //     --- --- --- --- For GMs

    @MessageMapping("/site/resetSite")
    fun resetSite(siteId: String, userPrincipal: UserPrincipal) {
        userTaskRunner.runTask(userPrincipal) {
            runService.gmRefreshSite(siteId)
            siteService.refreshSite(siteId, "00:00")
            stompService.replyMessage(NotyMessage.neutral("site reset"))
        }
    }

    @MessageMapping("/site/deleteRuns")
    fun deleteRuns(siteId: String, userPrincipal: UserPrincipal) {
        userTaskRunner.runTask(userPrincipal) {
            siteService.refreshSite(siteId, "00:00")
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
}