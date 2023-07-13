package org.n1.av2.backend.service.layerhacking.ice

import org.n1.av2.backend.entity.site.NodeEntityService
import org.n1.av2.backend.entity.site.layer.Layer
import org.n1.av2.backend.entity.site.layer.ice.*
import org.n1.av2.backend.service.layerhacking.ice.netwalk.NetwalkIceService
import org.n1.av2.backend.service.layerhacking.ice.tangle.TangleService
import org.n1.av2.backend.service.layerhacking.ice.tar.TarService
import org.n1.av2.backend.service.layerhacking.ice.wordsearch.WordSearchService
import org.springframework.stereotype.Service

@Service
class IceService(
    private val nodeEntityService: NodeEntityService,
    private val tangleService: TangleService,
    private val iceAppService: IceAppService,
    private val wordSearchService: WordSearchService,
    private val netwalkIceService: NetwalkIceService,
    private val tarService: TarService,
) {

    fun findIceIdForLayer(layer: Layer): String {
        if (layer is PasswordIceLayer) return iceAppService.findOrCreateIceStatus(layer).id

        val iceId = findOrCreateIce(layer)
        val iceStatus = iceAppService.findOrCreateIceStatus(iceId, layer)
        return iceStatus.id
    }

    fun findOrCreateIce(layer: Layer): String {
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
        allLayers.filterIsInstance<PasswordIceLayer>().forEach { iceAppService.deleteByLayerId(it.id) }
        allLayers.filterIsInstance<WordSearchIceLayer>().forEach { wordSearchService.deleteByLayerId(it.id) }
        allLayers.filterIsInstance<NetwalkIceLayer>().forEach { netwalkIceService.deleteByLayerId(it.id) }
        allLayers.filterIsInstance<TarIceLayer>().forEach { tarService.deleteByLayerId(it.id) }
    }
}