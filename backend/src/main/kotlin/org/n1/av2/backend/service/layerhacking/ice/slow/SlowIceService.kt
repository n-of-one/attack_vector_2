package org.n1.av2.backend.service.layerhacking.ice.slow

import org.n1.av2.backend.entity.ice.SlowIceStatus
import org.n1.av2.backend.entity.ice.SlowIceStatusRepo
import org.n1.av2.backend.entity.run.Run
import org.n1.av2.backend.entity.site.layer.SlowIceLayer
import org.n1.av2.backend.util.createId
import org.springframework.stereotype.Service

@Service
class SlowIceService(
    private val slowIceStatusRepo: SlowIceStatusRepo
) {

    fun createIce(layer: SlowIceLayer, nodeId: String, runId: String): SlowIceStatus {
        val totalUnits = SlowIceCreator().create(layer.strength)
        val id = createId("slow", slowIceStatusRepo::findById)

        val slowIceEntity = SlowIceStatus(
            id = id,
            runId = runId,
            nodeId = nodeId,
            layerId = layer.id,
            strength = layer.strength,
            hacked = false,
            totalUnits = totalUnits,
            unitsHacked = 0
        )

        return slowIceStatusRepo.save(slowIceEntity)
    }

    fun deleteAllForRuns(runs: List<Run>) {
        runs.forEach { slowIceStatusRepo.deleteAllByRunId(it.runId) }
    }

}