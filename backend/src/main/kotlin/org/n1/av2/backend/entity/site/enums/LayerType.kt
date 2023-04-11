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
    WORD_SEARCH_ICE(true),
//    ICE_MAGIC_EYE,
    PASSWORD_ICE(true),
    TANGLE_ICE(true),
//    ICE_MANUAL,
//    ICE_UNHACKABLE



}