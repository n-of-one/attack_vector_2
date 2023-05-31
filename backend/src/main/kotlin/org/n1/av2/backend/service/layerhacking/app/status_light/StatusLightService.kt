package org.n1.av2.backend.service.layerhacking.app.status_light

import org.n1.av2.backend.entity.app.StatusLightEntity
import org.n1.av2.backend.model.ui.ServerActions
import org.n1.av2.backend.service.StompService
import org.springframework.stereotype.Service

@Service
class StatusLightService(
    private val statusLightEntityService: StatusLightEntityService,
    private val stompService: StompService,
) {

    class StatusLightMessage(val status: Boolean, val description: String, val textForRed: String, val textForGreen: String)
    fun enter(appId: String) {
        val statusLightEntity = statusLightEntityService.findById(appId)
        sendUpdate(statusLightEntity)
    }

    fun setValue(appId: String, newValue: Boolean) {
        val statusLightEntity = statusLightEntityService.findById(appId)
        val updatedEntity = statusLightEntity.copy(status = newValue)
        statusLightEntityService.update(updatedEntity)
        sendUpdate(updatedEntity)
    }

    fun sendUpdate(statusLightEntity: StatusLightEntity) {
        val message = StatusLightMessage(statusLightEntity.status, statusLightEntity.description, statusLightEntity.textForRed, statusLightEntity.textForGreen)
        stompService.toApp(statusLightEntity.id, ServerActions.SERVER_STATUS_LIGHT_UPDATE, message)

    }
}