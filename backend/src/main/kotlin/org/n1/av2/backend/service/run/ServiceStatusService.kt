package org.n1.av2.backend.service.run

import org.n1.av2.backend.model.db.run.ServiceStatus
import org.n1.av2.backend.model.db.site.Node
import org.n1.av2.backend.repo.ServiceStatusRepo
import org.n1.av2.backend.util.createId
import org.springframework.stereotype.Service
import java.util.*

@Service
class ServiceStatusService(
        private val serviceStatusRepo: ServiceStatusRepo
) {

    fun getOrCreate(serviceId: String, runId: String): ServiceStatus {
        return serviceStatusRepo.findByServiceIdAndRunId(serviceId, runId) ?: createServiceStatus(serviceId, runId)
    }

    private fun createServiceStatus(serviceId: String, runId: String): ServiceStatus {
        val id = createId("serviceStatus-")
        val status = ServiceStatus(id, serviceId, runId, false, LinkedList())
        serviceStatusRepo.save(status)
        return status
    }

    fun save(serviceStatus: ServiceStatus) {
        serviceStatusRepo.save(serviceStatus)
    }

    fun getServicesStatus(runId: String, iceServiceIds: List<String>): List<ServiceStatus> {
        return serviceStatusRepo.findByRunIdAndServiceIdIn(runId, iceServiceIds)
    }
}