package org.n1.mainframe.backend.web.ws

import org.n1.mainframe.backend.engine.SerializingExecutor
import org.n1.mainframe.backend.service.ScanService
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller
import java.security.Principal

@Controller
class ScanController(
        val executor: SerializingExecutor,
        val scanService: ScanService
) {

    @MessageMapping("/scan/sendScan")
    fun siteFull(siteId: String, principal: Principal) {
        executor.run(principal) { scanService.sendScanToUser(siteId, principal) }
    }
}