package org.n1.av2.backend.service.service

import org.n1.av2.backend.model.db.service.Service
import org.n1.av2.backend.model.db.service.TextService
import org.n1.av2.backend.service.StompService

@org.springframework.stereotype.Service
class ServiceText(
        val stompService: StompService
        ) {

    fun hack(orig: Service) {
        val service = orig as TextService
        stompService.terminalReceive("Hacked: [primary]${service.name}", "", service.text)
    }

}