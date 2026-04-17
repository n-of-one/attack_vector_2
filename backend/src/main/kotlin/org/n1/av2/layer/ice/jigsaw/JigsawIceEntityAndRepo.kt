package org.n1.av2.layer.ice.jigsaw

import org.n1.av2.site.entity.enums.IceStrength
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

/**
 * Edge of a piece. Flat (border) edges have shape = null and direction = "flat".
 * Shaped edges have shape in {invected, embattled, indented, raguly} and direction in {in, out}.
 * Adjacent pieces on a shared internal edge have the same shape with flipped direction.
 */
data class EdgeConfig(
    val shape: String?,      // null for flat; otherwise one of the shape names
    val direction: String,   // "flat" | "in" | "out"
)

/**
 * Immutable geometry for one puzzle piece. Position and rotation live on the owning JigsawGroup,
 * not here — pieces are positioned via their group container.
 */
data class PieceConfig(
    val column: Int,
    val row: Int,
    val top: EdgeConfig,
    val right: EdgeConfig,
    val bottom: EdgeConfig,
    val left: EdgeConfig,
)

/** A piece's position on the puzzle grid. */
data class GridPosition(
    val column: Int,
    val row: Int,
)

/**
 * Authoritative game state unit. Initially one group per piece; snaps merge groups.
 * Solve = groups.size == 1 && groups[0].rotation == 0.
 *
 * (x, y) is the group's pivot in canvas coordinates — matches the client's Container.position basis.
 */
data class JigsawGroup(
    val id: String,
    val pieces: List<GridPosition>,
    val rotation: Int,      // 0, 90, 180, 270
    val x: Double,
    val y: Double,
)

@Document
data class JigsawIceStatus(
    @Id val id: String,
    @Indexed val layerId: String,
    val strength: IceStrength,
    val hacked: Boolean,
    val columns: Int,
    val rows: Int,
    val imageSource: String,
    val pieces: List<PieceConfig>,
    val groups: List<JigsawGroup>,
)

@Repository
interface JigsawIceStatusRepo : CrudRepository<JigsawIceStatus, String> {
    fun findByLayerId(layerId: String): JigsawIceStatus?
}
