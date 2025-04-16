package org.n1.av2.run.terminal.outside

import org.n1.av2.hacker.hackerstate.HackerActivity
import org.n1.av2.hacker.hackerstate.HackerStateRunning
import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.platform.util.TimeService
import org.n1.av2.site.entity.SitePropertiesEntityService
import org.springframework.stereotype.Service

val CONNECTION_REFUSED_DURING_SITE_RESET = "Connection refused. (site is in shutdown mode)"

@Service
class OutsideTerminalHelper(
    private val sitePropertiesEntityService: SitePropertiesEntityService,
    private val timeService: TimeService,
    private val connectionService: ConnectionService,
) {

    fun verifySiteNotShutdown(siteId: String): Boolean {
        val siteProperties = sitePropertiesEntityService.getBySiteId(siteId)
        val now = timeService.now()
        if (siteProperties.shutdownEnd != null && now < siteProperties.shutdownEnd) {
            connectionService.replyTerminalReceive(CONNECTION_REFUSED_DURING_SITE_RESET)
            return false
        }
        return true
    }

    fun verifyOutside(hackerState: HackerStateRunning): Boolean {
        if (hackerState.activity != HackerActivity.OUTSIDE) {
            connectionService.replyTerminalReceive("This command only works when outside of the site.")
            return false
        }
        return true
    }
}
