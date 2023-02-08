package org.n1.av2.backend.repo

import org.n1.av2.backend.model.db.run.*
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface ScanRepo : CrudRepository<Scan, String> {
    fun findByRunIdIn(siteIds: List<String>): List<Scan>
    fun findByRunId(runId: String): Scan?
}

@Repository
interface UserScanRepo : CrudRepository<UserScan, String> {
    fun findAllByUserId(userId:String): List<UserScan>
    fun findAllByRunId(runId: String): List<UserScan>
    fun findByUserIdAndRunId(userId: String, runId: String): UserScan?
    fun deleteByUserIdAndRunId(userId: String, runId: String)
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
}

@Repository
interface NodeStatusRepo: CrudRepository<NodeStatus, String> {
    fun findByRunId(runId: String): List<NodeStatus>
    fun findByNodeIdAndRunId(layerId: String, runId: String): NodeStatus?
}


@Repository
interface IceStatusRepo: CrudRepository<IceStatus, String> {
    fun findByLayerIdAndRunId(layerId: String, runId: String): IceStatus?
}


@Repository
interface TracingPatrollerRepo: CrudRepository<TracingPatroller, String> {
    fun findAllByRunId(runId: String): List<TracingPatroller>
    fun findAllByTargetUserId(userId: String): List<TracingPatroller>
}
