package org.n1.av2.backend.service.layerhacking.app.status_light

import org.n1.av2.backend.entity.site.Node
import org.n1.av2.backend.entity.site.NodeEntityService
import org.n1.av2.backend.entity.site.layer.other.StatusLightLayer
import org.n1.av2.backend.model.ui.ServerActions
import org.n1.av2.backend.service.site.EditorService
import org.n1.av2.backend.service.util.StompService
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct

@Configuration
class InitStatusLightService(
    private val statusLightService: StatusLightService,
    private val editorService: EditorService,
){
    @PostConstruct
    fun postConstruct() {
        statusLightService.editorService = editorService
    }
}


@Service
class StatusLightService(
    private val nodeEntityService: NodeEntityService,
    private val stompService: StompService,
) {
    lateinit var editorService: EditorService

    class StatusLightMessage(val status: Boolean, val textForRed: String, val textForGreen: String)
    fun enter(layerId: String) {
        val (_, layer) = findByLayerId(layerId)
        sendUpdate(layer)
    }

    // Called by Switch app
    fun setValue(layerId: String, newValue: Boolean) {
        val (node, layer) = findByLayerId(layerId)
        layer.status = newValue
        nodeEntityService.save(node)

        sendUpdate(layer)
        editorService.sendLayerUpdateMessage(node.siteId, node.id, layer)
    }

    fun sendUpdate(layer: StatusLightLayer) {
        val message = StatusLightMessage(layer.status, layer.textForRed, layer.textForGreen)
        stompService.toApp(layer.id, ServerActions.SERVER_STATUS_LIGHT_UPDATE, message)
    }

    fun findByLayerId(layerId: String): Pair<Node, StatusLightLayer> {
        val node = nodeEntityService.findByLayerId(layerId)
        val layer = node.getLayerById(layerId)
        if (layer !is StatusLightLayer) error("Layer ${layerId} is not a StatusLightLayer")
        return Pair(node, layer)
    }
}
