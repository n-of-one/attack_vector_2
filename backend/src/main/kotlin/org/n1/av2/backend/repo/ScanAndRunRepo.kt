package org.n1.av2.backend.repo

import org.n1.av2.backend.model.db.run.*
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

@Repository
interface ScanRepo : PagingAndSortingRepository<Scan, String> {
    fun findByRunIdIn(siteIds: List<String>): List<Scan>
    fun findByRunId(runId: String): Scan?
}

@Repository
interface UserScanRepo : PagingAndSortingRepository<UserScan, String> {
    fun findAllByUserId(userId:String): List<UserScan>
    fun findAllByRunId(runId: String): List<UserScan>
    fun findByUserIdAndRunId(userId: String, runId: String): UserScan?
    fun deleteByUserIdAndRunId(userId: String, runId: String)
}


@Repository
interface HackerStateRepo : PagingAndSortingRepository<HackerState, String> {
    fun findByUserId(userId: String): HackerState?
    fun findByRunId(runId: String):List<HackerState>
}

@Repository
interface LayerStatusRepo: PagingAndSortingRepository<LayerStatus, String> {
    fun findByLayerIdAndRunId(layerId: String, runId: String): LayerStatus?
    fun findByRunId(runId: String): List<LayerStatus>
    fun findByRunIdAndLayerIdIn(runId: String, layerIds: List<String>): List<LayerStatus>
}

@Repository
interface NodeStatusRepo: PagingAndSortingRepository<NodeStatus, String> {
    fun findByRunId(runId: String): List<NodeStatus>
    fun findByNodeIdAndRunId(layerId: String, runId: String): NodeStatus?
}


@Repository
interface IceStatusRepo: PagingAndSortingRepository<IceStatus, String> {
    fun findByLayerIdAndRunId(layerId: String, runId: String): IceStatus?
}


@Repository
interface TracingPatrollerRepo: PagingAndSortingRepository<TracingPatroller, String> {
    fun findAllByRunId(runId: String): List<TracingPatroller>
    fun findAllByTargetUserId(userId: String): List<TracingPatroller>
}
