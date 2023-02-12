package org.n1.av2.backend.entity.site.enums

enum class NodeType(val ice: Boolean) {
    TRANSIT_1(false),
    TRANSIT_2(false),
    TRANSIT_3(false),
    TRANSIT_4(false),
    SYSCON(false),
    DATA_STORE(false),
    PASSCODE_STORE(false),
    RESOURCE_STORE(false),
    ICE_1(true),
    ICE_2(true),
    ICE_3(true),
    UNHACKABLE(true),
    MANUAL_1(true),
    MANUAL_2(true),
    MANUAL_3(true)
}