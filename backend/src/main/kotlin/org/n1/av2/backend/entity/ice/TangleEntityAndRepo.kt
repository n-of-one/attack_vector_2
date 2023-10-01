package org.n1.av2.backend.entity.ice

import org.n1.av2.backend.entity.site.enums.IceStrength
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Document
data class TangleIceStatus(
    val id: String,
    @Indexed val layerId: String,
    val strength: IceStrength,
    val originalPoints: MutableList<TanglePoint>,
    val points: MutableList<TanglePoint>,
    val lines: List<TangleLine>,
)

data class TanglePoint(val id: String, val x: Int, val y: Int)

enum class TangleLineType { NORMAL, SETUP}
data class TangleLine(val id: String, val fromId: String, val toId: String, val type: TangleLineType)


@Repository
interface TangleIceStatusRepo: CrudRepository<TangleIceStatus, String> {
    fun findByLayerId(layerId: String): TangleIceStatus?
}
