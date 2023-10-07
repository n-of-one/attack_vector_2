package org.n1.av2.backend.service.layerhacking.ice

import org.n1.av2.backend.entity.ice.IcePasswordStatus
import org.n1.av2.backend.entity.site.Node
import org.n1.av2.backend.entity.site.layer.Layer
import org.n1.av2.backend.entity.site.layer.ice.IceLayer
import org.n1.av2.backend.service.layerhacking.app.auth.AuthAppService
import org.n1.av2.backend.service.user.CurrentUserService
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service

@Service
class IceAuthorizationService(
    @Lazy private val authAppService: AuthAppService,
    @Lazy private val iceService: IceService,
    private val currentUserService: CurrentUserService,
    ) {

    fun layerProtectingTargetLayer(node: Node, targetLayer: Layer): String? {
        val targetLevel = targetLayer.level

        val iceLayersAboveTarget = node.layers
            .filter { layer -> layer.level > targetLevel }
            .filterIsInstance<IceLayer>()

        iceLayersAboveTarget
            .sortedBy { it.level }
            .reversed()
            .forEach { layer ->
                if (!isAuthorized(layer)) {
                    return layer.id
                }
            }
        return null
    }

    fun isAuthorized(layer: IceLayer): Boolean {
        val iceId = iceService.findOrCreateIceForLayer(layer)
        val iceStatus = authAppService.findOrCreateIceStatus(iceId, layer)
        val isHacker = currentUserService.userEntity.type.hacker
        if (isHacker && layer.hacked) {
            return true
        }
        return authorizedAsUser(iceStatus)
    }

    private fun authorizedAsUser(icePasswordStatus: IcePasswordStatus): Boolean {
        return icePasswordStatus.authorized.contains(currentUserService.userId)
    }
}