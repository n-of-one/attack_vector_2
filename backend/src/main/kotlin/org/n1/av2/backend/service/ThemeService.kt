package org.n1.av2.backend.service

import org.n1.av2.backend.model.db.site.enums.LayerType
import org.springframework.stereotype.Service

@Service
class ThemeService {

    fun getDefaultName(serviceType: LayerType): String {
        return when(serviceType) {
            LayerType.OS -> "OS"
            LayerType.TEXT -> "Data vault"
            LayerType.ICE_PASSWORD -> "Aruna"
            LayerType.ICE_TANGLE -> "Reva"
            LayerType.NETWORK_SNIFFER -> "Network Sniffer"
//            else -> error("Unknown service type: ${serviceType}")
        }
    }
}