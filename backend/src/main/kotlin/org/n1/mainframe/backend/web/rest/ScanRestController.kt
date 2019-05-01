package org.n1.mainframe.backend.web.rest

import org.n1.mainframe.backend.service.scan.ScanService
import org.n1.mainframe.backend.service.scan.ScanningService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.security.Principal

@RestController
@RequestMapping("/api/scan/")
class ScanRestController(val scanningService: ScanningService,
                         val scanService: ScanService) {

    data class ScanIdData(val id: String)
    data class SiteIdData(val siteId: String)

    @PostMapping("")
    fun getSiteId(@RequestBody idData: ScanIdData):SiteIdData {
        val scan = scanService.getById(idData.id)
        return SiteIdData(scan.siteId)
    }

    data class ScanSiteData(val siteName: String)
    @RequestMapping("/site")
    fun scanSiteName(@RequestBody body: ScanSiteData): ScanningService.ScanResponse {
        return scanningService.scanSite(body.siteName)
    }

    @RequestMapping("/scansOfPlayer")
    fun scansOfPlayer(principal: Principal): Collection<ScanningService.ScanOverViewLine> {
        return scanningService.scansOfPlayer(principal)
    }

}
