package org.n1.av2.backend.service.layerhacking.service

import org.n1.av2.backend.entity.site.layer.Layer
import org.n1.av2.backend.service.util.StompService

@org.springframework.stereotype.Service
class OsLayerService(
    private val stompService: StompService
) {


    fun hack(layer: Layer) {
        stompService.replyTerminalReceive("Hacking ${layer.name} reveals nothing new.")
    }

    fun connect(layer: Layer) {
        stompService.replyTerminalReceive("Connecting to ${layer.name} reveals nothing new.")
    }

}