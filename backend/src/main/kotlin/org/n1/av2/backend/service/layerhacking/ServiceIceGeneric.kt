package org.n1.av2.backend.service.layerhacking

import org.n1.av2.backend.entity.run.LayerStatusEntityService
import org.n1.av2.backend.entity.site.enums.LayerType
import org.n1.av2.backend.entity.site.layer.IceTangleLayer
import org.n1.av2.backend.entity.site.layer.Layer
import org.n1.av2.backend.service.StompService
import org.n1.av2.backend.service.layerhacking.ice.password.IcePasswordService
import org.n1.av2.backend.service.layerhacking.ice.password.IceTangleService

@org.springframework.stereotype.Service
class ServiceIceGeneric(
    private val layerStatusEntityService: LayerStatusEntityService,
    private val icePasswordService: IcePasswordService,
    private val iceTangleService: IceTangleService,
    private val stompService: StompService) {

    fun hack(layer: Layer, runId: String) {
        val holder = layerStatusEntityService.getOrCreate(layer.id, runId)
        if (holder.hacked) {
            stompService.terminalReceiveCurrentUser("[info]not required[/] Ice already hacked.")
            return
        }

        when (layer.type) {
            LayerType.ICE_PASSWORD -> icePasswordService.hack(layer, runId)
            LayerType.ICE_TANGLE -> iceTangleService.hack(layer as IceTangleLayer, runId)
            else -> error("unsupported hack type: ${layer.type}")
        }
    }




}