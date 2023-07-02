package org.n1.av2.backend.web.rest

import org.n1.av2.backend.entity.site.NodeEntityService
import org.n1.av2.backend.entity.site.enums.LayerType
import org.n1.av2.backend.entity.site.layer.Layer
import org.n1.av2.backend.entity.site.layer.ice.*
import org.n1.av2.backend.service.layerhacking.ice.IceService
import org.n1.av2.backend.service.layerhacking.ice.netwalk.NetwalkIceService
import org.n1.av2.backend.service.layerhacking.ice.password.PasswordIceService
import org.n1.av2.backend.service.layerhacking.ice.slow.SlowIceService
import org.n1.av2.backend.service.layerhacking.ice.tangle.TangleService
import org.n1.av2.backend.service.layerhacking.ice.wordsearch.WordSearchService
import org.n1.av2.backend.service.user.CurrentUserService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/ice/")
class IceController(
    private val nodeEntityService: NodeEntityService,
    private val iceService: IceService,
    private val currentUserService: CurrentUserService
) {

    private val logger = mu.KotlinLogging.logger {}

    class IceBasicInfo(val type: LayerType, val iceId: String?, val userId: String)
    @GetMapping("{layerId}")
    fun getIce(@PathVariable layerId: String): IceBasicInfo {
        val node = nodeEntityService.findByLayerId(layerId)
        val layer = node.getLayerById(layerId)
        val iceId = iceService.findIceIdForLayer(layer)

        if (iceId == null) logger.error { "IceId not found for layerId=$layerId, ice type: $layer.type" }

        return IceBasicInfo(layer.type, iceId, currentUserService.userId)
    }


}
