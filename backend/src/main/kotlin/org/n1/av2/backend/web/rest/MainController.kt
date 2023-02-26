package org.n1.av2.backend.web.rest

import org.n1.av2.backend.entity.user.UserEntityService
import org.n1.av2.backend.entity.user.UserRepo
import org.n1.av2.backend.service.AdminService
import org.n1.av2.backend.service.run.StartAttackService
import org.n1.av2.backend.service.scan.ScanInfoService
import org.n1.av2.backend.service.site.SiteService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class MainController(
    private val adminService: AdminService,

    ) {

    @GetMapping("/health")
    fun state(): String {
        return "ok"
    }

    @RequestMapping("/purge")
    fun purgeAll(): String {
        adminService.purgeAll()

        return "Purged!"
    }

    @RequestMapping("reset")
    fun reset(): String {
        adminService.reset()

        return "Reset."
    }

}