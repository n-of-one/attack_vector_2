package org.n1.av2.layer.other.tripwire

import org.n1.av2.hacker.hackerstate.HackerStateRunning
import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.platform.connection.ServerActions
import org.n1.av2.platform.util.toDuration
import org.n1.av2.site.entity.NodeEntityService
import org.n1.av2.site.entity.SitePropertiesEntityService
import org.n1.av2.timer.TimerEntityService
import org.n1.av2.timer.TimerService


@org.springframework.stereotype.Service
class TripwireLayerService(
    private val connectionService: ConnectionService,
    private val timerEntityService: TimerEntityService,
    private val nodeEntityService: NodeEntityService,
    private val timerService: TimerService,
    private val sitePropertiesEntityService: SitePropertiesEntityService,
) {

    fun hack(layer: TripwireLayer, hackerState: HackerStateRunning) {
        if (layer.coreLayerId == null) {
            connectionService.replyTerminalReceive("This tripwire is irreversible.")
            return
        }
        val node = nodeEntityService.findByLayerId(layer.coreLayerId!!)
        val coreSiteId =  layer.coreSiteId ?: hackerState.siteId
        if (coreSiteId == hackerState.siteId) {
            connectionService.replyTerminalReceive("This tripwire is managed by core in node [ok]${node.networkId}")
        }
        else {
            val siteName = sitePropertiesEntityService.getBySiteId(coreSiteId).name
            connectionService.replyTerminalReceive("This tripwire is managed by remote core in node [ok]${node.networkId}[/] in site: [info]${siteName}")
        }
    }

    fun hackerArrivesNode(siteId: String, layer: TripwireLayer, nodeId: String, runId: String) {
        val existingTimer = timerEntityService.findByLayer(layer.id)
        if (existingTimer == null) {
            // Only start timer if there was not already a timer active for this tripwire. Cannot trigger twice
            triggerTimer(siteId, layer, nodeId, runId)
        }
    }

    private fun triggerTimer(siteId: String, layer: TripwireLayer, nodeId: String, runId: String) {
        val baseDuration = layer.countdown.toDuration()
        val shutdownDuration = layer.shutdown.toDuration()
        val remote = (layer.coreSiteId != null && layer.coreSiteId != siteId)

        val timerTargetSiteId = siteId
        val timerSiteId = if (remote) layer.coreSiteId!! else siteId

        timerService.startShutdownTimer(timerSiteId, timerTargetSiteId, layer, baseDuration, true, shutdownDuration, "[pri]${layer.level}[/]", null)
        if (remote) {
            connectionService.replyTerminalReceive(" ...timer managed in remote site. Hack layer [pri]${layer.level}[/] to trace site name.")
        }

        connectionService.toRun(runId, ServerActions.SERVER_FLASH_PATROLLER, "nodeId" to nodeId)
    }

}
