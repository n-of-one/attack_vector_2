package org.n1.av2.backend.service.service

import org.n1.av2.backend.model.db.run.HackerPosition
import org.n1.av2.backend.model.db.service.Service
import org.n1.av2.backend.model.db.site.Node
import org.n1.av2.backend.service.StompService

@org.springframework.stereotype.Service
class ServiceOs(
        val stompService: StompService
        ) {

    fun hack(service: Service, node: Node, position: HackerPosition) {
        stompService.terminalReceive("[warn]Not implemented for OS. Yet...")
    }

}