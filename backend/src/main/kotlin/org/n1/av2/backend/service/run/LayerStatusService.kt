package org.n1.av2.backend.service.run

import org.n1.av2.backend.model.db.run.LayerStatus
import org.n1.av2.backend.repo.LayerStatusRepo
import org.n1.av2.backend.util.createId
import org.springframework.stereotype.Service
import java.util.*

@Service
class LayerStatusService(
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

    fun getServicesStatus(runId: String, iceLayerIds: List<String>): List<LayerStatus> {
        return layerStatusRepo.findByRunIdAndLayerIdIn(runId, iceLayerIds)
    }

    fun getForRun(runId: String): List<LayerStatus> {
        return layerStatusRepo.findByRunId(runId)
    }
}