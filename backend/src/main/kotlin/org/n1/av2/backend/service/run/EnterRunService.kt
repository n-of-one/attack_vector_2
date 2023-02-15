package org.n1.av2.backend.service.run

import org.n1.av2.backend.entity.run.*
import org.n1.av2.backend.entity.site.SitePropertiesEntityService
import org.n1.av2.backend.model.ui.NotyMessage
import org.n1.av2.backend.model.ui.NotyType
import org.n1.av2.backend.model.ui.ReduxActions
import org.n1.av2.backend.model.ui.SiteFull
import org.n1.av2.backend.service.CurrentUserService
import org.n1.av2.backend.service.StompService
import org.n1.av2.backend.service.patroller.PatrollerUiData
import org.n1.av2.backend.service.patroller.TracingPatrollerService
import org.n1.av2.backend.service.scan.ScanInfoService
import org.n1.av2.backend.service.scan.TraverseNodeService
import org.n1.av2.backend.service.site.SiteService
import org.n1.av2.backend.service.terminal.ScanTerminalService
import org.springframework.stereotype.Service

@Service
class EnterRunService(
    private val runEntityService: RunEntityService,
    private val sitePropertiesEntityService: SitePropertiesEntityService,
    private val siteService: SiteService,
    private val currentUserService: CurrentUserService,
    private val stompService: StompService,
    private val hackerService: HackerService,
    private val traverseNodeService: TraverseNodeService,
    private val layerStatusEntityService: LayerStatusEntityService,
    private val tracingPatrollerService: TracingPatrollerService,
    private val nodeStatusRepo: NodeStatusRepo,
    private val scanTerminalService: ScanTerminalService,
    private val hackerStateEntityService: HackerStateEntityService,
    private val scanInfoService: ScanInfoService,
    ) {

    data class ScanSiteResponse(val runId: String, val siteId: String)

    fun searchSiteByName(siteName: String) {
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
        scanInfoService.sendScanInfosOfPlayer()
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

    fun enterRun(runId: String) {
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
}
