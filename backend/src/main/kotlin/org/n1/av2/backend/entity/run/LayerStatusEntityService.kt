package org.n1.av2.backend.entity.run

import org.n1.av2.backend.entity.site.Node
import org.n1.av2.backend.util.createId
import org.springframework.stereotype.Service
import java.util.*

@Service
class LayerStatusEntityService(
        private val layerStatusRepo: LayerStatusRepo
) {

    fun getOrCreate(layerId: String, runId: String): LayerStatus {
        return layerStatusRepo.findByLayerIdAndRunId(layerId, runId) ?: createLayerStatus(layerId, runId)
    }

    private fun createLayerStatus(layerId: String, runId: String): LayerStatus {
        val id = createId("layerStatus-")
        val status = LayerStatus(id, layerId, runId, false, LinkedList())
        layerStatusRepo.save(status)
        return status
    }

    fun save(layerStatus: LayerStatus) {
        layerStatusRepo.save(layerStatus)
    }

    fun getLayerStatuses(iceLayerIds: List<String>, runId: String): List<LayerStatus> {
        return layerStatusRepo.findByRunIdAndLayerIdIn(runId, iceLayerIds)
    }

    fun getLayerStatuses(node: Node, runId: String): List<LayerStatus> {
        return getLayerStatuses(node.layers.map { it.id }, runId)
    }


    fun getForRun(runId: String): List<LayerStatus> {
        return layerStatusRepo.findByRunId(runId)
    }
}