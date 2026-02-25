package org.n1.av2.run.terminal.inside

import org.n1.av2.hacker.hackerstate.HackerStateEntityService
import org.n1.av2.hacker.hackerstate.HackerStateRunning
import org.n1.av2.hacker.skill.SkillService
import org.n1.av2.hacker.skill.SkillType
import org.n1.av2.layer.ice.common.IceLayer
import org.n1.av2.layer.other.os.OsLayer
import org.n1.av2.layer.other.timeradjuster.TimerAdjusterLayer
import org.n1.av2.layer.other.timeradjuster.TimerAdjusterService
import org.n1.av2.layer.other.tripwire.TripwireLayer
import org.n1.av2.layer.other.tripwire.TripwireLayerService
import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.platform.connection.ServerActions
import org.n1.av2.platform.engine.ScheduledTask
import org.n1.av2.platform.engine.UserTaskRunner
import org.n1.av2.platform.util.isOneOf
import org.n1.av2.run.entity.NodeScanStatus.*
import org.n1.av2.run.entity.RunEntityService
import org.n1.av2.run.scanning.ScanService
import org.n1.av2.run.terminal.generic.DevCommandHelper
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
    private val timerAdjusterService: TimerAdjusterService,
    private val scanService: ScanService,
    private val userTaskRunner: UserTaskRunner,
    private val connectionService: ConnectionService,
    private val timingsService: TimingsService,
    private val sitePropertiesEntityService: SitePropertiesEntityService,
    private val insideTerminalHelper: InsideTerminalHelper,
    private val devCommandHelper: DevCommandHelper,
    private val skillService: SkillService,
) {

    fun processCommand(arguments: List<String>, hackerState: HackerStateRunning) {
        if (!insideTerminalHelper.verifyInside(hackerState)) return

        if (arguments.isEmpty()) {
            connectionService.replyTerminalReceive("Missing [ok]<network id>[/], for example: [b]mv[ok] 01[/].")
            return
        }
        val networkId = arguments.first()
        val toNode = nodeEntityService.findByNetworkId(hackerState.siteId, networkId) ?: return reportNodeNotFound(networkId)

        if (hackerState.previousNodeId == toNode.id) {
            // can always go back to previous node, regardless of ice
            handleMove(toNode, hackerState, false)
            return
        }

        if (!checkMovePrerequisites(toNode, hackerState, networkId)) return

        requireNotNull(hackerState.currentNodeId)
        val currentNode = nodeEntityService.getById(hackerState.currentNodeId)
        if (hasActiveIce(currentNode)) {
            if (!bypassingIceAtStartNode(currentNode, hackerState)) {
                reportProtected()
                return
            } else {
                connectionService.replyTerminalReceive("[i]Bypassing[/] ICE at ${currentNode.networkId}")
                handleMove(toNode, hackerState, true)
            }
        } else {
            handleMove(toNode, hackerState, false)
        }
    }


    private fun checkMovePrerequisites(toNode: Node, hackerState: HackerStateRunning, networkId: String): Boolean {
        if (toNode.id == hackerState.currentNodeId) return reportAtTargetNode(networkId)

        val scan = runEntityService.getByRunId(hackerState.runId)
        if (scan.nodeScanById[toNode.id] == null || scan.nodeScanById[toNode.id]!!.status.isOneOf(UNDISCOVERED_0, UNCONNECTABLE_1)) {
            reportNodeNotFound(networkId)
            return false
        }
        requireNotNull(hackerState.currentNodeId)
        connectionEntityService.findConnection(hackerState.currentNodeId, toNode.id) ?: return reportNoPath(networkId)

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

    private fun hasActiveIce(currentNode: Node) = currentNode.layers.any { it is IceLayer && !it.hacked }

    private fun bypassingIceAtStartNode(currentNode: Node, hackerState: HackerStateRunning): Boolean {
        val hasBypassSkill = skillService.currentUserHasSkill(SkillType.BYPASS)
        if (!hasBypassSkill) return false


        val siteProperties = sitePropertiesEntityService.getBySiteId(hackerState.siteId)
        val atStartNode = (siteProperties.startNodeNetworkId == currentNode.networkId)

        return atStartNode
    }

    private fun reportProtected(): Boolean {
        connectionService.replyTerminalReceive("[warn b]blocked[/] ICE in current node is blocking your move.")
        return false
    }

    private fun handleMove(toNode: Node, hackerState: HackerStateRunning, bypassingIceAtStartNode: Boolean) {
        connectionService.replyTerminalSetLocked(true)

        val timings = timingsService.skillAndConfigAdjusted(timingsService.MOVE_START, hackerState.userId)

        @Suppress("unused")
        class StartMove(val userId: String, val nodeId: String, val bypassingIceAtStartNode: Boolean, val timings: Timings)
        connectionService.toRun(
            hackerState.runId, ServerActions.SERVER_HACKER_MOVE_START, StartMove(
                hackerState.userId, toNode.id,
                bypassingIceAtStartNode, timings
            )
        )

        userTaskRunner.queueInTicksForSite("move-arrive", hackerState.siteId, timings.totalTicks) {
            moveArrive(
                toNode.id,
                hackerState.userId,
                hackerState.runId
            )
        }
    }

    @ScheduledTask
    fun moveArrive(nodeId: String, userId: String, runId: String) {
        val hackerState = hackerStateEntityService.retrieveForUserId(userId)
        if (hackerState.runId == null || hackerState.runId != runId) return // hacker has left the original run
        val runState = hackerState.toRunState()
        val run = runEntityService.getByRunId(runId)
        if (sitePropertiesEntityService.getBySiteId(run.siteId).shutdownEnd != null) {
            // site has shut down, abort moving
            connectionService.replyTerminalSetLocked(false)
            return
        }

        connectionService.toUser(userId, ServerActions.SERVER_LEAVE_NODE)

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
        val state = hackerStateEntityService.retrieveForUserId(userId).toRunState()
        hackerStateEntityService.arriveAt(state, nodeId)

        val node = nodeEntityService.getById(nodeId)
        val nodeName = (node.layers[0] as OsLayer).nodeName
        val nodeNameText = if (nodeName.isNotBlank()) "[/]: $nodeName" else ""
        connectionService.replyTerminalReceive("Entered node [ok]${node.networkId}$nodeNameText")

        triggerLayersAtArrive(state.siteId, nodeId, runId)

        connectionService.toRun(
            runId, ServerActions.SERVER_HACKER_MOVE_ARRIVE,
            "nodeId" to nodeId, "userId" to userId, "timings" to timingsService.MOVE_ARRIVE
        )

    }

    private fun triggerLayersAtArrive(siteId: String, nodeId: String, runId: String) {
        val node = nodeEntityService.getById(nodeId)
        node.layers.forEach { layer ->
            when (layer) {
                is TripwireLayer -> tripwireLayerService.hackerArrivesNode(siteId, layer, nodeId, runId)
                is TimerAdjusterLayer -> timerAdjusterService.hackerArrivesNode(siteId, layer, nodeId, runId)
                else -> {} // do nothing
            }
        }
    }

    fun processQuickMove(arguments: List<String>, hackerState: HackerStateRunning) {
        if (!devCommandHelper.checkDevModeEnabled()) return
        if (!insideTerminalHelper.verifyInside(hackerState)) return

        if (arguments.isEmpty()) {
            connectionService.replyTerminalReceive("Missing [ok]<network id>[/], for example: [b]mv[ok] 01[/].")
            return
        }
        val networkId = arguments.first()
        val toNode = nodeEntityService.findByNetworkId(hackerState.siteId, networkId) ?: return reportNodeNotFound(networkId)

        moveArrive(toNode.id, hackerState.userId, hackerState.runId)
    }

}
