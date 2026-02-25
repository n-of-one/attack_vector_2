package org.n1.av2.layer.app.status_light

import org.n1.av2.editor.EditorService
import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.platform.connection.ServerActions
import org.n1.av2.site.entity.Node
import org.n1.av2.site.entity.NodeEntityService
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct

@Configuration
class InitStatusLightService(
    private val statusLightService: StatusLightService,
    private val editorService: EditorService,
) {
    @PostConstruct
    fun postConstruct() {
        statusLightService.editorService = editorService
    }
}


@Service
class StatusLightService(
    private val nodeEntityService: NodeEntityService,
    private val connectionService: ConnectionService,
) {
    lateinit var editorService: EditorService

    class StatusLightMessage(val switchLabel: String, val currentOption: Int, val options: List<StatusLightOption>)

    fun enter(layerId: String) {
        val (_, layer) = findByLayerId(layerId)
        sendUpdate(layer)
    }

    // Called by Switch app
    fun setValue(layerId: String, newOption: Int) {
        val (node, layer) = findByLayerId(layerId)
        layer.currentOption = newOption
        nodeEntityService.save(node)

        sendUpdate(layer)
        editorService.sendLayerUpdateMessage(node.siteId, node.id, layer)
    }

    fun sendUpdate(layer: StatusLightLayer) {
        val message = StatusLightMessage(layer.switchLabel, layer.currentOption, layer.options)
        connectionService.toApp(layer.id, ServerActions.SERVER_STATUS_LIGHT_UPDATE, message)
    }

    fun findByLayerId(layerId: String): Pair<Node, StatusLightLayer> {
        val node = nodeEntityService.findByLayerId(layerId)
        val layer = node.getLayerById(layerId)
        if (layer !is StatusLightLayer) error("Layer ${layerId} is not a StatusLightLayer")
        return Pair(node, layer)
    }
}
