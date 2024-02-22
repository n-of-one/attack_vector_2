package org.n1.av2.backend.web.ws

import org.n1.av2.backend.engine.UserTaskRunner
import org.n1.av2.backend.model.iam.UserPrincipal
import org.n1.av2.backend.service.site.RunLinkService
import org.n1.av2.backend.service.site.SiteService
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller

@Controller
class HackerController(
    private val userTaskRunner: UserTaskRunner,
    private val runLinkService: RunLinkService,
    private val siteService: SiteService,

    ) {

    private val logger = mu.KotlinLogging.logger {}


    @MessageMapping("/hacker/logon")
    fun logon(userPrincipal: UserPrincipal) {
        userTaskRunner.runTask(userPrincipal) {
            runLinkService.sendRunInfosToUser()
            siteService.sendSitesList();
        }
    }

}