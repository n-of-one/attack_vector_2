package org.n1.av2.backend.entity.app

import org.n1.av2.backend.entity.app.StatusLightApp
import org.n1.av2.backend.entity.app.StatusLightRepo
import org.n1.av2.backend.util.createId
import org.springframework.stereotype.Service
import kotlin.jvm.optionals.getOrElse

@Service
class StatusLightEntityService(
    private val statusLightRepo: StatusLightRepo
) {

    fun create(layerId: String, status: Boolean = false, description: String, textForRed: String, textForGreen: String): StatusLightApp {
        val id = createId("statusLight", statusLightRepo::findById)
        val statusLightApp = StatusLightApp(id, layerId, status, description, textForRed, textForGreen)
        return statusLightRepo.save(statusLightApp)
    }

    fun findById(id: String): StatusLightApp {
        return statusLightRepo.findById(id).getOrElse { error("No StatusLightEntity for id: ${id}") }
    }


    fun update(toUpdate: StatusLightApp)  {
        statusLightRepo.save(toUpdate)
    }


}