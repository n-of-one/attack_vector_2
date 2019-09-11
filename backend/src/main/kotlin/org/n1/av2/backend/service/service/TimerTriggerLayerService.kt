package org.n1.av2.backend.service.service

import org.n1.av2.backend.model.db.layer.TimerTriggerLayer
import org.n1.av2.backend.service.StompService
import org.springframework.stereotype.Service

@Service
class TimerTriggerLayerService(
        val stompService: StompService) {

    fun hack(layer: TimerTriggerLayer) {
        stompService.terminalReceive("${layer.name} resists hack.")
    }

}