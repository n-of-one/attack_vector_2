package org.n1.av2.backend.service.layer

import org.n1.av2.backend.model.db.layer.IceTangleLayer
import org.n1.av2.backend.model.db.layer.Layer
import org.n1.av2.backend.model.db.site.enums.LayerType
import org.n1.av2.backend.service.StompService
import org.n1.av2.backend.service.layer.ice.password.IcePasswordService
import org.n1.av2.backend.service.layer.ice.password.IceTangleService
import org.n1.av2.backend.service.run.LayerStatusService

@org.springframework.stereotype.Service
class ServiceIceGeneric(
        private val layerStatusService: LayerStatusService,
        private val icePasswordService: IcePasswordService,
        private val iceTangleService: IceTangleService,
        private val stompService: StompService) {

    fun hack(layer: Layer, runId: String) {
        val holder = layerStatusService.getOrCreate(layer.id, runId)
        if (holder.hacked) {
            stompService.terminalReceive("[info]not required[/] Ice already hacked.")
            return
        }

        when (layer.type) {
            LayerType.ICE_PASSWORD -> icePasswordService.hack(layer, runId)
            LayerType.ICE_TANGLE -> iceTangleService.hack(layer as IceTangleLayer, runId)
            else -> error("unsupported hack type: ${layer.type}")
        }
    }




}