package org.n1.mainframe.backend.model.service

import org.n1.mainframe.backend.model.site.enums.ServiceType

private const val HACKED = "hacked"

abstract class IceService(
        id: String,
        type: ServiceType,
        layer: Int,
        name: String,
        note: String,
        var hacked: Boolean
) : Service(id, type, layer, name, note) {

    constructor(id: String, layer: Int, defaultName: String) :
            this(id, ServiceType.ICE_PASSWORD, layer, defaultName, "", false)

    /** For the benefit of the JS client */
    val ice = true

    override fun updateInternal(key: String, value: String): Boolean {
        when(key) {
            HACKED -> hacked = value.toBoolean()
            else -> return super.updateInternal(key, value)
        }
        return true
    }

}