package org.n1.av2.layer.ice.common

import org.n1.av2.layer.Layer
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
import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.platform.connection.ServerActions
import org.n1.av2.site.entity.Node
import org.n1.av2.site.entity.NodeEntityService
import org.n1.av2.site.entity.ThemeService
import org.n1.av2.site.entity.enums.LayerType
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct
import kotlin.reflect.KClass

// To prevent circular bean dependencies
@Configuration
class InitIceService(
    private val iceService: IceService,
    private val tangleService: TangleService,
    private val authAppService: AuthAppService,
    private val wordSearchService: WordSearchService,
    private val netwalkIceService: NetwalkIceService,
    private val sweeperService: SweeperService,
    private val tarService: TarService,
) {

    @PostConstruct
    fun postConstruct() {
        iceService.tangleService = tangleService
        iceService.authAppService = authAppService
        iceService.wordSearchService = wordSearchService
        iceService.netwalkIceService = netwalkIceService
        iceService.tarService = tarService
        iceService.sweeperService = sweeperService
    }
}

@Service
class IceService(
    private val nodeEntityService: NodeEntityService,
    private val themeService: ThemeService,
    private val connectionService: ConnectionService,
) {

    lateinit var tangleService: TangleService
    lateinit var authAppService: AuthAppService
    lateinit var wordSearchService: WordSearchService
    lateinit var netwalkIceService: NetwalkIceService
    lateinit var tarService: TarService
    lateinit var sweeperService: SweeperService


    fun findOrCreateIceForLayerAndIceStatus(layer: IceLayer): String {
        val iceId = createIceForLayerInternal(layer)
        authAppService.findOrCreateIceStatus(iceId, layer)
        return iceId
    }

    fun createIceForLayerInternal(layer: IceLayer): String {
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

    fun resetIceForSite(siteId: String) {

        nodeEntityService.findBySiteId(siteId).forEach { node ->
            var iceChangedBackToOriginal = false
            node.layers
                .filterIsInstance<IceLayer>()
                .forEach { currentIceLayer ->
                    resetIceForLayer(currentIceLayer)
                    if (currentIceLayer.original != null) {
                        currentIceLayer.original.hacked = false
                        node.layers[node.layers.indexOf(currentIceLayer)] = currentIceLayer.original
                        informFrontendOfLayerChanged(node, currentIceLayer.original)
                        iceChangedBackToOriginal = true
                    }
                }
            if (iceChangedBackToOriginal) {
                nodeEntityService.save(node)
            }
        }
    }

    class LayerUpdatedData(val nodeId: String, val layer: Layer)
    fun informFrontendOfLayerChanged(node: Node, updatedLayer: Layer) {
        connectionService.toSite(node.siteId, ServerActions.SERVER_LAYER_CHANGED, LayerUpdatedData(node.id, updatedLayer))
    }

    fun resetIceForLayer(layer: IceLayer) {
        when (layer) {
            is TangleIceLayer -> tangleService.resetIceByLayerId(layer.id)
            is WordSearchIceLayer -> wordSearchService.resetIceByLayerId(layer.id)
            is NetwalkIceLayer -> netwalkIceService.resetIceByLayerId(layer.id)
            is TarIceLayer -> tarService.resetIceByLayerId(layer.id)
            is PasswordIceLayer -> authAppService.resetIceByLayerId(layer.id)
            is SweeperIceLayer -> sweeperService.resetIceByLayerId(layer.id)
            else -> error("Unsupported ice layer: $layer")
        }
        authAppService.deleteByLayerId(layer.id)
    }

    fun klassFor(layerType: LayerType): KClass<out IceLayer> = when (layerType) {
        LayerType.TANGLE_ICE -> TangleIceLayer::class
        LayerType.WORD_SEARCH_ICE -> WordSearchIceLayer::class
        LayerType.NETWALK_ICE -> NetwalkIceLayer::class
        LayerType.TAR_ICE -> TarIceLayer::class
        LayerType.PASSWORD_ICE -> PasswordIceLayer::class
        LayerType.SWEEPER_ICE -> SweeperIceLayer::class
        else -> error("Unsupported ice layer type: $layerType")
    }

    fun formalNameFor(layerType: LayerType): String = "${themeService.themeName(layerType)} ICE"
    fun helpfulNameFor(layerType: LayerType): String = "${themeService.themeName(layerType)} ICE (${themeService.iceSimpleName(layerType)})"

}
