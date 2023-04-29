package org.n1.av2.backend.entity.ice

import org.n1.av2.backend.entity.site.enums.IceStrength
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

data class SlowIceStatus(
    val id: String,
    val runId: String,
    val nodeId: String,
    val layerId: String,
    val hacked : Boolean = false,
    val strength: IceStrength,

    val totalUnits: Int,
    val unitsHacked: Int = 0
)

@Repository
interface SlowIceStatusRepo: CrudRepository<SlowIceStatus, String> {
    fun deleteAllByRunId(runId: String)
}
