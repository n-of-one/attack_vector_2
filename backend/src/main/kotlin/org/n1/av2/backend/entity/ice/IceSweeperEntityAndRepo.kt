package org.n1.av2.backend.entity.ice

import org.n1.av2.backend.entity.site.enums.IceStrength
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository


/*
Cells is a string representation of the board, regardless of visibility or modifiers like flags.
mine: *
empty: .

For example:
.....**
.......
..*....
.......

Modifiers is a string representation of the modifiers:
. hidden
- revealed
f flag
? question mark

For example:
-----?f
-------
--.----
-------

 */

@Document
data class SweeperIceStatus(
    @Id val id: String,
    @Indexed val layerId: String,
    val strength: IceStrength,
    val cells: MutableList<String>,
    val modifiers: MutableList<String>,
    val hacked: Boolean,
)

@Repository
interface SweeperIceStatusRepo: CrudRepository<SweeperIceStatus, String> {
    fun findByLayerId(layerId: String): SweeperIceStatus?
}