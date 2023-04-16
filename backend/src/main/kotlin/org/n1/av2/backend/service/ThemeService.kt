package org.n1.av2.backend.service

import org.n1.av2.backend.entity.site.enums.LayerType
import org.springframework.stereotype.Service

@Service
class ThemeService {

    fun getDefaultName(serviceType: LayerType): String {
        return when(serviceType) {
            LayerType.OS -> "OS"
            LayerType.TEXT -> "Data vault"
            LayerType.PASSWORD_ICE -> "Aruna"
            LayerType.TANGLE_ICE -> "Reva"
            LayerType.WORD_SEARCH_ICE -> "Pumer"
            LayerType.NETWALK_ICE -> "Dahana"
            LayerType.TIMER_TRIGGER -> "Network Sniffer"
//            else -> error("Unknown service type: ${serviceType}")
        }
    }
}