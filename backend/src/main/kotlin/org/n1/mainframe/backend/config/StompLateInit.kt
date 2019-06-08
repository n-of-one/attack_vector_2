package org.n1.mainframe.backend.config

import mu.KLogging
import org.n1.mainframe.backend.service.StompService
import org.n1.mainframe.backend.service.user.HackerActivityService
import org.springframework.context.annotation.Configuration

@Configuration
class StompLateInit(hackerActivityService: HackerActivityService, stompService: StompService) {

    companion object : KLogging()

    init {
        hackerActivityService.stompService = stompService
        logger.info("Configured stompService into hackerActivityService")
    }

}