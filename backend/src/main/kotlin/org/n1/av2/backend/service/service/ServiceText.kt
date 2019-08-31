package org.n1.av2.backend.service.service

import org.n1.av2.backend.model.db.service.Service
import org.n1.av2.backend.model.db.service.TextService
import org.n1.av2.backend.model.db.site.Node
import org.n1.av2.backend.service.StompService
import org.n1.av2.backend.service.site.NodeService

@org.springframework.stereotype.Service
class ServiceText(
        val stompService: StompService,
        val nodeService: NodeService
) {

    fun hack(orig: Service, node: Node) {
        val service = orig as TextService
//        stompService.terminalReceive("Hacked: [primary]${service.name}", "", service.text)

        // FIXME: service.hacked = true
        nodeService.save(node)

        stompService.terminalReceive("Hacked: [pri]${service.layer}[/] ${service.name}", "", service.text)
    }

}