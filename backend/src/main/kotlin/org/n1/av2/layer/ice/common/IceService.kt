package org.n1.av2.layer.ice.common

import org.n1.av2.layer.ice.netwalk.NetwalkIceLayer
import org.n1.av2.layer.ice.netwalk.NetwalkIceService
import org.n1.av2.layer.ice.password.AuthAppService
import org.n1.av2.layer.ice.password.PasswordIceLayer
import org.n1.av2.layer.ice.sweeper.SweeperIceLayer
import org.n1.av2.layer.ice.sweeper.SweeperService
import org.n1.av2.layer.ice.tangle.TangleIceLayer
import org.n1.av2.layer.ice.tangle.TangleService
import org.n1.av2.layer.ice.tar.TarIceLayer
import org.n1.av2.layer.ice.tar.TarService
import org.n1.av2.layer.ice.wordsearch.WordSearchIceLayer
import org.n1.av2.layer.ice.wordsearch.WordSearchService
import org.n1.av2.site.entity.NodeEntityService
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct

// To prevent circular bean dependencies
@Configuration
class InitIceService(
    val iceService: IceService,
    val tangleService: TangleService,
    val authAppService: AuthAppService,
    val wordSearchService: WordSearchService,
    val netwalkIceService: NetwalkIceService,
    val sweeperService: SweeperService,
    val tarService: TarService
) {

    @PostConstruct
    fun postConstruct() {
        iceService.tangleService = tangleService
        iceService.authAppService = authAppService
        iceService.wordSearchService = wordSearchService
        iceService.netwalkIceService = netwalkIceService
        iceService.tarService = tarService
        iceService. sweeperService = sweeperService
    }
}

@Service
class IceService(
    private val nodeEntityService: NodeEntityService,
) {

    lateinit var tangleService: TangleService
    lateinit var authAppService: AuthAppService
    lateinit var wordSearchService: WordSearchService
    lateinit var netwalkIceService: NetwalkIceService
    lateinit var tarService: TarService
    lateinit var sweeperService: SweeperService


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
            is SweeperIceLayer -> return sweeperService.findOrCreateIceStatus(layer).id
            else -> error("Unsupported ice layer: $layer")
        }
    }

    fun deleteIce(siteId: String) {
        val iceLayers = nodeEntityService
            .findBySiteId(siteId)
            .map { node -> node.layers }
            .flatten()
            .filterIsInstance<IceLayer>()

        iceLayers.forEach { authAppService.deleteByLayerId(it.id) }

        iceLayers.filterIsInstance<TangleIceLayer>().forEach { tangleService.deleteByLayerId(it.id) }
        iceLayers.filterIsInstance<WordSearchIceLayer>().forEach { wordSearchService.deleteByLayerId(it.id) }
        iceLayers.filterIsInstance<NetwalkIceLayer>().forEach { netwalkIceService.deleteByLayerId(it.id) }
        iceLayers.filterIsInstance<TarIceLayer>().forEach { tarService.deleteByLayerId(it.id) }
        iceLayers.filterIsInstance<SweeperIceLayer>().forEach { sweeperService.deleteByLayerId(it.id) }
    }


}
