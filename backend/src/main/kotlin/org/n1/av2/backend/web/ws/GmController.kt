package org.n1.av2.backend.web.ws

import org.n1.av2.backend.engine.UserTaskRunner
import org.n1.av2.backend.model.iam.UserPrincipal
import org.n1.av2.backend.service.site.SiteService
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller

@Controller
class GmController(
    private val siteService: SiteService,
    private val userTaskRunner: UserTaskRunner,
) {

    @MessageMapping("/siteList")
    fun siteList(siteId: String, userPrincipal: UserPrincipal) {
        userTaskRunner.runTask(userPrincipal) { siteService.sendSitesList() }
    }
}
