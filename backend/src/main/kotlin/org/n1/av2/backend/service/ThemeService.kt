package org.n1.av2.backend.service

import org.n1.av2.backend.entity.site.enums.LayerType
import org.springframework.stereotype.Service

@Service
class ThemeService {

    fun getDefaultName(serviceType: LayerType): String {
        return when(serviceType) {
            LayerType.OS -> "OS"
            LayerType.TEXT -> "Data vault"
            LayerType.TIMER_TRIGGER -> "Network Sniffer"
            LayerType.KEYSTORE -> "Keystore"
            LayerType.TRIPWIRE -> "Tripwire"

            LayerType.STATUS_LIGHT -> "Status Light"
            LayerType.LOCK -> "Lock"

            LayerType.PASSWORD_ICE -> "Rahasy" // mystery in Hindi
            LayerType.TANGLE_ICE -> "Gaanth" // knot in Hindi
            LayerType.WORD_SEARCH_ICE -> "Jaal" // grid in Hindi
            LayerType.NETWALK_ICE -> "Sanrachana" // structure in Hindi
            LayerType.TAR_ICE -> "Tar" // In Hindi this would be "taar" but in this case it's nice that it's clear in English
        }
    }
}