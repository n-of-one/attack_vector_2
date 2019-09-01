package org.n1.av2.backend.service.service

import org.n1.av2.backend.model.db.service.Service
import org.n1.av2.backend.model.db.service.TextService
import org.n1.av2.backend.model.db.site.Node
import org.n1.av2.backend.service.StompService
import org.n1.av2.backend.service.site.NodeService

@org.springframework.stereotype.Service
class ServiceText(
        val stompService: StompService,
        val nodeService: NodeService,
        val hackedUtil: HackedUtil
) {

    fun hack(orig: Service, node: Node, runId: String) {
        val service = orig as TextService

        nodeService.save(node)
        hackedUtil.nonIceHacked(service.id, node, runId)

        stompService.terminalReceive("Hacked: [pri]${service.layer}[/] ${service.name}", "", service.text)
    }

}