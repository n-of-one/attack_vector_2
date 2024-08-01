package org.n1.av2.layer.app

import org.n1.av2.layer.Layer
import org.n1.av2.layer.app.status_light.StatusLightLayer
import org.n1.av2.layer.ice.common.IceLayer
import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.platform.connection.ServerActions
import org.n1.av2.site.entity.NodeEntityService
import org.springframework.stereotype.Service

@Service
class AppService(
    private val nodeEntityService: NodeEntityService,
    private val connectionService: ConnectionService,
    private val iceAuthorizationService: org.n1.av2.layer.ice.common.IceAuthorizationService,
) {

    fun gotoNextLayerAfterExternalHack(layerId: String) {
        val layer = nodeEntityService.findLayer(layerId)
        val path = determineNextLayerPath(layer)

        class NextLayer(val path: String?)
        connectionService.reply(ServerActions.SERVER_REDIRECT_NEXT_LAYER, NextLayer(path))
    }

    fun determineNextLayerPath(currentIceLayer: Layer): String? {
        val layer = nextLayerToAccess(currentIceLayer) ?: return null
        return layerToPath(layer)
    }


    private fun nextLayerToAccess(currentIceLayer: Layer): Layer? {
        val node = nodeEntityService.findByLayerId(currentIceLayer.id)
        val layersBelowIce = node.layers.filter { it.level < currentIceLayer.level }
        if (layersBelowIce.size <= 1) return null // only OS left
        layersBelowIce
            .sortedBy { it.level }
            .reversed()
            .forEach { layer ->
                if (layer is IceLayer && !iceAuthorizationService.isAuthorized(layer)) {
                    return layer
                }
                if (layer is StatusLightLayer) {
                    return layer
                }
            }
        return null
    }

    private fun layerToPath(layer: Layer): String {
        if (layer is IceLayer) return "app/auth/${layer.id}"
        if (layer is StatusLightLayer) return "app/switch/${layer.id}"
        error("Don't know how to proceed to ${layer.id}: of type ${layer.type}")
    }
}
