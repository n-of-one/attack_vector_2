package org.n1.mainframe.backend.service

import org.n1.mainframe.backend.model.scan.NodeStatus
import org.n1.mainframe.backend.model.scan.Scan
import org.n1.mainframe.backend.model.ui.NotyMessage
import org.n1.mainframe.backend.model.ui.site.SiteFull
import org.n1.mainframe.backend.repo.ScanRepo
import org.n1.mainframe.backend.service.site.LayoutService
import org.n1.mainframe.backend.service.site.SiteDataService
import org.n1.mainframe.backend.service.site.SiteService
import org.n1.mainframe.backend.util.createId
import org.springframework.stereotype.Service
import java.security.Principal
import java.util.*

@Service
class ScanService(val scanRepo: ScanRepo,
                  val layoutService: LayoutService,
                  val siteDataService: SiteDataService,
                  val siteService: SiteService,
                  val stompService: StompService) {

    data class ScanResponse(val scanId: String?, val message: NotyMessage?)

    fun scanSite(siteName: String): ScanResponse {
        val siteData = siteDataService.findByName(siteName) ?: return ScanResponse(null, NotyMessage("error", "Error", "Site '${siteName}' not found"))
        val layout = layoutService.findById(siteData.id)


        val id = createId("scan") { candidate: String -> scanRepo.findById(candidate) }

        val nodeStatusById = layout.nodeIds.map { it to NodeStatus.UNDISCOVERED }.toMap().toMutableMap()
        nodeStatusById[siteData.startNodeId] = NodeStatus.DISCOVERED
        val scan = Scan(
                id = id,
                siteId = siteData.id,
                complete = false,
                nodeStatusById = nodeStatusById
        )
        scanRepo.save(scan)

        return ScanResponse(id, null)
    }

    val rand = Random()

    fun randomStatus(): NodeStatus {
        val s = rand.nextInt(5)
        when(s) {
            0 -> return NodeStatus.UNDISCOVERED
            1 -> return NodeStatus.DISCOVERED
            2 -> return NodeStatus.TYPE
            3 -> return NodeStatus.CONNECTIONS
            4 -> return NodeStatus.SERVICES
        }
        return NodeStatus.UNDISCOVERED
    }

    data class ScanAndSite(val scan: Scan, val site: SiteFull)
    fun sendScanToUser(scanId: String, principal: Principal) {
        val scan = scanRepo.findById(scanId).get()
        val siteFull = siteService.getSiteFull(scan.siteId)
        siteFull.nodes.forEach { node -> scan.nodeStatusById[node.id] = randomStatus() }

        val scanAndSite = ScanAndSite(scan, siteFull)
        stompService.toUser(principal, ReduxActions.SERVER_SCAN_FULL, scanAndSite)

    }

    fun findById(id: String): Scan {
        return scanRepo.findById(id).orElseThrow { IllegalStateException("No scan found with ID ${id}") }
    }
}