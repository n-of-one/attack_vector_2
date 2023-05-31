package org.n1.av2.backend.service.layerhacking.app.status_light

import org.n1.av2.backend.entity.app.StatusLightEntity
import org.n1.av2.backend.entity.app.StatusLightRepo
import org.n1.av2.backend.util.createId
import org.springframework.stereotype.Service
import kotlin.jvm.optionals.getOrElse

@Service
class StatusLightEntityService(
    private val statusLightRepo: StatusLightRepo
) {

    fun create(status: Boolean = false, description: String, textForRed: String, textForGreen: String): StatusLightEntity {
        val id = createId("status_light", statusLightRepo::findById)
        val statusLightEntity = StatusLightEntity(id, status, description, textForRed, textForGreen)
        return statusLightRepo.save(statusLightEntity)
    }

    fun findById(id: String): StatusLightEntity {
        return statusLightRepo.findById(id).getOrElse { error("No StatusLightEntity for id: ${id}") }
    }


    fun update(toUpdate: StatusLightEntity)  {
        statusLightRepo.save(toUpdate)
    }


}