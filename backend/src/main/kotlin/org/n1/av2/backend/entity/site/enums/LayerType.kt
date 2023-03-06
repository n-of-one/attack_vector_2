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
//    ICE_WORD_SEARCH,
//    ICE_MAGIC_EYE,
    PASSWORD_ICE(true),
    TANGLE_ICE(true),
//    ICE_MANUAL,
//    ICE_UNHACKABLE



}