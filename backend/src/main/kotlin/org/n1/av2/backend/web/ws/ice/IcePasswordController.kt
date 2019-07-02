package org.n1.av2.backend.web.ws.ice

import org.n1.av2.backend.engine.SerializingExecutor
import org.n1.av2.backend.model.db.service.IcePasswordService
import org.springframework.stereotype.Controller

@Controller
class IcePasswordController(
        val icePasswordService: IcePasswordService
        val executor: SerializingExecutor ) {
}