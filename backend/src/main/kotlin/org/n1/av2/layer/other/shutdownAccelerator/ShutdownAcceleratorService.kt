package org.n1.av2.layer.other.shutdownAccelerator

import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.platform.connection.ServerActions
import org.n1.av2.platform.util.toDuration
import org.n1.av2.site.entity.NodeEntityService
import org.n1.av2.site.entity.SitePropertiesEntityService
import org.n1.av2.timer.TimerEntityService
import org.n1.av2.timer.TimerService
import org.springframework.stereotype.Service


@Service
class ShutdownAcceleratorService(
    private val connectionService: ConnectionService,
    private val timerService: TimerService,
    private val timerEntityService: TimerEntityService,
) {

    fun hack(layer: ShutdownAcceleratorLayer) {
        connectionService.replyTerminalReceive("Hacking ${layer.name} reveals nothing new.")
    }

    fun hackerArrivesNode(siteId: String, layer: ShutdownAcceleratorLayer, nodeId: String, runId: String) {
        val timers = timerEntityService.findBySiteId(siteId)
        connectionService.toRun(runId, ServerActions.SERVER_FLASH_PATROLLER, "nodeId" to nodeId)

        if (timers.isNotEmpty()) {
            connectionService.replyTerminalReceive("${layer.name} accelerates existing shutdown timers.")

            timers.forEach { timer ->
                timerService.speedUpTimer(timer, layer.increase.toDuration())
            }
        }
    }

}
