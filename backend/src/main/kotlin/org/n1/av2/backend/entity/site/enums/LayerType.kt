package org.n1.av2.backend.entity.site.enums

enum class LayerType(
        val ice: Boolean = false
) {

    OS,
    TEXT,
//    IMAGE,
//    LINK,
    TIMER_TRIGGER,
//    SYSCON,
//    CODE,
//    RESOURCE,
//    SCAN_BLOCKER,

    LOCK (false),
    STATUS_LIGHT(false),

    WORD_SEARCH_ICE(true),
//    ICE_MAGIC_EYE,
    PASSWORD_ICE(true),
    TANGLE_ICE(true),
    NETWALK_ICE(true),
//    ICE_MANUAL,
    SLOW_ICE(true),


}