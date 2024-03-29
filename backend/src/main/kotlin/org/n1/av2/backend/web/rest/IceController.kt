package org.n1.av2.backend.web.rest

import org.n1.av2.backend.entity.site.NodeEntityService
import org.n1.av2.backend.entity.site.enums.LayerType
import org.n1.av2.backend.entity.site.layer.ice.IceLayer
import org.n1.av2.backend.security.ValidLayerId
import org.n1.av2.backend.service.layerhacking.ice.IceService
import org.n1.av2.backend.service.user.CurrentUserService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/ice/")
class IceController(
    private val nodeEntityService: NodeEntityService,
    private val iceService: IceService,
    private val currentUserService: CurrentUserService
) {


    class IceBasicInfo(val type: LayerType, val iceId: String?, val userId: String)
    @GetMapping("{layerId}")
    fun getIce(@PathVariable @ValidLayerId layerId: String): IceBasicInfo {
        val node = nodeEntityService.findByLayerId(layerId)
        val layer = node.getLayerById(layerId) as IceLayer
        val iceId = iceService.findOrCreateIceForLayer(layer)

        return IceBasicInfo(layer.type, iceId, currentUserService.userId)
    }
}
