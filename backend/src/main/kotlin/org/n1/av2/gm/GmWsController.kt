package org.n1.av2.gm

import org.n1.av2.platform.engine.UserTaskRunner
import org.n1.av2.platform.iam.UserPrincipal
import org.n1.av2.site.SiteService
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller

@Controller
class GmWsController(
    private val siteService: SiteService,
    private val userTaskRunner: UserTaskRunner,
) {

    @MessageMapping("/gm/logon")
    fun siteList(siteId: String, userPrincipal: UserPrincipal) {
        userTaskRunner.runTask(userPrincipal) { siteService.sendSitesList() }
    }
}
