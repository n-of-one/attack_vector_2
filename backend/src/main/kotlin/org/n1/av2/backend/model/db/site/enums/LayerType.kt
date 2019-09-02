package org.n1.av2.backend.model.db.site.enums

enum class LayerType(
        val ice: Boolean = false
) {

    OS,
    TEXT,
//    IMAGE,
//    LINK,
//    TIME,
//    SYSCON,
//    CODE,
//    RESOURCE,
//    SCAN_BLOCKER,
//    ICE_WORD_SEARCH,
//    ICE_MAGIC_EYE,
    ICE_PASSWORD(true),
    ICE_TANGLE(true),
//    ICE_MANUAL,
//    ICE_UNHACKABLE



}