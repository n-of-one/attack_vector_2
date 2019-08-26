package org.n1.av2.backend.service.service

import org.n1.av2.backend.model.db.run.ServiceStatus
import org.n1.av2.backend.model.db.service.Service
import org.n1.av2.backend.model.db.site.enums.ServiceType
import org.n1.av2.backend.repo.ServiceStatusRepo
import org.n1.av2.backend.service.StompService
import org.n1.av2.backend.service.service.ice.password.ServiceIcePassword
import org.n1.av2.backend.util.createId
import java.util.*

@org.springframework.stereotype.Service
class ServiceIceGeneric(
        private val serviceStatusRepo: ServiceStatusRepo,
        private val stompService: StompService,
        private val serviceIcePassword: ServiceIcePassword) {

    fun hack(service: Service, runId: String) {
        val holder = getOrCreateServiceStatus(service.id, runId)
        if (holder.hacked) {
            stompService.terminalReceive("[info]not required[/] Ice already hacked.")
            return
        }

        when (service.type) {
            ServiceType.ICE_PASSWORD -> serviceIcePassword.hack(service, runId)
            else -> error("unsupported hack type: ${service.type}")
        }

    }


    private fun getOrCreateServiceStatus(serviceId: String, runId: String): ServiceStatus {
        return serviceStatusRepo.findByServiceIdAndRunId(serviceId, runId) ?: createServiceStatus(serviceId, runId)
    }

    private fun createServiceStatus(serviceId: String, runId: String): ServiceStatus {
        val id = createId("serviceStatus-")
        val status = ServiceStatus(id, serviceId, runId, false, LinkedList())
        serviceStatusRepo.save(status)
        return status
    }

}