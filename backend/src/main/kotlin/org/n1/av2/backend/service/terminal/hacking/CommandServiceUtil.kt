package org.n1.av2.backend.service.terminal.hacking

import org.n1.av2.backend.model.db.layer.Layer
import org.n1.av2.backend.model.db.site.Node
import org.n1.av2.backend.repo.LayerStatusRepo
import org.springframework.stereotype.Service

@Service
class CommandServiceUtil(
        private val serviceStatusRepo: LayerStatusRepo
) {
    fun findBlockingIceLayer(node: Node, runId: String): Layer? {

        if (node.layers.none { it.type.ice }) {
            return null
        }

        val iceLayerIds = node.layers.filter {it.type.ice }. map { it.id }
        val layerStatuses = serviceStatusRepo.findByRunIdAndLayerIdIn(runId, iceLayerIds)
        val hackedLayerIds = layerStatuses.filter { it.hacked } .map { it.layerId }
        val nonHackedIceLayerIds = iceLayerIds.filter { !hackedLayerIds.contains(it) }
        if (nonHackedIceLayerIds.isEmpty()) {
            return null
        }
        val upperNonHackedIceLayerId = nonHackedIceLayerIds.last()
        return node.layers.find {it.id == upperNonHackedIceLayerId}!!
    }
}