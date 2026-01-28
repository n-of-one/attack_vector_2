package org.n1.av2.layer.ice.tar

import org.n1.av2.site.entity.enums.IceStrength
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Document
data class TarIceStatus(
    @Id val id: String,
    @Indexed val layerId: String,
    val hacked : Boolean,
    val strength: IceStrength,
    val totalUnits: Int,
    val unitsHacked: Int = 0
)

@Repository
interface TarIceStatusRepo: CrudRepository<TarIceStatus, String> {
    fun findByLayerId(layerId: String): TarIceStatus?
}
