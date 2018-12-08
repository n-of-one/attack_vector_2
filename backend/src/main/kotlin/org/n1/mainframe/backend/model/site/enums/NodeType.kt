package org.n1.mainframe.backend.model.site.enums

enum class NodeType(val ice: Boolean) {
    TRANSIT_1(false),
    TRANSIT_2(false),
    TRANSIT_3(false),
    TRANSIT_4(false),
    SYSCON(false),
    DATA_STORE(false),
    PASSCODE_STORE(false),
    RESOURCE_STORE(false),
    WORD_SEARCH(true),
    MAGIC_EYE(true),
    PASSWORD_GUESS(true),
    UNHACKABLE(true),
    MANUAL_1(true),
    MANUAL_2(true),
    MANUAL_3(true)
}