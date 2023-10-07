package org.n1.av2.backend.entity.service

import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Document
data class IcePassword(
    val id: String,
    val iceId: String,
    val password: String
)

@Repository
interface IcePasswordRepository: CrudRepository<IcePassword, String> {
    fun findByIceId(iceId: String): IcePassword?
}