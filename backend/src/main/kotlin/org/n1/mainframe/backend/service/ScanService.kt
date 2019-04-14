package org.n1.mainframe.backend.service

import org.n1.mainframe.backend.model.scan.NodeStatus
import org.n1.mainframe.backend.model.scan.Scan
import org.n1.mainframe.backend.model.ui.NotyMessage
import org.n1.mainframe.backend.repo.ScanRepo
import org.n1.mainframe.backend.service.site.SiteService
import org.n1.mainframe.backend.util.createId
import org.springframework.stereotype.Service

@Service
class ScanService(val scanRepo: ScanRepo,
                  val siteService: SiteService) {

    data class ScanResponse(val scanId: String?, val message: NotyMessage?)

    fun scanSite(siteName: String): ScanResponse {
        val site = siteService.findByName(siteName) ?: return ScanResponse(null, NotyMessage("error", "Error", "Site '${siteName}' not found"))


        val id = createId("scan") { candidate: String -> scanRepo.findById(candidate) }

        val nodeStatusById = site.nodeIds.map { it to NodeStatus.UNDISCOVERED }.toMap().toMutableMap()
        nodeStatusById[site.startNodeId!!] = NodeStatus.DISCOVERED
        val scan = Scan(
                id = id,
                siteId = site.id,
                complete = false,
                nodeStatusById = nodeStatusById
        )
        scanRepo.save(scan)

        return ScanResponse(id, null)
    }
}