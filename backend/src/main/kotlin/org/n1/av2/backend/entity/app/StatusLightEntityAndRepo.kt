package org.n1.av2.backend.entity.app

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Document
data class StatusLightApp (
    @Id val id: String,
    val layerId: String,
    val status: Boolean,
    val description: String,
    val textForRed: String,
    val textForGreen: String,
)

@Repository
interface StatusLightRepo : CrudRepository<StatusLightApp, String>