package org.n1.av2.layer.ice.tangle

import org.n1.av2.site.entity.enums.IceStrength
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Document
data class TangleIceStatus(
    @Id val id: String,
    @Indexed val layerId: String,
    val strength: IceStrength,
    val originalPoints: MutableList<TanglePoint>,
    val points: MutableList<TanglePoint>,
    val lines: List<TangleLine>,
    val clustersRevealed: Boolean,
)

data class TanglePoint(val id: String, val x: Int, val y: Int, val cluster: Int)

enum class TangleLineType { NORMAL, SETUP}
data class TangleLine(val id: String, val fromId: String, val toId: String, val type: TangleLineType)


@Repository
interface TangleIceStatusRepo: CrudRepository<TangleIceStatus, String> {
    fun findByLayerId(layerId: String): TangleIceStatus?
}
