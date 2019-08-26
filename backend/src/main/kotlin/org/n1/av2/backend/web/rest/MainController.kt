package org.n1.av2.backend.web.rest

import org.n1.av2.backend.service.run.HackingService
import org.n1.av2.backend.service.scan.ScanningService
import org.n1.av2.backend.service.site.SiteService
import org.n1.av2.backend.service.user.UserService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class MainController(
        val siteService: SiteService,
        val scanningService: ScanningService,
        val userService: UserService,
        val hackingService: HackingService
) {

    @GetMapping("/health")
    fun state(): String {
        return "ok"
    }

    @RequestMapping("purgeAll")
    fun purgeAll(): String {
        siteService.purgeAll()
        scanningService.purgeAll()
        userService.purgeAll()
        hackingService.purgeAll()

        return "Purged!"
    }

    @RequestMapping("reset")
    fun reset(): String {
        hackingService.reset()

        return "Reset."
    }

}