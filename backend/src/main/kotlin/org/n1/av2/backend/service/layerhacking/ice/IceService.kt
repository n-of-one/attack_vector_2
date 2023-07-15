package org.n1.av2.backend.service.layerhacking.ice

import org.n1.av2.backend.entity.ice.IceStatus
import org.n1.av2.backend.entity.site.NodeEntityService
import org.n1.av2.backend.entity.site.enums.LayerType
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

    fun findOrCreateIceForLayer(layer: Layer): IceStatus {
        if (layer is PasswordIceLayer) return authAppService.findOrCreateIceStatus(layer)

        val iceId = findOrCreateIceForNonPasswordIce(layer)
        return authAppService.findOrCreateIceStatus(iceId, layer)
    }

    private fun findOrCreateIceForNonPasswordIce(layer: Layer): String {
        return when (layer) {
            is TangleIceLayer -> tangleService.findOrCreateIceByLayerId(layer).id
            is WordSearchIceLayer -> wordSearchService.findOrCreateIceByLayerId(layer).id
            is NetwalkIceLayer -> netwalkIceService.findOrCreateIceByLayerId(layer).id
            is TarIceLayer -> tarService.findOrCreateIceByLayerId(layer).id
            else -> error("Unknown ice type: ${layer.type}")
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

    class ProtectionResponse(val type: LayerType, val iceId: String, val path: String, val level: Int)
    fun checkIceProtectingApp(appId: String, hackerUpToLevel: Int?, layerId: String): ProtectionResponse? {

        val node = nodeEntityService.findByLayerId(layerId)
        val targetLayer = node.layers.find { it.id == layerId } ?: error("Layer not found: $layerId")
        val targetLevel = targetLayer.level

        node.layers.forEach { layer ->
            if ((hackerUpToLevel != null && layer.level > hackerUpToLevel)  ||  // The hacker has already hacked this level
                !layer.type.ice ||  // non-ice does not provide protection
                layer.level < targetLevel) return@forEach // ice below the app does not provide protection

            val iceStatus = findOrCreateIceForLayer(layer)
            if (iceStatus.authorized.contains(currentUserService.userId)) return@forEach // the user is already authorized

            return ProtectionResponse(layer.type, iceStatus.id, "app/${appId}", layer.level -1)
        }

        return null
    }

}