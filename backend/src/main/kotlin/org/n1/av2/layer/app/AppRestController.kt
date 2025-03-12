package org.n1.av2.layer.app

import org.n1.av2.layer.app.status_light.StatusLightLayer
import org.n1.av2.layer.ice.common.IceLayer
import org.n1.av2.layer.ice.common.IceService
import org.n1.av2.platform.inputvalidation.ValidLayerId
import org.n1.av2.site.entity.NodeEntityService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController


@RestController
class AppRestController(
    private val nodeEntityService: NodeEntityService,
    private val iceAuthorizationService: org.n1.av2.layer.ice.common.IceAuthorizationService,
    private val iceService: IceService,
) {

    /** See routes.md in the fronted for an explanation on how this works */

    @Suppress("unused")
    class AppResponse(val appId: String?, val redirectLayerId: String?, val error: String?)

    @GetMapping("/api/app/{layerId}")
    fun getAppOrRedirect(@PathVariable @ValidLayerId layerId: String, ): AppResponse {

        val node = nodeEntityService.findByLayerId(layerId)
        val layer = node.getLayerById(layerId)

        val protectingLayerId = iceAuthorizationService.layerProtectingTargetLayer(node, layer)
        if (protectingLayerId != null) {
            return AppResponse(null, protectingLayerId, null)
        }

        return when (layer) {
            is StatusLightLayer -> {
                AppResponse(layer.appId, null, null)
            }
            is IceLayer -> {
                val iceId = iceService.findOrCreateIceForLayerAndIceStatus(layer)
                return AppResponse(iceId, null, null)
            }

            else -> {
                AppResponse(null, null, "Unknown layer type: ${layer.type}")
            }
        }
    }
}
