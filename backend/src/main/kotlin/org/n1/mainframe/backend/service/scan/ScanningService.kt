package org.n1.mainframe.backend.service.scan

import org.springframework.stereotype.Service
import java.security.Principal

/** This service deals with the action of scanning (as opposed to the actions performed on a scan). */
@Service
class ScanningService(val scanService: ScanService) {

    fun processCommand(scanId: String, command: String, principal: Principal) {

    }
}