package org.n1.av2.backend.model.db.layer

import org.n1.av2.backend.model.db.site.enums.LayerType

abstract class IceLayer(
        id: String,
        type: LayerType,
        level: Int,
        name: String,
        note: String
) : Layer(id, type, level, name, note) {

    constructor(id: String, level: Int, defaultName: String) :
            this(id, LayerType.ICE_PASSWORD, level, defaultName, "")

    @Suppress("UNUSED_PARAMETER")
    var ice: Boolean
    set(ignore) {
        // Needed setter because creating the object from Mongo always called the setter.
    }
    get() {
        return true
    }

}