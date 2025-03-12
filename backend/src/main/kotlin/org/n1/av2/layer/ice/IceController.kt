package org.n1.av2.layer.ice

import org.n1.av2.layer.ice.common.IceLayer
import org.n1.av2.layer.ice.common.IceService
import org.n1.av2.platform.iam.user.CurrentUserService
import org.n1.av2.platform.inputvalidation.ValidLayerId
import org.n1.av2.site.entity.NodeEntityService
import org.n1.av2.site.entity.enums.LayerType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class IceController(
    private val nodeEntityService: NodeEntityService,
    private val iceService: IceService,
    private val currentUserService: CurrentUserService
) {


    class IceBasicInfo(val type: LayerType, val iceId: String?, val userId: String)
    @GetMapping("/api/ice/{layerId}")
    fun getIce(@PathVariable @ValidLayerId layerId: String): IceBasicInfo {
        val node = nodeEntityService.findByLayerId(layerId)
        val layer = node.getLayerById(layerId) as IceLayer
        val iceId = iceService.findOrCreateIceForLayerAndIceStatus(layer)

        return IceBasicInfo(layer.type, iceId, currentUserService.userId)
    }
}
