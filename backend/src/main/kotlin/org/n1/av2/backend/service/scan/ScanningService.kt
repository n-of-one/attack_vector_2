package org.n1.av2.backend.service.scan

import org.n1.av2.backend.engine.TICK_MILLIS
import org.n1.av2.backend.engine.TaskRunner
import org.n1.av2.backend.entity.run.NodeScan
import org.n1.av2.backend.entity.run.NodeScanStatus
import org.n1.av2.backend.entity.run.Run
import org.n1.av2.backend.entity.run.RunEntityService
import org.n1.av2.backend.entity.site.LayoutEntityService
import org.n1.av2.backend.entity.site.Node
import org.n1.av2.backend.entity.site.NodeEntityService
import org.n1.av2.backend.model.Timings
import org.n1.av2.backend.model.ui.NodeScanType
import org.n1.av2.backend.model.ui.ServerActions
import org.n1.av2.backend.service.user.CurrentUserService
import org.n1.av2.backend.service.StompService
import org.n1.av2.backend.service.TimeService
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.math.roundToInt

@Service
class ScanningService(
    private val runEntityService: RunEntityService,
    private val stompService: StompService,
    private val nodeEntityService: NodeEntityService,
    private val currentUserService: CurrentUserService,
    private val traverseNodeService: TraverseNodeService,
    private val time: TimeService,
    private val taskRunner: TaskRunner,
    private val scanInfoService: ScanInfoService,
    private val scanProbActionService: ScanProbActionService,
    private val layoutEntityService: LayoutEntityService,
) {

    private val logger = mu.KotlinLogging.logger {}

    fun performAutoScan(runId: String) {
        val run = runEntityService.getByRunId(runId)
        val targetTraverseNode = findProbeTarget(run)
        if (targetTraverseNode != null) {
            launchProbe(targetTraverseNode, run, true)
        }
        else {
            stompService.replyTerminalReceiveAndLocked(false, "Scan already complete")
        }
    }

    fun performManualScan(run: Run, node: Node, status: NodeScanStatus) {
        if (run.scanDuration != null ) {
            stompService.replyTerminalReceiveAndLocked(false, "Scan already complete")
            return
        }
        val traverseNodesById = traverseNodeService.createTraverseNodesWithDistance(run)
        val targetTraverseNode: TraverseNode = traverseNodesById[node.id]!!
        launchProbe(targetTraverseNode, run, false)
    }

    data class ProbeAction(val probeUserId: String, val path: List<String>, val scanType: NodeScanType, val timings: Timings)

    private fun launchProbe(targetNode: TraverseNode, run: Run, autoScan: Boolean) {
        val status = run.nodeScanById[targetNode.id]!!.status
        val scanType = determineNodeScanType(status)
        val path = createNodePath(targetNode)
        val userId = currentUserService.userId
        val node = nodeEntityService.findById(targetNode.id)

        val timings = scanType.timings
        val probeAction = ProbeAction(userId, path, scanType, timings)

        stompService.toRun(run.runId, ServerActions.SERVER_PROBE_LAUNCH, probeAction)

        val totalTicks = (path.size) * timings.connection + timings.totalWithoutConnection
        taskRunner.queueInTicks(totalTicks - 20) {
            scanProbActionService.probeCompleted(run, node, scanType)

            if (allNodesScanned(run)) {
                completeScan(run)
            } else if (autoScan) {
                performAutoScan(run.runId)
            }
        }
    }

    fun determineNodeScanType(status: NodeScanStatus): NodeScanType {
        return when (status) {
            NodeScanStatus.DISCOVERED_1 -> NodeScanType.SCAN_NODE_INITIAL
            NodeScanStatus.TYPE_KNOWN_2 -> NodeScanType.SCAN_CONNECTIONS
            NodeScanStatus.CONNECTIONS_KNOWN_3 -> NodeScanType.SCAN_NODE_DEEP
            NodeScanStatus.FULLY_SCANNED_4 -> NodeScanType.SCAN_NODE_DEEP
            NodeScanStatus.UNDISCOVERED_0 -> error("Cannot scan a node that has not yet been discovered.")
        }
    }

    fun createNodePath(targetNode: TraverseNode): LinkedList<String> {
        val path = LinkedList<String>()
        path.add(targetNode.id)
        var currentNode = targetNode
        while (currentNode.distance != 1) {
            currentNode = currentNode.connections.find { it.distance == (currentNode.distance!! - 1) }!!
            path.add(0, currentNode.id)
        }
        return path
    }

    /**
     * Of all nodes that are know to the players, find the one of which the least is known (lowest scan status level).
     */
    private fun findProbeTarget(run: Run): TraverseNode? {
        val traverseNodeValues = traverseNodeService.createTraverseNodesWithDistance(run).values
        val traverseNodes = traverseNodeValues.filter { run.nodeScanById[it.id]!!.status != NodeScanStatus.UNDISCOVERED_0 }
        val distanceSortedNodes = traverseNodes.sortedBy { it.distance }
        val targetNode = distanceSortedNodes.minBy {
            run.nodeScanById[it.id]!!.status.level
        }
        return if (run.nodeScanById[targetNode.id]!!.status == NodeScanStatus.FULLY_SCANNED_4) {
            null
        } else targetNode
    }


    private fun allNodesScanned(run: Run): Boolean {
        return run.nodeScanById
            .filter { it.value.status !== NodeScanStatus.FULLY_SCANNED_4 }
            .isEmpty()
    }

    private fun completeScan(run: Run) {
        val now = time.now()
        val durationMillis = ChronoUnit.MILLIS.between(run.scanStartTime, now)
        val duration = Duration.ofMillis(durationMillis)
        run.scanDuration = (durationMillis / 1000.0).roundToInt()
        run.scanEfficiency = calculateEfficiency(run, durationMillis)
        runEntityService.save(run)
        stompService.replyTerminalReceive("Scan completed in ${time.formatDuration(duration)}, with efficiency: ${run.scanEfficiency}%")
        scanInfoService.updateScanInfoToPlayers(run)
    }

    private fun calculateEfficiency(run: Run, durationMillis: Long): Int {
        val nodeCount = run.nodeScanById.keys.size
        val scanTicksTravel = run.totalDistanceScanned * NodeScanType.SCAN_CONNECTIONS.timings.connection
        val scanTicksInitial = nodeCount * NodeScanType.SCAN_NODE_INITIAL.timings.totalWithoutConnection
        val scanTicksConnections = nodeCount * NodeScanType.SCAN_CONNECTIONS.timings.totalWithoutConnection
        val scanTicksDeep = nodeCount * NodeScanType.SCAN_NODE_DEEP.timings.totalWithoutConnection

        val totalTicsk = scanTicksTravel + scanTicksInitial + scanTicksConnections + scanTicksDeep
        val minimumScanMillis = (totalTicsk * TICK_MILLIS)


        val efficiency = (100 * minimumScanMillis / durationMillis).toInt()
        logger.debug("Efficiency calculation:")
        logger.debug("timeForNodes: ${scanTicksInitial + scanTicksConnections + scanTicksDeep}")
        logger.debug("timeForPaths: ${scanTicksTravel}")
        logger.debug("expectedTime: ${minimumScanMillis}")
        logger.debug("duration: ${durationMillis}")
        logger.debug("efficiency: ${efficiency}")
        return efficiency
    }

    fun quickScan(runId: String) {
        val run = runEntityService.getByRunId(runId)

        val layout = layoutEntityService.getBySiteId(run.siteId)

        layout.nodeIds.forEach {
            val oldStatus = run.nodeScanById[it]
            run.nodeScanById[it] = NodeScan(NodeScanStatus.FULLY_SCANNED_4, oldStatus!!.distance)
        }
        runEntityService.save(run)

        val nodeStatusById = layout.nodeIds.map {
            it to NodeScanStatus.FULLY_SCANNED_4
        }.toMap()

        stompService.toRun(run.runId, ServerActions.SERVER_DISCOVER_NODES, ProbeResultConnections(nodeStatusById, layout.connectionIds))
        scanInfoService.updateScanInfoToPlayers(run)
    }


}