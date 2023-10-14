package org.n1.av2.backend.entity.site.enums

enum class LayerType(
        val ice: Boolean = false
) {

    OS,
    TEXT,
    TIMER_TRIGGER,
    KEYSTORE,
    TRIPWIRE,
    LOCK,
    STATUS_LIGHT,

    WORD_SEARCH_ICE(true),
    PASSWORD_ICE(true),
    TANGLE_ICE(true),
    NETWALK_ICE(true),
    TAR_ICE(true),



}