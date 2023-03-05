package org.n1.av2.backend.entity.run

import org.n1.av2.backend.entity.site.Node
import org.n1.av2.backend.util.createId
import org.springframework.stereotype.Service
import java.util.*

@Service
class LayerStatusEntityService(
        private val layerStatusRepo: LayerStatusRepo
) {

    fun get(layerId: String, runId: String): LayerStatus {
        return layerStatusRepo.findByLayerIdAndRunId(layerId, runId) ?: error("No layer status found for layerId: ${layerId} runId: ${runId}")
    }

    fun getOrCreate(layerId: String, runId: String): LayerStatus {
        return layerStatusRepo.findByLayerIdAndRunId(layerId, runId) ?: createLayerStatus(layerId, runId, null)
    }

    fun createLayerStatus(layerId: String, runId: String, iceId: String?): LayerStatus {
        val id = createId("layerStatus-")
        val status = LayerStatus(id, layerId, runId, false, LinkedList(), iceId)
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