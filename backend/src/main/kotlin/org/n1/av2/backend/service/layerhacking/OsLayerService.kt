package org.n1.av2.backend.service.layerhacking

import org.n1.av2.backend.entity.site.layer.Layer
import org.n1.av2.backend.service.StompService

@org.springframework.stereotype.Service
class OsLayerService(
    private val stompService: StompService) {


    fun hack(layer: Layer) {
        stompService.terminalReceiveCurrentUser("Hacking ${layer.name} reveals nothing new.")
    }


}