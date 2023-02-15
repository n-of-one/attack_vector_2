package org.n1.av2.backend.web.rest

import org.n1.av2.backend.entity.user.UserEntityService
import org.n1.av2.backend.service.run.StartAttackService
import org.n1.av2.backend.service.scan.ScanInfoService
import org.n1.av2.backend.service.site.SiteService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class MainController(
    val siteService: SiteService,
    val scanInfoService: ScanInfoService,
    val userEntityService: UserEntityService,
    val startAttackService: StartAttackService
) {

    @GetMapping("/health")
    fun state(): String {
        return "ok"
    }

    @RequestMapping("/purge")
    fun purgeAll(): String {
        siteService.purgeAll()
        scanInfoService.purgeAll()
        userEntityService.purgeAll()
        startAttackService.purgeAll()

        return "Purged!"
    }

    @RequestMapping("reset")
    fun reset(): String {
        startAttackService.reset()

        return "Reset."
    }

}