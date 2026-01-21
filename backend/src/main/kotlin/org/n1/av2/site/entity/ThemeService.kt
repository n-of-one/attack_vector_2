package org.n1.av2.site.entity

import org.n1.av2.site.entity.enums.LayerType
import org.springframework.stereotype.Service

@Service
class ThemeService {

    fun themeName(type: LayerType) = when (type) {
        LayerType.OS -> "OS"
        LayerType.TEXT -> "Database"
        LayerType.KEYSTORE -> "Keystore"
        LayerType.TRIPWIRE -> "Tripwire"
        LayerType.CORE -> "Core"
        LayerType.SCRIPT_INTERACTION -> "Process"
        LayerType.SCRIPT_CREDITS -> "Database"

        LayerType.STATUS_LIGHT -> "Status Light"
        LayerType.LOCK -> "Lock"

        LayerType.PASSWORD_ICE -> "Rahasy" // mystery in Hindi
        LayerType.TANGLE_ICE -> "Gaanth" // knot in Hindi
        LayerType.WORD_SEARCH_ICE -> "Jaal" // grid in Hindi
        LayerType.NETWALK_ICE -> "Sanrachana" // structure in Hindi
        LayerType.TAR_ICE -> "Tar" // after the substance tar in English
        LayerType.SWEEPER_ICE -> "Visphotak" // explosive in Hindi
    }

    fun iceSimpleName(type: LayerType): String = when (type) {
        LayerType.PASSWORD_ICE -> "static password"
        LayerType.TANGLE_ICE -> "tangle"
        LayerType.WORD_SEARCH_ICE -> "word search"
        LayerType.NETWALK_ICE -> "netwalk"
        LayerType.TAR_ICE -> "tar"
        LayerType.SWEEPER_ICE -> "minesweeper"
        else -> error("Not ICE: $type")
    }

}
