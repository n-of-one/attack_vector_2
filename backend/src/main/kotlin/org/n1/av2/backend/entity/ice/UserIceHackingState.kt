package org.n1.av2.backend.entity.ice

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Document
data class UserIceHackingState(
    @Id val userId: String,
    val connectionId: String,
    val iceId: String?,
)

@Repository
interface UserIceHackingStateRepository: CrudRepository<UserIceHackingState, String> {
    fun findByIceId(iceId: String): List<UserIceHackingState>

}


