package org.n1.av2.layer.ice.sweeper

import org.n1.av2.site.entity.enums.IceStrength
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository


/**
Cells is a string representation of the board, regardless of visibility or modifiers like flags.
mine: *
empty: 0
adjacent to mine: 1-8

For example:
*1001**
1211122
01*1000
0111000

Modifiers is a string representation of the modifiers:
h hidden
r revealed
f flag

For example:
rrrrrrf
rrrrrrr
rrhrrrr
rrrrrrr

Note that in this example, the top left mine has exploded, as it has been revealed

*/

const val MINE = '*'
const val HIDDEN = 'h'
const val REVEALED = 'r'
const val FLAG = 'f'
const val NEIGHBOUR_MINES_0 = '0'

@Document
data class SweeperIceStatus(
    @Id val id: String,
    @Indexed val layerId: String,
    val strength: IceStrength,
    val cells: List<String>,
    val modifiers: MutableList<String>,
    val hacked: Boolean,
    val blockedUserIds: List<String> = emptyList()
)

@Repository
interface SweeperIceStatusRepo: CrudRepository<SweeperIceStatus, String> {
    fun findByLayerId(layerId: String): SweeperIceStatus?
}
