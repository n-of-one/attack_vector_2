package org.n1.av2.backend.service.layerhacking

import org.n1.av2.backend.entity.run.LayerStatusEntityService
import org.n1.av2.backend.entity.site.enums.LayerType
import org.n1.av2.backend.entity.site.layer.Layer
import org.n1.av2.backend.model.ui.ServerActions
import org.n1.av2.backend.service.StompService

@org.springframework.stereotype.Service
class ServiceIceGeneric(
    private val layerStatusEntityService: LayerStatusEntityService,
    private val stompService: StompService) {

    fun hack(layer: Layer, runId: String) {
        val holder = layerStatusEntityService.getOrCreate(layer.id, runId)
        if (holder.hacked) {
            stompService.replyTerminalReceive("[info]not required[/] Ice already hacked.")
            return
        }

        when (layer.type) {
            LayerType.PASSWORD_ICE -> enterIce(layer, runId)
            LayerType.TANGLE_ICE -> enterIce(layer, runId)
            LayerType.WORD_SEARCH_ICE -> enterIce(layer, runId)
            LayerType.NETWALK_ICE -> enterIce(layer, runId)
            LayerType.SLOW_ICE -> enterIce(layer, runId)
            else -> error("unsupported ice type: ${layer.type}")
        }
    }

    fun enterIce(layer: Layer, runId: String) {
        val layerStatus = layerStatusEntityService.get(layer.id, runId)

        data class EnterIce(val redirectId: String)
        val redirectId = layerStatus.id.substringAfter("-")
        stompService.reply(ServerActions.SERVER_REDIRECT_HACK_ICE, EnterIce(redirectId))
    }

}