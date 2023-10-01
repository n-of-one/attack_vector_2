package org.n1.av2.backend.web.rest

import org.n1.av2.backend.entity.site.NodeEntityService
import org.n1.av2.backend.entity.site.layer.other.StatusLightLayer
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/api/widget/")
class WidgetController(
    private val nodeEntityService: NodeEntityService
) {

    /** See UrlPaths.md for an explanation on how this works */

    class WidgetResponse(val appId: String)

    @GetMapping("{layerId}")
    fun getWidget(@PathVariable layerId: String): WidgetResponse {

        val node = nodeEntityService.findByLayerId(layerId)
        val layer = node.getLayerById(layerId)

        return when (layer) {
            is StatusLightLayer -> {
                WidgetResponse(layer.appId)
            }

            else -> {
                error("Unknown layer type: ${layer.type}")
            }
        }
    }
}
