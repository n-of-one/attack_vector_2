package org.n1.av2.run.terminal.inside

import org.n1.av2.hacker.hackerstate.HackerStateEntityService
import org.n1.av2.hacker.hackerstate.HackerStateRunning
import org.n1.av2.layer.ice.common.IceLayer
import org.n1.av2.layer.other.tripwire.TripwireLayer
import org.n1.av2.layer.other.tripwire.TripwireLayerService
import org.n1.av2.platform.config.ConfigItem
import org.n1.av2.platform.config.ConfigService
import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.platform.connection.ServerActions
import org.n1.av2.platform.engine.ScheduledTask
import org.n1.av2.platform.engine.UserTaskRunner
import org.n1.av2.platform.util.isOneOf
import org.n1.av2.run.entity.NodeScanStatus.*
import org.n1.av2.run.entity.RunEntityService
import org.n1.av2.run.scanning.ScanService
import org.n1.av2.run.timings.Timings
import org.n1.av2.run.timings.TimingsService
import org.n1.av2.site.entity.ConnectionEntityService
import org.n1.av2.site.entity.Node
import org.n1.av2.site.entity.NodeEntityService
import org.n1.av2.site.entity.SitePropertiesEntityService
import org.springframework.stereotype.Service


@Service
class CommandMoveService(
    private val nodeEntityService: NodeEntityService,
    private val connectionEntityService: ConnectionEntityService,
    private val hackerStateEntityService: HackerStateEntityService,
    private val runEntityService: RunEntityService,
    private val tripwireLayerService: TripwireLayerService,
    private val scanService: ScanService,
    private val userTaskRunner: UserTaskRunner,
    private val connectionService: ConnectionService,
    private val timingsService: TimingsService,
    private val configService: ConfigService,
    private val sitePropertiesEntityService: SitePropertiesEntityService,
) {

    fun processCommand(runId: String, tokens: List<String>, state: HackerStateRunning) {
        if (tokens.size == 1) {
            connectionService.replyTerminalReceive("Missing [ok]<network id>[/], for example: [b]mv[ok] 01[/].")
            return
        }
        val networkId = tokens[1]
        val toNode = nodeEntityService.findByNetworkId(state.siteId, networkId) ?: return reportNodeNotFound(networkId)

        if (checkMovePrerequisites(toNode, state, networkId, runId)) {
            handleMove(runId, toNode, state)
        }
    }

    private fun checkMovePrerequisites(toNode: Node, state: HackerStateRunning, networkId: String, runId: String): Boolean {
        if (toNode.id == state.currentNodeId) return reportAtTargetNode(networkId)

        if (state.previousNodeId == toNode.id) return true /// can always go back to previous node, regardless of ice

        val scan = runEntityService.getByRunId(runId)
        if (scan.nodeScanById[toNode.id] == null || scan.nodeScanById[toNode.id]!!.status.isOneOf(UNDISCOVERED_0, UNCONNECTABLE_1)) {
            reportNodeNotFound(networkId)
            return false
        }

        connectionEntityService.findConnection(state.currentNodeId, toNode.id) ?: return reportNoPath(networkId)
        val fromNode = nodeEntityService.getById(state.currentNodeId)
        if (hasActiveIce(fromNode)) return reportProtected()

        return true
    }

    private fun reportAtTargetNode(networkId: String): Boolean {
        connectionService.replyTerminalReceive("[error]error[/] already at [ok]${networkId}[/].")
        return false
    }

    fun reportNodeNotFound(networkId: String) {
        connectionService.replyTerminalReceive("[error]error[/] node [ok]${networkId}[/] not found.")
    }

    fun reportNoPath(networkId: String): Boolean {
        connectionService.replyTerminalReceive("[error]error[/] no path from current node to [ok]${networkId}[/].")
        return false
    }

    private fun hasActiveIce(node: Node): Boolean {
        return node.layers.any { it is IceLayer && !it.hacked }

    }

    private fun reportProtected(): Boolean {
        connectionService.replyTerminalReceive("[warn b]blocked[/] ICE in current node is blocking your move.")
        return false
    }

    private fun handleMove(runId: String, toNode: Node, state: HackerStateRunning) {
        connectionService.replyTerminalSetLocked(true)

        @Suppress("unused")
        class StartMove(val userId: String, val nodeId: String, val timings: Timings)
        connectionService.toRun(runId, ServerActions.SERVER_HACKER_MOVE_START, StartMove(state.userId, toNode.id, timingsService.MOVE_START))

        userTaskRunner.queueInTicksForSite("move-arrive", state.siteId, timingsService.MOVE_START.totalTicks) { moveArrive(toNode.id, state.userId, runId) }
    }

    @ScheduledTask
    fun moveArrive(nodeId: String, userId: String, runId: String) {
        val hackerState = hackerStateEntityService.retrieve(userId)
        if (hackerState.runId == null || hackerState.runId != runId) return // hacker has left the original run
        val runState = hackerState.toRunState()
        val run = runEntityService.getByRunId(runId)
        if (sitePropertiesEntityService.getBySiteId(run.siteId).shutdownEnd != null) {
            // site has shut down, abort moving
            connectionService.replyTerminalSetLocked(false)
            return
        }

        val nodeStatus = run.nodeScanById[nodeId]!!.status

        if (nodeStatus.isOneOf(FULLY_SCANNED_4, ICE_PROTECTED_3)) {
            arriveComplete(nodeId, userId, runId)
        } else {
            connectionService.toRun(
                runId,
                ServerActions.SERVER_HACKER_SCANS_NODE,
                "userId" to userId,
                "nodeId" to nodeId,
                "timings" to timingsService.INSIDE_SCAN
            )

            userTaskRunner.queueInTicksForSite("internal-scan", runState.siteId, timingsService.INSIDE_SCAN.totalTicks) {
                scanService.hackerArrivedNodeScan(nodeId, runId)
                arriveComplete(nodeId, userId, runId)
            }
        }
    }

    private fun arriveComplete(nodeId: String, userId: String, runId: String) {
        val state = hackerStateEntityService.retrieve(userId).toRunState()
        hackerStateEntityService.arriveAt(state, nodeId)
        triggerLayersAtArrive(state.siteId, nodeId, runId)

        connectionService.toRun(
            runId, ServerActions.SERVER_HACKER_MOVE_ARRIVE,
            "nodeId" to nodeId, "userId" to userId, "timings" to timingsService.MOVE_ARRIVE
        )
        connectionService.replyTerminalSetLocked(false)
    }

    private fun triggerLayersAtArrive(siteId: String, nodeId: String, runId: String) {
        val node = nodeEntityService.getById(nodeId)
        node.layers.forEach { layer ->
            when (layer) {
                is TripwireLayer -> tripwireLayerService.hackerArrivesNode(siteId, layer, nodeId, runId)
                else -> {} // do nothing
            }
        }
    }

    fun processQuickMove(runId: String, tokens: List<String>, state: HackerStateRunning) {
        if (!configService.getAsBoolean(ConfigItem.DEV_HACKER_USE_DEV_COMMANDS)) {
            connectionService.replyTerminalReceive("QuickMove is not enabled outside of development.")
            return
        }

        if (tokens.size == 1) {
            connectionService.replyTerminalReceive("Missing [ok]<network id>[/], for example: [b]mv[ok] 01[/].")
            return
        }
        val networkId = tokens[1]
        val toNode = nodeEntityService.findByNetworkId(state.siteId, networkId) ?: return reportNodeNotFound(networkId)

        moveArrive(toNode.id, state.userId, runId)
    }

}
