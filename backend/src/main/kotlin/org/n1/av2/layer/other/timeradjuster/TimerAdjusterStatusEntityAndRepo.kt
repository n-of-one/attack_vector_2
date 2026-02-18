package org.n1.av2.layer.other.timeradjuster

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Document
data class TimerAdjusterStatusEntity(
    @Id val id: String,
    @Indexed val siteId: String,
    @Indexed val layerId: String,
    val enteredUserIds: List<String>,
)

@Repository
interface TimerAdjusterStatusRepository : CrudRepository<TimerAdjusterStatusEntity, String> {
    fun findByLayerId(layerId: String): TimerAdjusterStatusEntity?
    fun deleteBySiteId(siteId: String)
}


