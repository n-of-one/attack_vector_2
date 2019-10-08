package org.n1.av2.backend.web.ws

import mu.KLogging
import org.n1.av2.backend.engine.SerializingExecutor
import org.n1.av2.backend.service.run.HackingService
import org.springframework.stereotype.Controller

@Controller
class HackingController(
        val executor: SerializingExecutor,
        val hackingService: HackingService
) {

    companion object: KLogging()




}