package org.n1.av2.gm

import org.n1.av2.platform.engine.UserTaskRunner
import org.n1.av2.platform.iam.UserPrincipal
import org.n1.av2.site.SiteService
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller

@Controller
class GmWsController(
    private val siteService: SiteService,
    private val userTaskRunner: UserTaskRunner,
) {

    @PreAuthorize("hasRole('ROLE_GM')")
    @MessageMapping("/gm/logon")
    fun siteList(userPrincipal: UserPrincipal) {
        userTaskRunner.runTask(userPrincipal) { siteService.sendSitesList() }
    }
}
