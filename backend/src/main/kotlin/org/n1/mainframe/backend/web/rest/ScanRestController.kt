package org.n1.mainframe.backend.web.rest

import org.n1.mainframe.backend.service.ScanService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/scan/")
class ScanRestController(val scanService: ScanService) {

    data class IdData(val id: String)
    data class SiteIdData(val siteId: String)

    @PostMapping("")
    fun root(@RequestBody idData: IdData):SiteIdData {
        val scan = scanService.findById(idData.id)
        return SiteIdData(scan.siteId)
    }

    data class ScanSiteData(val siteName: String)
    @RequestMapping("/site")
    fun scanSiteName(@RequestBody body: ScanSiteData): ScanService.ScanResponse {
        return scanService.scanSite(body.siteName)
    }

}
