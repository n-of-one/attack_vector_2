package org.n1.av2.hacker

import org.n1.av2.platform.engine.UserTaskRunner
import org.n1.av2.platform.iam.UserPrincipal
import org.n1.av2.run.runlink.RunLinkService
import org.n1.av2.site.SiteService
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller

@Controller
class HackerWsController(
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
