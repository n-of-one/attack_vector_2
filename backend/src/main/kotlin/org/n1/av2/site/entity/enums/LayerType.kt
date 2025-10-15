package org.n1.av2.site.entity.enums

private const val GM_ONLY = true
private const val ICE = true
private const val NOT_ICE = false

enum class LayerType(
    val ice: Boolean = false,
    val gmOnly: Boolean = false,
) {

    OS,
    TEXT,
    KEYSTORE,
    TRIPWIRE,
    LOCK,
    STATUS_LIGHT,
    CORE,
    SCRIPT_INTERACTION,
    SCRIPT_CREDITS(NOT_ICE, GM_ONLY),

    WORD_SEARCH_ICE(ICE),
    PASSWORD_ICE(ICE),
    TANGLE_ICE(ICE),
    NETWALK_ICE(ICE),
    TAR_ICE(ICE),
    SWEEPER_ICE(ICE);

    companion object {
        fun valueOfOrNull(value: String): LayerType? {
            return entries.firstOrNull { it.name == value }
        }
    }
}
