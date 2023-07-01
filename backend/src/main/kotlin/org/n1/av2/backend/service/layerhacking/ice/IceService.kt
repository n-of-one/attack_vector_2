package org.n1.av2.backend.service.layerhacking.ice

import org.n1.av2.backend.entity.site.NodeEntityService
import org.n1.av2.backend.entity.site.layer.Layer
import org.n1.av2.backend.entity.site.layer.ice.*
import org.n1.av2.backend.service.layerhacking.ice.netwalk.NetwalkIceService
import org.n1.av2.backend.service.layerhacking.ice.password.PasswordIceService
import org.n1.av2.backend.service.layerhacking.ice.slow.SlowIceService
import org.n1.av2.backend.service.layerhacking.ice.tangle.TangleService
import org.n1.av2.backend.service.layerhacking.ice.wordsearch.WordSearchService
import org.springframework.stereotype.Service

@Service
class IceService(
    private val nodeEntityService: NodeEntityService,
    private val tangleService: TangleService,
    private val passwordIceService: PasswordIceService,
    private val wordSearchService: WordSearchService,
    private val netwalkIceService: NetwalkIceService,
    private val slowIceService: SlowIceService,
) {

    fun findIceIdForLayer(layer: Layer): String? {
        return when (layer) {
            is TangleIceLayer -> tangleService.findOrCreateIceByLayerId(layer).id
            is WordSearchIceLayer -> wordSearchService.findOrCreateIceByLayerId(layer).id
            is PasswordIceLayer -> passwordIceService.findOrCreateIceByLayerId(layer).id
            is NetwalkIceLayer -> netwalkIceService.findOrCreateIceByLayerId(layer).id
            is SlowIceLayer -> slowIceService.findOrCreateIceByLayerId(layer).id
            else -> null
        }
    }

    fun deleteIce(siteId: String) {

        val allLayers = nodeEntityService.getAll(siteId).map { node -> node.layers }.flatten()

        allLayers.filterIsInstance<TangleIceLayer>().forEach { tangleService.deleteByLayerId(it.id) }
        allLayers.filterIsInstance<PasswordIceLayer>().forEach { passwordIceService.deleteByLayerId(it.id) }
        allLayers.filterIsInstance<WordSearchIceLayer>().forEach { wordSearchService.deleteByLayerId(it.id) }
        allLayers.filterIsInstance<NetwalkIceLayer>().forEach { netwalkIceService.deleteByLayerId(it.id) }
        allLayers.filterIsInstance<SlowIceLayer>().forEach { slowIceService.deleteByLayerId(it.id) }
    }
}