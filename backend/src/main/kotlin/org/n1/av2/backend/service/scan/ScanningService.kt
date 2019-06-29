package org.n1.av2.backend.service.scan

import mu.KLogging
import org.n1.av2.backend.model.db.run.NodeScan
import org.n1.av2.backend.model.db.run.NodeStatus
import org.n1.av2.backend.model.db.run.Scan
import org.n1.av2.backend.model.db.user.User
import org.n1.av2.backend.model.hacker.HackerActivityType
import org.n1.av2.backend.model.hacker.HackerPresence
import org.n1.av2.backend.model.ui.NodeScanType
import org.n1.av2.backend.model.ui.NotyMessage
import org.n1.av2.backend.model.ui.NotyType
import org.n1.av2.backend.model.ui.SiteFull
import org.n1.av2.backend.service.CurrentUserService
import org.n1.av2.backend.service.ReduxActions
import org.n1.av2.backend.service.StompService
import org.n1.av2.backend.service.TimeService
import org.n1.av2.backend.service.site.NodeService
import org.n1.av2.backend.service.site.SiteDataService
import org.n1.av2.backend.service.site.SiteService
import org.n1.av2.backend.service.terminal.ScanTerminalService
import org.n1.av2.backend.service.user.HackerActivityService
import org.n1.av2.backend.service.user.UserService
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

/** This service deals with the action of scanning (as opposed to the actions performed on a scan). */
@Service
class ScanningService(val scanService: ScanService,
                      val siteDataService: SiteDataService,
                      val siteService: SiteService,
                      val stompService: StompService,
                      val nodeService: NodeService,
                      val hackerActivityService: HackerActivityService,
                      val currentUserService: CurrentUserService,
                      val traverseNodeService: TraverseNodeService,
                      val scanProbeService: ScanProbeService,
                      val time: TimeService,
                      val userService: UserService
) {

    companion object: KLogging()

    lateinit var scanTerminalService: ScanTerminalService

    fun deleteScan(runId: String) {
        scanService.deleteUserScan(runId)
        sendScanInfosOfPlayer()
    }

    data class ScanSiteResponse(val runId: String, val siteId: String)

    fun scanSiteForName(siteName: String) {
        val siteData = siteDataService.findByName(siteName)
        if (siteData == null) {
            stompService.toUser(NotyMessage(NotyType.NEUTRAL, "Error", "Site '${siteName}' not found"))
            return
        }

        val nodeScans = createNodeScans(siteData.siteId)

        val user = currentUserService.user

        val runId = scanService.createScan(siteData, nodeScans, user)
        val response = ScanSiteResponse(runId, siteData.siteId)
        stompService.toUser(ReduxActions.SERVER_SITE_DISCOVERED, response)
        sendScanInfosOfPlayer()
    }

    private fun createNodeScans(siteId: String): MutableMap<String, NodeScan> {
        val traverseNodes = traverseNodeService.createTraverseNodesWithDistance(siteId)
        return traverseNodes.map {
            val nodeStatus = when (it.value.distance) {
                1 -> NodeStatus.DISCOVERED
                else -> NodeStatus.UNDISCOVERED
            }
            it.key to NodeScan(status = nodeStatus, distance = it.value.distance)
        }.toMap().toMutableMap()
    }


    data class ScanAndSite(val scan: Scan, val site: SiteFull, val hackers: List<HackerPresence>)

    fun enterScan(runId: String) {
        hackerActivityService.startActivityScanning(runId)
        scanTerminalService.sendSyntaxHighlighting()
        stompService.toRun(runId, ReduxActions.SERVER_HACKER_ENTER_SCAN, createPresence(currentUserService.user))

        val scan = scanService.getByRunId(runId)
        val siteFull = siteService.getSiteFull(scan.siteId)
        siteFull.sortNodeByDistance(scan)
        val userPresence = hackerActivityService
                .getAll(scan.runId, HackerActivityType.SCANNING)
                .map { activity -> createPresence(activity.authentication.user) }

        val scanAndSite = ScanAndSite(scan, siteFull, userPresence)
        stompService.toUser(ReduxActions.SERVER_SCAN_FULL, scanAndSite)
    }

    private fun reportNodeNotFound(networkId: String) {
        if (networkId.length == 1) {
            stompService.terminalReceive("Node [ok]${networkId}[/] not found. Did you mean: [u]scan [ok]0${networkId}[/] ?")

        } else {
            stompService.terminalReceive("Node [ok]${networkId}[/] not found.")
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
        val scans = scanService.getAll(userId)
        val scanItems = scans.map (::createScanInfo)
        stompService.toUser(userId, ReduxActions.SERVER_RECEIVE_USER_SCANS, scanItems)
    }

    fun createScanInfo(scan: Scan): ScanningService.ScanInfo {
        val site = siteDataService.getBySiteId(scan.siteId)
        val nodes = scan.nodeScanById.filterValues { it.status != NodeStatus.UNDISCOVERED}.size
        val nodesText = if (scan.duration != null) "${nodes}" else "${nodes}+"
        val userName = userService.getById(scan.initiatorId).name
        val efficiencyStatus = deriveEfficiencyStatus(scan)
        return ScanningService.ScanInfo(scan.runId, site.name, site.siteId, userName, nodesText, efficiencyStatus)
    }

    private fun deriveEfficiencyStatus(scan: Scan): String {
        if (scan.startTime == null) {
            return "(not started)"
        }
        if (scan.efficiency == null) {
            return "(scanning)"
        }
        return "${scan.efficiency}%"
    }

    fun shareScan(runId: String, user: User) {
        if (scanService.hasUserScan(user, runId)) {
            stompService.terminalReceive("[info]${user.name}[/] already has this scan.")
            return
        }
        scanService.createUserScan(runId, user)
        stompService.terminalReceive("Shared scan with [info]${user.name}[/].")

        val myUserName = currentUserService.user.name

        val scan = scanService.getByRunId(runId)
        val siteData = siteDataService.getBySiteId(scan.siteId)

        stompService.toUser(user.id, NotyMessage(NotyType.NEUTRAL, myUserName, "Scan shared for: ${siteData.name}"))
        stompService.terminalReceiveForId(user, "chat", "[warn]${myUserName}[/] shared scan: [info]${siteData.name}[/]")
        sendScanInfosOfPlayer(user.id)
    }

    fun leaveScan(runId: String) {
        hackerActivityService.stopActivityScanning(runId)
        stompService.toRun(runId, ReduxActions.SERVER_HACKER_LEAVE_SCAN, createPresence(currentUserService.user))

    }

    private fun createPresence(user: User): HackerPresence {
        return HackerPresence(user.id, user.name, user.icon)
    }

    fun purgeAll() {
        scanService.purgeAll()
    }

    fun launchProbeAtNode(runId: String, networkId: String) {
        val scan = scanService.getByRunId(runId)
        val node = nodeService.findByNetworkId(scan.siteId, networkId)
        if (node == null) {
            reportNodeNotFound(networkId)
            return
        }

        val traverseNodesById = traverseNodeService.createTraverseNodesWithDistance(scan)
        val targetNode = traverseNodesById[node.id]!!
        val status = scan.nodeScanById[targetNode.id]!!.status
        if (status == NodeStatus.UNDISCOVERED) {
            reportNodeNotFound(networkId)
            return
        }
        val scanType = scanProbeService.determineNodeScanType(status) ?: NodeScanType.SCAN_NODE_DEEP
        val path = scanProbeService.createNodePath(targetNode)
        val userId = currentUserService.userId
        val probeAction = ScanProbeService.ProbeAction(probeUserId = userId, path = path, scanType = scanType, autoScan = false)
        stompService.toRun(scan.runId, ReduxActions.SERVER_PROBE_LAUNCH, probeAction)
    }

    fun autoScan(runId: String) {
        launchProbe(runId, true)
    }

    fun launchProbe(runId: String, autoScan: Boolean) {
        val scan = scanService.getByRunId(runId)
        if (scan.startTime == null) {
            scan.startTime = time.now()
            scanService.save(scan)
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
                stompService.terminalReceive("Scan complete.")
            }
        }
    }

    fun updateScanInfoToPlayers(scan: Scan) {
        val scanInfo = createScanInfo(scan)
        scanService.getUsersOfScan(scan.runId).forEach{ userId ->
            stompService.toUser(userId, ReduxActions.SERVER_UPDATE_SCAN_INFO, scanInfo)
        }
    }

    private fun completeScan(scan: Scan) {
        val now = time.now()
        val durationMillis = ChronoUnit.MILLIS.between(scan.startTime, now)
        val duration = Duration.ofMillis(durationMillis)
        scan.duration = (durationMillis / 1000.0).roundToInt()
        scan.efficiency = calculateEfficiency(scan, durationMillis)
        scanService.save(scan)
        stompService.terminalReceive("Scan completed in ${time.formatDuration(duration)}, with efficiency: ${scan.efficiency}%")
        updateScanInfoToPlayers(scan)
    }

    private fun calculateEfficiency(scan: Scan, durationMillis: Long): Int {
        val nodeCount = scan.nodeScanById.keys.size

        /*
        Time per node zooming in and out
          calculated: 85 * 0.05 s = 4.25 s
          measured: ~4.26 s
         */

        val timeForNodes = 0.1 + (3 * 4.26) * nodeCount // 0.1 startup cost + 4.26 s per node * 3 scans per node * nodeCount
        val timeForPaths = (1.1 * scan.totalDistanceScanned) // 1.1 s per path segment
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
        val scan = scanService.getByRunId(runId)
        val nodes = nodeService.getAll(scan.siteId)
        nodes.forEach { scanProbeService.quickScanNode(it, scan) }
        autoScan(runId)
   }

    fun probeArrive(runId: String, nodeId: String, action: NodeScanType) {
        val updateScanInfo = scanProbeService.probeArrive(runId, nodeId, action)
        if (updateScanInfo) {
            val scan = scanService.getByRunId(runId)
            updateScanInfoToPlayers(scan)
        }
    }

}