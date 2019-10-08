package org.n1.av2.backend.service.layer

import org.n1.av2.backend.engine.TicksGameEvent
import org.n1.av2.backend.engine.TimedEventQueue
import org.n1.av2.backend.model.Ticks
import org.n1.av2.backend.model.db.layer.Layer
import org.n1.av2.backend.model.db.run.HackerPosition
import org.n1.av2.backend.model.db.run.NodeScanStatus
import org.n1.av2.backend.model.db.site.Node
import org.n1.av2.backend.model.ui.ReduxActions
import org.n1.av2.backend.service.CurrentUserService
import org.n1.av2.backend.service.StompService
import org.n1.av2.backend.service.scan.ScanProbeService
import org.n1.av2.backend.service.scan.ScanService
import org.n1.av2.backend.service.site.NodeService

private val SCAN_CONNECTIONS_TICKS = Ticks(
        "out" to 4,
        "start" to 50,
        "finish" to 50,
        "back" to 4)
class HackedOsGameEvent(val layerId: String, val nodeId: String, val userId: String, val runId: String,
                        ticks: Ticks = SCAN_CONNECTIONS_TICKS): TicksGameEvent(ticks)

@org.springframework.stereotype.Service
class OsLayerService(
        private val scanService: ScanService,
        private val hackedUtil: HackedUtil,
        private val nodeService: NodeService,
        private val probeService: ScanProbeService,
        private val currentUser: CurrentUserService,
        private val timedEventQueue: TimedEventQueue,
        private val stompService: StompService) {


    fun hack(layer: Layer, node: Node, position: HackerPosition) {
        val scan = scanService.getByRunId(position.runId)
        val nodeStatus = scan.nodeScanById[node.id]!!.status

        if (nodeStatus != NodeScanStatus.LAYERS) {
            class ProbeConnections(val nodeId: String, val userId: String)
            val data = ProbeConnections(node.id, position.userId)
            stompService.toRun(position.runId, ReduxActions.SERVER_HACKER_PROBE_CONNECTIONS, data)

            val event = HackedOsGameEvent(layer.id, node.id, currentUser.userId, position.runId)
            timedEventQueue.queueInTicks(event)
        }
        else {
            stompService.terminalReceive("Hacking ${layer.name} reveals nothing new.")
        }
    }


    fun probedConnections(event: HackedOsGameEvent) {
        val node = nodeService.getById(event.nodeId)
        hackedUtil.nonIceHacked(event.layerId, node, event.runId)

        val scan = scanService.getByRunId(event.runId)
        val nodeScan = scan.nodeScanById[event.nodeId]!!
        val layer = node.layers.first()
        val prefix = "Hacked: [pri]0[/] ${layer.name}"
        probeService.probeScanConnection(scan, node, nodeScan, prefix)
    }

}