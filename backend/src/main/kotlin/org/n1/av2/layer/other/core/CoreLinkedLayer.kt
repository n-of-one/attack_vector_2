package org.n1.av2.layer.other.core

/**
 * A layer that can be reset by hacking a core, possibly a core in another site.
 */
interface CoreLinkedLayer {
    var coreLayerId: String?
    var coreSiteId: String?

    // These fields are used during export to make the export portable (i.e. not rely on DB id references).
    var coreSiteName: String?
    var coreNetworkId: String?
    var coreLayerLevel: Int?
}

const val CORE_LAYER_ID = "CORE_LAYER_ID"
const val CORE_SITE_ID = "CORE_SITE_ID"

/**
 * @return true if the key was a core link key.
 */
fun CoreLinkedLayer.updateCoreLink(key: String, value: String): Boolean {
    when (key) {
        CORE_LAYER_ID -> coreLayerId = value.ifEmpty { null }
        CORE_SITE_ID -> {
            coreSiteId = value
            coreLayerId = null // site changed, so current core layer is not valid in new site
        }
        else -> return false
    }
    return true
}
