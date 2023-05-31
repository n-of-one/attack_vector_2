package org.n1.av2.backend.entity.app

import org.springframework.data.annotation.Id
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

data class StatusLightEntity (
    @Id val id: String,
    val status: Boolean,
    val description: String,
    val textForRed: String,
    val textForGreen: String,
)

@Repository
interface StatusLightRepo : CrudRepository<StatusLightEntity, String>