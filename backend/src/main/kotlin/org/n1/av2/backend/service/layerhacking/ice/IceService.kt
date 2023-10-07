package org.n1.av2.backend.service.layerhacking.ice

import org.n1.av2.backend.entity.site.NodeEntityService
import org.n1.av2.backend.entity.site.layer.ice.*
import org.n1.av2.backend.service.layerhacking.app.auth.AuthAppService
import org.n1.av2.backend.service.layerhacking.ice.netwalk.NetwalkIceService
import org.n1.av2.backend.service.layerhacking.ice.tangle.TangleService
import org.n1.av2.backend.service.layerhacking.ice.tar.TarService
import org.n1.av2.backend.service.layerhacking.ice.wordsearch.WordSearchService
import org.springframework.stereotype.Service

@Service
class IceService(
    private val nodeEntityService: NodeEntityService,
    private val tangleService: TangleService,
    private val authAppService: AuthAppService,
    private val wordSearchService: WordSearchService,
    private val netwalkIceService: NetwalkIceService,
    private val tarService: TarService,
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
        val iceLayers = nodeEntityService
            .getAll(siteId)
            .map { node -> node.layers }
            .flatten()
            .filterIsInstance<IceLayer>()

        iceLayers.forEach { authAppService.deleteByLayerId(it.id) }

        iceLayers.filterIsInstance<TangleIceLayer>().forEach { tangleService.deleteByLayerId(it.id) }
        iceLayers.filterIsInstance<WordSearchIceLayer>().forEach { wordSearchService.deleteByLayerId(it.id) }
        iceLayers.filterIsInstance<NetwalkIceLayer>().forEach { netwalkIceService.deleteByLayerId(it.id) }
        iceLayers.filterIsInstance<TarIceLayer>().forEach { tarService.deleteByLayerId(it.id) }
    }




}