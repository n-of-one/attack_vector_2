package org.n1.av2.backend.service.scan

import org.n1.av2.backend.entity.run.*
import org.n1.av2.backend.entity.site.NodeEntityService
import org.n1.av2.backend.entity.site.SitePropertiesEntityService
import org.n1.av2.backend.entity.user.User
import org.n1.av2.backend.entity.user.UserEntityService
import org.n1.av2.backend.model.ui.*
import org.n1.av2.backend.service.CurrentUserService
import org.n1.av2.backend.service.StompService
import org.n1.av2.backend.service.TimeService
import org.n1.av2.backend.service.patroller.PatrollerUiData
import org.n1.av2.backend.service.patroller.TracingPatrollerService
import org.n1.av2.backend.service.run.HackerPresence
import org.n1.av2.backend.service.run.HackerService
import org.n1.av2.backend.service.site.SiteService
import org.n1.av2.backend.service.terminal.ScanTerminalService
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

/** This service deals with the action of scanning (as opposed to the actions performed on a scan). */
@Service
class ScanningService(private val runEntityService: RunEntityService,
                      private val sitePropertiesEntityService: SitePropertiesEntityService,
                      private val siteService: SiteService,
                      private val stompService: StompService,
                      private val nodeEntityService: NodeEntityService,
                      private val hackerStateEntityService: HackerStateEntityService,
                      private val hackerService: HackerService,
                      private val currentUserService: CurrentUserService,
                      private val traverseNodeService: TraverseNodeService,
                      private val scanProbeService: ScanProbeService,
                      private val time: TimeService,
                      private val userEntityService: UserEntityService,
                      private val layerStatusEntityService: LayerStatusEntityService,
                      private val tracingPatrollerService: TracingPatrollerService,
                      private val nodeStatusRepo: NodeStatusRepo
) {

    private val logger = mu.KotlinLogging.logger {}

    lateinit var scanTerminalService: ScanTerminalService

    fun deleteScan(runId: String) {
        runEntityService.deleteUserScan(runId)
        sendScanInfosOfPlayer()
    }

    data class ScanSiteResponse(val runId: String, val siteId: String)

    fun scanSiteForName(siteName: String) {
        val siteProperties = sitePropertiesEntityService.findByName(siteName)
        if (siteProperties == null) {
            stompService.toUser(NotyMessage(NotyType.NEUTRAL, "Error", "Site '${siteName}' not found"))
            return
        }

        val nodeScans = createNodeScans(siteProperties.siteId)

        val user = currentUserService.user

        val runId = runEntityService.createScan(siteProperties, nodeScans, user)
        val response = ScanSiteResponse(runId, siteProperties.siteId)
        stompService.toUser(ReduxActions.SERVER_SITE_DISCOVERED, response)
        sendScanInfosOfPlayer()
    }

    private fun createNodeScans(siteId: String): MutableMap<String, NodeScan> {
        val traverseNodes = traverseNodeService.createTraverseNodesWithDistance(siteId)
        return traverseNodes.map {
            val nodeStatus = when (it.value.distance) {
                1 -> NodeScanStatus.DISCOVERED_1
                else -> NodeScanStatus.UNDISCOVERED_0
            }
            it.key to NodeScan(status = nodeStatus, distance = it.value.distance)
        }.toMap().toMutableMap()
    }



    fun enterScan(runId: String) {
        val scan = runEntityService.getByRunId(runId)
        val thisHackerState = hackerStateEntityService.enterScan(scan.siteId, runId)

        scanTerminalService.sendSyntaxHighlighting()
        stompService.toRun(runId, ReduxActions.SERVER_HACKER_ENTER_SCAN, hackerService.toPresence(thisHackerState))

        val siteFull = siteService.getSiteFull(scan.siteId)
        siteFull.sortNodeByDistance(scan)

        siteFull.nodeStatuses = nodeStatusRepo.findByRunId(runId)
        siteFull.layerStatuses = layerStatusEntityService.getForRun(runId)
        val hackerPresences = hackerService.getPresenceInRun(runId)
        val patrollers = tracingPatrollerService.getAllForRun(runId)

        class ScanAndSite(val run: Run, val site: SiteFull, val hackers: List<HackerPresence>, val patrollers: List<PatrollerUiData>)
        val scanAndSite = ScanAndSite(scan, siteFull, hackerPresences, patrollers)
        stompService.toUser(ReduxActions.SERVER_SCAN_FULL, scanAndSite)
    }

    private fun reportNodeNotFound(networkId: String) {
        if (networkId.length == 1) {
            stompService.terminalReceiveCurrentUser("Node [ok]${networkId}[/] not found. Did you mean: [u]scan [ok]0${networkId}[/] ?")

        } else {
            stompService.terminalReceiveCurrentUser("Node [ok]${networkId}[/] not found.")
        }
    }


    fun sendScanInfosOfPlayer() {
        val userId = currentUserService.userId
        sendScanInfosOfPlayer(userId)
    }

    data class ScanInfo(val runId: String,
                        val siteName: String,
                        val siteId: String,
                        val initiatorName: String,
                        val nodes: String,
                        val efficiency: String)

    fun sendScanInfosOfPlayer(userId: String) {
        val scans = runEntityService.getAll(userId)
        val scanItems = scans.map (::createScanInfo)
        stompService.toUser(userId, ReduxActions.SERVER_RECEIVE_USER_SCANS, scanItems)
    }

    fun createScanInfo(run: Run): ScanInfo {
        val site = sitePropertiesEntityService.getBySiteId(run.siteId)
        val nodes = run.nodeScanById.filterValues { it.status != NodeScanStatus.UNDISCOVERED_0}.size
        val nodesText = if (run.duration != null) "${nodes}" else "${nodes}+"
        val userName = userEntityService.getById(run.initiatorId).name
        val efficiencyStatus = deriveEfficiencyStatus(run)
        return ScanInfo(run.runId, site.name, site.siteId, userName, nodesText, efficiencyStatus)
    }

    private fun deriveEfficiencyStatus(run: Run): String {
        if (run.startTime == null) {
            return "(not started)"
        }
        if (run.efficiency == null) {
            return "(scanning)"
        }
        return "${run.efficiency}%"
    }

    fun shareScan(runId: String, user: User) {
        if (runEntityService.hasUserScan(user, runId)) {
            stompService.terminalReceiveCurrentUser("[info]${user.name}[/] already has this scan.")
            return
        }
        runEntityService.createUserScan(runId, user)
        stompService.terminalReceiveCurrentUser("Shared scan with [info]${user.name}[/].")

        val myUserName = currentUserService.user.name

        val scan = runEntityService.getByRunId(runId)
        val siteProperties = sitePropertiesEntityService.getBySiteId(scan.siteId)

        stompService.toUser(user.id, NotyMessage(NotyType.NEUTRAL, myUserName, "Scan shared for: ${siteProperties.name}"))
        stompService.terminalReceiveForUserForTerminal(user.id, "chat", "[warn]${myUserName}[/] shared scan: [info]${siteProperties.name}[/]")
        sendScanInfosOfPlayer(user.id)
    }

    fun leaveRun(runId: String) {
        hackerStateEntityService.leaveRun()
    }

    fun purgeAll() {
        runEntityService.purgeAll()
    }

    fun launchProbeAtNode(runId: String, networkId: String) {
        val scan = runEntityService.getByRunId(runId)
        val node = nodeEntityService.findByNetworkId(scan.siteId, networkId)
        if (node == null) {
            reportNodeNotFound(networkId)
            return
        }

        val traverseNodesById = traverseNodeService.createTraverseNodesWithDistance(scan)
        val targetNode = traverseNodesById[node.id]!!
        val status = scan.nodeScanById[targetNode.id]!!.status
        if (status == NodeScanStatus.UNDISCOVERED_0) {
            reportNodeNotFound(networkId)
            return
        }
        val scanType = scanProbeService.determineNodeScanType(status) ?: NodeScanType.SCAN_NODE_DEEP
        val path = scanProbeService.createNodePath(targetNode)
        val userId = currentUserService.userId
        val probeAction = ScanProbeService.ProbeAction(probeUserId = userId, path = path, scanType = scanType, autoScan = false, ticks = scanType.ticks)
        stompService.toRun(scan.runId, ReduxActions.SERVER_PROBE_LAUNCH, probeAction)
    }

    fun launchProbe(runId: String, autoScan: Boolean) {
        val scan = runEntityService.getByRunId(runId)
        if (scan.startTime == null) {
            scan.startTime = time.now()
            runEntityService.save(scan)
            updateScanInfoToPlayers(scan)
        }
        val probeAction = scanProbeService.createProbeAction(scan, autoScan)
        if (probeAction != null) {
            stompService.toRun(scan.runId, ReduxActions.SERVER_PROBE_LAUNCH, probeAction)
        } else {
            if (scan.duration == null) {
                completeScan(scan)
            }
            else {
                stompService.terminalReceiveCurrentUser("Scan complete.")
            }
        }
    }

    fun updateScanInfoToPlayers(run: Run) {
        val scanInfo = createScanInfo(run)
        runEntityService.getUsersOfScan(run.runId).forEach{ userId ->
            stompService.toUser(userId, ReduxActions.SERVER_UPDATE_SCAN_INFO, scanInfo)
        }
    }

    private fun completeScan(run: Run) {
        val now = time.now()
        val durationMillis = ChronoUnit.MILLIS.between(run.startTime, now)
        val duration = Duration.ofMillis(durationMillis)
        run.duration = (durationMillis / 1000.0).roundToInt()
        run.efficiency = calculateEfficiency(run, durationMillis)
        runEntityService.save(run)
        stompService.terminalReceiveCurrentUser("Scan completed in ${time.formatDuration(duration)}, with efficiency: ${run.efficiency}%")
        updateScanInfoToPlayers(run)
    }

    private fun calculateEfficiency(run: Run, durationMillis: Long): Int {
        val nodeCount = run.nodeScanById.keys.size

        /*
        Time per node zooming in and out
          calculated: 85 * 0.05 s = 4.25 s
          measured: ~4.26 s
         */

        val timeForNodes = 0.1 + (3 * 4.26) * nodeCount // 0.1 startup cost + 4.26 s per node * 3 scans per node * nodeCount
        val timeForPaths = (1.1 * run.totalDistanceScanned) // 1.1 s per path segment
        val expectedTime = timeForNodes + timeForPaths
        val duration = (durationMillis / 1000.0)
        val efficiency = (100 * expectedTime / duration).toInt()
//        logger.debug("Efficiency calculation:")
//        logger.debug("timeForNodes: ${timeForNodes}")
//        logger.debug("timeForPaths: ${timeForPaths}")
//        logger.debug("expectedTime: ${expectedTime}")
//        logger.debug("duration: ${duration}")
//        logger.debug("efficiency: ${efficiency}")
        return efficiency
    }

    fun quickScan(runId: String) {
        val scan = runEntityService.getByRunId(runId)
        val nodes = nodeEntityService.getAll(scan.siteId)
        nodes.forEach { scanProbeService.quickScanNode(it, scan) }
        autoScan(runId)
   }

    fun probeArrive(runId: String, nodeId: String, action: NodeScanType, autoScan: Boolean) {
        val updateScanInfo = scanProbeService.probeArrive(runId, nodeId, action, autoScan)
        if (updateScanInfo) {
            val scan = runEntityService.getByRunId(runId)
            updateScanInfoToPlayers(scan)
        }
        if (autoScan) {
            autoScan(runId)
        }
    }

    fun autoScan(runId: String) {
        launchProbe(runId, true)
    }


}