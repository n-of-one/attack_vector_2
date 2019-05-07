package org.n1.mainframe.backend.web.rest

import org.n1.mainframe.backend.service.scan.ScanService
import org.n1.mainframe.backend.service.site.SiteService
import org.n1.mainframe.backend.service.user.UserService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class MainController(
        val siteService: SiteService,
        val scanService: ScanService,
        val userService: UserService
) {

    @GetMapping("/health")
    fun state(): String {
        return "ok"
    }

    @RequestMapping("purgeAll/{confirm}")
    fun get(@PathVariable("confirm") confirm: String): String {
        if (confirm != "confirm") return "please confirm."
        siteService.purgeAll()
        scanService.purgeAll()
        userService.purgeAll()
        return "It is done."
    }
}