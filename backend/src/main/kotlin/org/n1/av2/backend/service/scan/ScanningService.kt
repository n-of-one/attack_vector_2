package org.n1.av2.backend.service.scan

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
import org.n1.av2.backend.service.user.HackerActivityService
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.temporal.ChronoUnit

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
                      val time: TimeService
) {


    fun deleteScan(runId: String) {
        scanService.deleteUserScan(runId)
        sendScansOfPlayer()
    }

    data class ScanSiteResponse(val runId: String, val siteId: String)

    fun scanSiteForName(siteName: String) {
        val siteData = siteDataService.findByName(siteName)
        if (siteData == null) {
            stompService.toUser(NotyMessage(NotyType.NEUTRAL, "Error", "Site '${siteName}' not found"))
            return
        }

        val nodeScans = createNodeScans(siteData.id)

        val user = currentUserService.user

        val runId = scanService.createScan(siteData, nodeScans, user)
        val response = ScanSiteResponse(runId, siteData.id)
        stompService.toUser(ReduxActions.SERVER_SITE_DISCOVERED, response)
        sendScansOfPlayer()
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
        stompService.toRun(runId, ReduxActions.SERVER_HACKER_ENTER_SCAN, createPresence(currentUserService.user))

        val scan = scanService.getById(runId)
        val siteFull = siteService.getSiteFull(scan.siteId)
        val userPresence = hackerActivityService
                .getAll(HackerActivityType.SCANNING, scan.id)
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


    fun sendScansOfPlayer() {
        val userId = currentUserService.userId
        sendScansOfPlayer(userId)
    }

    data class ScanOverViewLine(val runId: String, val siteName: String, val complete: Boolean, val siteId: String)

    fun sendScansOfPlayer(userId: String) {
        val scans = scanService.getAll(userId)
        val scanItems = scans.map { scan ->
            val site = siteDataService.getById(scan.siteId)
            val complete = (scan.nodeScanById.values.find { it.status != NodeStatus.SERVICES } == null)
            ScanOverViewLine(scan.id, site.name, complete, site.id)
        }
        stompService.toUser(userId, ReduxActions.SERVER_RECEIVE_USER_SCANS, scanItems)
    }

    fun shareScan(runId: String, user: User) {
        if (scanService.hasUserScan(user, runId)) {
            stompService.terminalReceive("[info]${user.name}[/] already has this scan.")
            return
        }
        scanService.createUserScan(runId, user)
        stompService.terminalReceive("Shared scan with [info]${user.name}[/].")

        val myUserName = currentUserService.user.name

        val scan = scanService.getById(runId)
        val siteData = siteDataService.getById(scan.siteId)

        stompService.toUser(user.id, NotyMessage(NotyType.NEUTRAL, myUserName, "Scan shared for: ${siteData.name}"))
        stompService.terminalReceiveForId(user, "chat", "[warn]${myUserName}[/] shared scan: [info]${siteData.name}[/]")
        sendScansOfPlayer(user.id)
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
        val scan = scanService.getById(runId)
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
        stompService.toRun(scan.id, ReduxActions.SERVER_PROBE_LAUNCH, probeAction)
    }

    fun autoScan(runId: String) {
        launchProbe(runId, true)
    }

    fun launchProbe(runId: String, autoScan: Boolean) {
        val scan = scanService.getById(runId)
        val probeAction = scanProbeService.createProbeAction(scan, autoScan)
        if (probeAction != null) {
            stompService.toRun(scan.id, ReduxActions.SERVER_PROBE_LAUNCH, probeAction)
        } else {
            if (scan.duration == null) {
                val durationSeconds = ChronoUnit.SECONDS.between(scan.startTime, time.now())
                val duration = Duration.ofSeconds(durationSeconds)
                scan.duration = duration
                scanService.save(scan)
                stompService.terminalReceive("Scan completed in ${duration.toHoursPart()}:${duration.toMinutesPart()}:${duration.toSecondsPart()}, total distance: ${scan.totalDistanceScanned}")
            }
            else {
                stompService.terminalReceive("Scan complete.")
            }
            sendScansOfPlayer()
        }
    }

    fun quickScan(runId: String) {
        val scan = scanService.getById(runId)
        val nodes = nodeService.getAll(scan.siteId)
        nodes.forEach { scanProbeService.quickScanNode(it, scan) }
        autoScan(runId)
    }

    fun probeArrive(runId: String, nodeId: String, action: NodeScanType) {
        scanProbeService.probeArrive(runId, nodeId, action)
    }

}