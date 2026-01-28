package org.n1.av2.hacker

import org.n1.av2.platform.config.ConfigService
import org.n1.av2.platform.engine.UserTaskRunner
import org.n1.av2.platform.iam.UserPrincipal
import org.n1.av2.platform.iam.user.CurrentUserService
import org.n1.av2.platform.iam.user.UserAndHackerService
import org.n1.av2.run.runlink.RunLinkService
import org.n1.av2.script.ScriptService
import org.n1.av2.script.access.ScriptAccessService
import org.n1.av2.site.SiteService
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller

@Controller
class HackerWsController(
    private val userTaskRunner: UserTaskRunner,
    private val runLinkService: RunLinkService,
    private val siteService: SiteService,
    private val configService: ConfigService,
    private val userAndHackerService: UserAndHackerService,
    private val scriptService: ScriptService,
    private val scriptAccessService: ScriptAccessService,
    private val currentUserService: CurrentUserService,

    ) {

    @PreAuthorize("hasRole('ROLE_HACKER')")
    @MessageMapping("/hacker/logon")
    fun logon(userPrincipal: UserPrincipal) {
        userTaskRunner.runTask("/hacker/logon", userPrincipal) {
            runLinkService.sendRunInfosToUser()
            configService.replyConfigValues()
            siteService.sendSitesList()
            scriptService.sendScriptStatusToCurrentUser()
            scriptAccessService.sendScriptAccess(currentUserService.userId)

            // This will trigger the rendering of the page, send last so that no page updates happen after.
            userAndHackerService.sendDetailsOfCurrentUser()
        }
    }

}
