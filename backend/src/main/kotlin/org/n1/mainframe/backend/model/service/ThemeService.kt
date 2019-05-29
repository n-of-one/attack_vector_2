package org.n1.mainframe.backend.model.service

import org.n1.mainframe.backend.model.site.enums.ServiceType
import org.springframework.stereotype.Service

@Service
class ThemeService {

    fun getDefaultName(serviceType: ServiceType): String {
        return when(serviceType) {
            ServiceType.OS -> "OS"
            ServiceType.TEXT -> "Data vault"
            ServiceType.ICE_PASSWORD -> "Aruna"
            else -> error("Unknown service type: ${serviceType}")
        }
    }
}