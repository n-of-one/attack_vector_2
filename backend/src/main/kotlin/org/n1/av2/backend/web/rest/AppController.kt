package org.n1.av2.backend.web.rest

import org.n1.av2.backend.entity.site.NodeEntityService
import org.n1.av2.backend.entity.site.layer.ice.IceLayer
import org.n1.av2.backend.entity.site.layer.other.StatusLightLayer
import org.n1.av2.backend.security.ValidLayerId
import org.n1.av2.backend.service.layerhacking.ice.IceAuthorizationService
import org.n1.av2.backend.service.layerhacking.ice.IceService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/api/app/")
class AppController(
    private val nodeEntityService: NodeEntityService,
    private val iceAuthorizationService: IceAuthorizationService,
    private val iceService: IceService,
) {

    /** See UrlPaths.md for an explanation on how this works */

    class AppResponse(val appId: String?, val redirectLayerId: String?, val error: String?)

    @GetMapping("{layerId}")
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
                val iceId = iceService.findOrCreateIceForLayer(layer)
                return AppResponse(iceId, null, null)
            }

            else -> {
                AppResponse(null, null, "Unknown layer type: ${layer.type}")
            }
        }
    }
}
