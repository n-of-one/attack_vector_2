package org.n1.av2.backend.service.service

import org.n1.av2.backend.model.db.service.Service
import org.n1.av2.backend.model.db.site.enums.ServiceType
import org.n1.av2.backend.service.StompService
import org.n1.av2.backend.service.run.ServiceStatusService
import org.n1.av2.backend.service.service.ice.password.ServiceIcePassword

@org.springframework.stereotype.Service
class ServiceIceGeneric(
        private val serviceStatusService: ServiceStatusService,
        private val stompService: StompService,
        private val serviceIcePassword: ServiceIcePassword) {

    fun hack(service: Service, runId: String) {
        val holder = serviceStatusService.getOrCreate(service.id, runId)
        if (holder.hacked) {
            stompService.terminalReceive("[info]not required[/] Ice already hacked.")
            return
        }

        when (service.type) {
            ServiceType.ICE_PASSWORD -> serviceIcePassword.hack(service, runId)
            else -> error("unsupported hack type: ${service.type}")
        }
    }




}