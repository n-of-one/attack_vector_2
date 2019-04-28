package org.n1.mainframe.backend.service.scan

import org.n1.mainframe.backend.model.scan.NodeStatus
import org.n1.mainframe.backend.model.scan.Scan
import org.n1.mainframe.backend.model.site.SiteData
import org.n1.mainframe.backend.repo.ScanRepo
import org.n1.mainframe.backend.util.createId
import org.springframework.stereotype.Service

@Service
class ScanService(val scanRepo: ScanRepo) {
    fun getById(scanId: String): Scan {
        return scanRepo.findById(scanId).orElseGet { throw IllegalArgumentException("${scanId} not found") }
    }

    fun createScan(siteData: SiteData, nodeStatusById: MutableMap<String, NodeStatus>): String {
        val id = createId("scan") { candidate: String -> scanRepo.findById(candidate) }
        val scan = Scan(
                id = id,
                siteId = siteData.id,
                complete = false,
                nodeStatusById = nodeStatusById
        )
        scanRepo.save(scan)
        return id
    }
}

