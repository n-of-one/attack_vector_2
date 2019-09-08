package org.n1.av2.backend.service.service

import org.n1.av2.backend.model.db.layer.Layer
import org.n1.av2.backend.model.db.site.enums.LayerType
import org.n1.av2.backend.service.StompService
import org.n1.av2.backend.service.run.LayerStatusService
import org.n1.av2.backend.service.service.ice.password.IcePasswordService
import org.n1.av2.backend.service.service.ice.password.IceTangleService

@org.springframework.stereotype.Service
class ServiceIceGeneric(
        private val layerStatusService: LayerStatusService,
        private val icePasswordService: IcePasswordService,
        private val iceTangleService: IceTangleService,
        private val stompService: StompService) {

    fun hack(service: Layer, runId: String) {
        val holder = layerStatusService.getOrCreate(service.id, runId)
        if (holder.hacked) {
            stompService.terminalReceive("[info]not required[/] Ice already hacked.")
            return
        }

        when (service.type) {
            LayerType.ICE_PASSWORD -> icePasswordService.hack(service, runId)
            LayerType.ICE_TANGLE -> iceTangleService.hack(service, runId)
            else -> error("unsupported hack type: ${service.type}")
        }
    }




}