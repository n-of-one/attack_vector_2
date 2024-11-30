package org.n1.av2.script

import org.n1.av2.platform.util.toDuration
import org.n1.av2.platform.util.toHumanTime
import org.n1.av2.script.effect.ScriptEffect
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.repository.CrudRepository
import java.math.BigDecimal
import java.time.ZonedDateTime




@Document
data class Script(
    @Id val id: String,
    val typeId: String,
    val ownerUserId: String,
    val expiry: ZonedDateTime,
    @Indexed(unique = true) val code: String,
    @Indexed(unique = false) val accessId: String?, // the script access that was used to create this script. If this is null,
    // it means the script was bought from the shop or given to the user by a GM.
    val used: Boolean = false,
    val loadedByUserId: String? = null,
    val deleted: Boolean = false,
)

interface ScriptRepository : CrudRepository<Script, String> {
    fun findByCode(code: String): Script?
    fun findByOwnerUserId(userId: String): List<Script>
    fun findByAccessId(accessId: String?): List<Script>
}


@Document
data class ScriptAccess(
    @Id val id: String,
    val ownerUserId: String,
    val typeId: String,
    val receiveForFree: Int,
    val price: BigDecimal?,
)

interface ScriptAccessRepository : CrudRepository<ScriptAccess, String> {
    fun findByOwnerUserId(userId: String): List<ScriptAccess>
}
