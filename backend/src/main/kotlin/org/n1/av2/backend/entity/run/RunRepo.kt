package org.n1.av2.backend.entity.run

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface RunRepo : CrudRepository<Run, String> {
    fun findByRunIdIn(runIds: List<String>): List<Run>
    fun findBySiteId(siteId: String): List<Run>
    fun findByRunId(runId: String): Run?
}

@Repository
interface UserRunLinkRepo : CrudRepository<UserRunLink, String> {
    fun findAllByUserId(userId:String): List<UserRunLink>
    fun findAllByRunId(runId: String): List<UserRunLink>
    fun findByUserIdAndRunId(userId: String, runId: String): UserRunLink?
    fun deleteByUserIdAndRunId(userId: String, runId: String)
    fun deleteAllByRunId(runId: String)
}


@Repository
interface HackerStateRepo : CrudRepository<HackerState, String> {
    fun findByUserId(userId: String): HackerState?
    fun findByRunId(runId: String):List<HackerState>
}

@Repository
interface LayerStatusRepo: CrudRepository<LayerStatus, String> {
    fun findByLayerIdAndRunId(layerId: String, runId: String): LayerStatus?
    fun findByRunId(runId: String): List<LayerStatus>
    fun findByRunIdAndLayerIdIn(runId: String, layerIds: List<String>): List<LayerStatus>
    fun findByIceId(iceId: String): LayerStatus?
    fun deleteAllByRunId(runId: String)
}

@Repository
interface NodeStatusRepo: CrudRepository<NodeStatus, String> {
    fun findByRunId(runId: String): List<NodeStatus>
    fun findByNodeIdAndRunId(layerId: String, runId: String): NodeStatus?
    fun deleteAllByRunId(runId: String)
}

@Repository
interface TracingPatrollerRepo: CrudRepository<TracingPatroller, String> {
    fun findAllByRunId(runId: String): List<TracingPatroller>
    fun findAllByTargetUserId(userId: String): List<TracingPatroller>
    fun deleteAllByRunId(runId: String)
}
