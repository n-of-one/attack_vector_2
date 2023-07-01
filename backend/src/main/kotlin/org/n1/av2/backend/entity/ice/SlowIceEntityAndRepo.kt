package org.n1.av2.backend.entity.ice

import org.n1.av2.backend.entity.site.enums.IceStrength
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Document
data class SlowIceStatus(
    val id: String,
    @Indexed val layerId: String,
    val hacked : Boolean,
    val strength: IceStrength,
    val totalUnits: Int,
    val unitsHacked: Int = 0
)

@Repository
interface SlowIceStatusRepo: CrudRepository<SlowIceStatus, String> {
    fun findByLayerId(layerId: String): SlowIceStatus?
}
