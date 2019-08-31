package org.n1.av2.backend.model.db.service

import org.n1.av2.backend.model.db.site.enums.ServiceType

private const val HACKED = "hacked"

abstract class IceService(
        id: String,
        type: ServiceType,
        layer: Int,
        name: String,
        note: String
) : Service(id, type, layer, name, note) {

    constructor(id: String, layer: Int, defaultName: String) :
            this(id, ServiceType.ICE_PASSWORD, layer, defaultName, "")

    @Suppress("UNUSED_PARAMETER")
    var ice: Boolean
    set(ignore) {
        // Needed setter because creating the object from Mongo always called the setter.
    }
    get() {
        return true
    }

}