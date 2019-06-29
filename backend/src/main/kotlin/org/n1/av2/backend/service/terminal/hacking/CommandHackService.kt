package org.n1.av2.backend.service.terminal.hacking

import org.n1.av2.backend.model.db.run.HackerPosition
import org.n1.av2.backend.model.db.site.Node
import org.n1.av2.backend.model.db.site.enums.ServiceType
import org.n1.av2.backend.service.StompService
import org.n1.av2.backend.service.run.HackerPositionService
import org.n1.av2.backend.service.service.ServiceOs
import org.n1.av2.backend.service.service.ServiceText
import org.n1.av2.backend.service.site.NodeService
import org.springframework.stereotype.Service

@Service
class CommandHackService(
        private val stompService: StompService,
        private val nodeService: NodeService,
        private val hackerPositionService: HackerPositionService,
        private val serviceOs: ServiceOs,
        private val serviceText: ServiceText
) {

    fun process(runId: String, tokens: List<String>) {
        if (tokens.size == 1) {
            return stompService.terminalReceive("Missing [primary]<layer>[/]        -- for example: [u]hack[primary] 0")

        }
        val position = hackerPositionService.get()
        val node = nodeService.getById(position.currentNodeId)

        val layer = tokens[1].toIntOrNull() ?: return reportLayerUnknown(node, tokens[1])
        if (layer < 0 || layer >= node.services.size) return reportLayerUnknown(node, tokens[1])

        val blockingIceLayer = findBlockingIceLayer(node)
        if (blockingIceLayer != null && blockingIceLayer > layer) return reportBlockingIce(node, blockingIceLayer)

        handleHack(node, layer, position)
    }

    private fun handleHack(node: Node, layer: Int, position: HackerPosition) {
        val service = node.services.find { it.layer == layer }!!

        when (service.type) {
            ServiceType.OS -> serviceOs.hack(service, node, position)
            ServiceType.TEXT -> serviceText.hack(service, node)
            else -> stompService.terminalReceive("Service type not supported yet: ${service.type}")
        }
    }


    private fun reportLayerUnknown(node: Node, layerInput: String) {
        val serviceCount = node.services.size
        if (serviceCount == 1) {
            stompService.terminalReceive("[error]layer error[/] - Layer number [primary]${layerInput}[/] not understood.",
                    "This node has only one service, the only valid option is: [u]hack [primary]0[/].")
        } else {
            stompService.terminalReceive("[error]layer error[/] - Layer number [primary]${layerInput}[/] not understood.",
                    "This node has ${serviceCount} services, so use a number between [primary]0[/] and [primary]${serviceCount - 1}[/].")
        }
    }

    private fun reportBlockingIce(node: Node, blockingIceLayer: Int) {
        val ice = node.services.find { it.layer == blockingIceLayer }!!
        stompService.terminalReceive("[warn b]blocked[/] - ICE (${ice.name}) blocks hacking. Hack the ICE first: [u]hack[/] [primary]${blockingIceLayer}")
    }


}