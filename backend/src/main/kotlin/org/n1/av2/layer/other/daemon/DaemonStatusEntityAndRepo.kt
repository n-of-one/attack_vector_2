package org.n1.av2.layer.other.daemon

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

/**
 * Records which hackers have been shown the text of a daemon layer. Removed when the site resets.
 */
@Document
data class DaemonStatusEntity(
    @Id val id: String,
    @Indexed val siteId: String,
    @Indexed val layerId: String,
    val triggeredUserIds: List<String>,
)

@Repository
interface DaemonStatusRepository : CrudRepository<DaemonStatusEntity, String> {
    fun findByLayerId(layerId: String): DaemonStatusEntity?
    fun deleteBySiteId(siteId: String)
}
