package org.n1.av2.backend.service.layerhacking.ice

import org.n1.av2.backend.entity.ice.IceStatus
import org.n1.av2.backend.entity.site.Node
import org.n1.av2.backend.entity.site.NodeEntityService
import org.n1.av2.backend.entity.site.layer.Layer
import org.n1.av2.backend.entity.site.layer.ice.*
import org.n1.av2.backend.service.layerhacking.app.auth.AuthAppService
import org.n1.av2.backend.service.layerhacking.ice.netwalk.NetwalkIceService
import org.n1.av2.backend.service.layerhacking.ice.tangle.TangleService
import org.n1.av2.backend.service.layerhacking.ice.tar.TarService
import org.n1.av2.backend.service.layerhacking.ice.wordsearch.WordSearchService
import org.n1.av2.backend.service.user.CurrentUserService
import org.springframework.stereotype.Service

@Service
class IceService(
    private val nodeEntityService: NodeEntityService,
    private val tangleService: TangleService,
    private val authAppService: AuthAppService,
    private val wordSearchService: WordSearchService,
    private val netwalkIceService: NetwalkIceService,
    private val tarService: TarService,
    private val currentUserService: CurrentUserService,
) {

    fun findOrCreateIceForLayer(layer: IceLayer): String {
        val iceId = createIceForLayerInternal(layer)
        authAppService.findOrCreateIceStatus(iceId, layer)
        return iceId
    }

    private fun createIceForLayerInternal(layer: IceLayer): String {
        return when (layer) {
            is TangleIceLayer -> tangleService.findOrCreateIceByLayerId(layer).id
            is WordSearchIceLayer -> wordSearchService.findOrCreateIceByLayerId(layer).id
            is NetwalkIceLayer -> netwalkIceService.findOrCreateIceByLayerId(layer).id
            is TarIceLayer -> tarService.findOrCreateIceByLayerId(layer).id
            is PasswordIceLayer -> return authAppService.findOrCreateIceStatus(layer).id
        }
    }

    fun deleteIce(siteId: String) {

        val allLayers = nodeEntityService.getAll(siteId).map { node -> node.layers }.flatten()

        allLayers.filterIsInstance<TangleIceLayer>().forEach { tangleService.deleteByLayerId(it.id) }
        allLayers.filterIsInstance<PasswordIceLayer>().forEach { authAppService.deleteByLayerId(it.id) }
        allLayers.filterIsInstance<WordSearchIceLayer>().forEach { wordSearchService.deleteByLayerId(it.id) }
        allLayers.filterIsInstance<NetwalkIceLayer>().forEach { netwalkIceService.deleteByLayerId(it.id) }
        allLayers.filterIsInstance<TarIceLayer>().forEach { tarService.deleteByLayerId(it.id) }
    }

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
        val iceId = findOrCreateIceForLayer(layer)
        val iceStatus = authAppService.findOrCreateIceStatus(iceId, layer)
        val isHacker = currentUserService.userEntity.type.hacker
        if (isHacker && layer.hacked) {
            return true
        }
        return authorizedAsUser(iceStatus)
    }

    private fun authorizedAsUser(iceStatus: IceStatus): Boolean {
        return iceStatus.authorized.contains(currentUserService.userId)
    }


}