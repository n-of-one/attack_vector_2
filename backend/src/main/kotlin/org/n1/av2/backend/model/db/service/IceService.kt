package org.n1.av2.backend.model.db.service

import org.n1.av2.backend.model.db.site.enums.ServiceType

private const val HACKED = "hacked"

abstract class IceService(
        id: String,
        type: ServiceType,
        layer: Int,
        name: String,
        note: String,
        hacked: Boolean
) : Service(id, type, layer, name, note, hacked) {

    constructor(id: String, layer: Int, defaultName: String) :
            this(id, ServiceType.ICE_PASSWORD, layer, defaultName, "", false)

    @Suppress("UNUSED_PARAMETER")
    var ice: Boolean
    set(ignore) {
        // Needed setter because creating the object from Mongo always called the setter.
    }
    get() {
        return true
    }



    override fun updateInternal(key: String, value: String): Boolean {
        when (key) {
            HACKED -> hacked = value.toBoolean()
            else -> return super.updateInternal(key, value)
        }
        return true
    }

}