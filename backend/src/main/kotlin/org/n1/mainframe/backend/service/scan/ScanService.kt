package org.n1.mainframe.backend.service.scan

import org.n1.mainframe.backend.model.scan.NodeScan
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

    fun createScan(siteData: SiteData, nodeScanById: MutableMap<String, NodeScan>): String {
        val id = createId("scan") { candidate: String -> scanRepo.findById(candidate) }
        val scan = Scan(
                id = id,
                siteId = siteData.id,
                complete = false,
                nodeScanById = nodeScanById
        )
        scanRepo.save(scan)
        return id
    }

    fun save(scan: Scan) {
        scanRepo.save(scan)
    }

    fun getAll(): List<Scan> {
        return scanRepo.findAll().toList()
    }

    fun purgeAll() {
        scanRepo.deleteAll()
    }
}

