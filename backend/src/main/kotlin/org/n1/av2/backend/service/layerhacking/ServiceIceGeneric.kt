package org.n1.av2.backend.service.layerhacking

import org.n1.av2.backend.entity.run.LayerStatusEntityService
import org.n1.av2.backend.entity.site.enums.LayerType
import org.n1.av2.backend.entity.site.layer.Layer
import org.n1.av2.backend.model.ui.ReduxActions
import org.n1.av2.backend.service.StompService
import org.n1.av2.backend.service.layerhacking.ice.password.IcePasswordService

@org.springframework.stereotype.Service
class ServiceIceGeneric(
    private val layerStatusEntityService: LayerStatusEntityService,
    private val icePasswordService: IcePasswordService,
    private val stompService: StompService) {

    fun hack(layer: Layer, runId: String) {
        val holder = layerStatusEntityService.getOrCreate(layer.id, runId)
        if (holder.hacked) {
            stompService.replyTerminalReceive("[info]not required[/] Ice already hacked.")
            return
        }

        when (layer.type) {
            LayerType.ICE_PASSWORD -> icePasswordService.hack(layer, runId)
            LayerType.ICE_TANGLE -> enterIce(layer, runId)
            else -> error("unsupported hack type: ${layer.type}")
        }
    }

    fun enterIce(layer: Layer, runId: String) {
        val layerStatus = layerStatusEntityService.get(layer.id, runId)
        val iceId = layerStatus.iceId ?: error("No ice id for layer: ${layer.id} in run: ${runId}")

        data class EnterIce(val redirectId: String)
        val redirectId = iceId.substringAfter("-")
        stompService.reply(ReduxActions.SERVER_REDIRECT_HACK_ICE, EnterIce(redirectId))
    }

}