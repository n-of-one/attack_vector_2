package org.n1.av2.backend.service.service

import org.n1.av2.backend.model.db.site.Node
import org.n1.av2.backend.model.ui.ReduxActions
import org.n1.av2.backend.service.StompService

data class IceHackedUpdate(val serviceId: String, val nodeId: String)

@org.springframework.stereotype.Service
class ServiceIceUtil(
        val stompService: StompService
) {

    fun iceHacked(serviceId: String, node: Node, runId: String) {
        val update = IceHackedUpdate(serviceId, node.id)
        stompService.toRun(runId, ReduxActions.SERVER_ICE_HACKED, update)

    }
}