package org.n1.mainframe.backend.service

import org.n1.mainframe.backend.model.scan.NodeStatus
import org.n1.mainframe.backend.model.scan.Scan
import org.n1.mainframe.backend.model.ui.NotyMessage
import org.n1.mainframe.backend.model.ui.site.SiteFull
import org.n1.mainframe.backend.repo.ScanRepo
import org.n1.mainframe.backend.service.site.LayoutService
import org.n1.mainframe.backend.service.site.NodeService
import org.n1.mainframe.backend.service.site.SiteDataService
import org.n1.mainframe.backend.service.site.SiteService
import org.n1.mainframe.backend.util.createId
import org.springframework.stereotype.Service
import java.security.Principal

@Service
class ScanService(val scanRepo: ScanRepo,
                  val layoutService: LayoutService,
                  val siteDataService: SiteDataService,
                  val siteService: SiteService,
                  val stompService: StompService,
                  val nodeService: NodeService) {

    data class ScanResponse(val scanId: String?, val message: NotyMessage?)

    fun scanSite(siteName: String): ScanResponse {
        val siteData = siteDataService.findByName(siteName) ?: return ScanResponse(null, NotyMessage("error", "Error", "Site '${siteName}' not found"))
        val layout = layoutService.findById(siteData.id)
        if (layout.nodeIds.isEmpty()) {
            throw java.lang.IllegalStateException("Site does not have nodes.")
        }
        val nodes = nodeService.getAll(layout.nodeIds)

        val startNode = siteService.findStartNode(siteData.startNodeNetworkId, nodes) ?:
                throw java.lang.IllegalStateException("Start node invalid. NetworkID: ${siteData.startNodeNetworkId} for ${siteName}")
        val nodeStatusById = layout.nodeIds.map { it to NodeStatus.UNDISCOVERED }.toMap().toMutableMap()
        nodeStatusById[startNode.id] = NodeStatus.DISCOVERED

        val id = createId("scan") { candidate: String -> scanRepo.findById(candidate) }
        val scan = Scan(
                id = id,
                siteId = siteData.id,
                complete = false,
                nodeStatusById = nodeStatusById
        )
        scanRepo.save(scan)

        return ScanResponse(id, null)
    }

    data class ScanAndSite(val scan: Scan, val site: SiteFull)
    fun sendScanToUser(scanId: String, principal: Principal) {
        val scan = scanRepo.findById(scanId).get()
        val siteFull = siteService.getSiteFull(scan.siteId)

//        siteFull.nodes.forEach { node -> scan.nodeStatusById[node.id] = NodeStatus.CONNECTIONS }

        val scanAndSite = ScanAndSite(scan, siteFull)
        stompService.toUser(principal, ReduxActions.SERVER_SCAN_FULL, scanAndSite)

    }

    fun findById(id: String): Scan {
        return scanRepo.findById(id).orElseThrow { IllegalStateException("No scan found with ID ${id}") }
    }
}