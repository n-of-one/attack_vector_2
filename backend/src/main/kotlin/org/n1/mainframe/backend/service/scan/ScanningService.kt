package org.n1.mainframe.backend.service.scan

import org.n1.mainframe.backend.model.scan.NodeStatus
import org.n1.mainframe.backend.model.scan.Scan
import org.n1.mainframe.backend.model.ui.NotyMessage
import org.n1.mainframe.backend.model.ui.site.SiteFull
import org.n1.mainframe.backend.service.ReduxActions
import org.n1.mainframe.backend.service.StompService
import org.n1.mainframe.backend.service.site.NodeService
import org.n1.mainframe.backend.service.site.SiteDataService
import org.n1.mainframe.backend.service.site.SiteService
import org.springframework.stereotype.Service
import java.security.Principal

/** This service deals with the action of scanning (as opposed to the actions performed on a scan). */
@Service
class ScanningService(val scanService: ScanService,
        val siteDataService: SiteDataService,
                      val siteService: SiteService,
                      val stompService: StompService,
                      val nodeService: NodeService) {

    data class ScanResponse(val scanId: String?, val message: NotyMessage?)

    fun scanSite(siteName: String): ScanResponse {
        val siteData = siteDataService.findByName(siteName) ?: return ScanResponse(null, NotyMessage("error", "Error", "Site '${siteName}' not found"))
        val nodes = nodeService.getAll(siteData.id)
        if (nodes.isEmpty()) {
            throw java.lang.IllegalStateException("Site does not have nodes.")
        }

        val startNode = siteService.findStartNode(siteData.startNodeNetworkId, nodes) ?:
                throw java.lang.IllegalStateException("Start node invalid. NetworkID: ${siteData.startNodeNetworkId} for ${siteName}")
        val nodeStatusById = nodes.map { it.id to NodeStatus.UNDISCOVERED }.toMap().toMutableMap()
        nodeStatusById[startNode.id] = NodeStatus.DISCOVERED

        val id = scanService.createScan(siteData, nodeStatusById)

        return ScanResponse(id, null)
    }


    data class ScanAndSite(val scan: Scan, val site: SiteFull)
    fun sendScanToUser(scanId: String, principal: Principal) {
        val scan = scanService.getById(scanId)
        val siteFull = siteService.getSiteFull(scan.siteId)

//        siteFull.nodes.forEach { node -> scan.nodeStatusById[node.id] = NodeStatus.CONNECTIONS }

        val scanAndSite = ScanAndSite(scan, siteFull)
        stompService.toUser(principal, ReduxActions.SERVER_SCAN_FULL, scanAndSite)

    }

    fun processCommand(scanId: String, command: String, principal: Principal) {

    }
}