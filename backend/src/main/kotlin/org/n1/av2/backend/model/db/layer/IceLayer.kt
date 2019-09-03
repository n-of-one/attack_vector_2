package org.n1.av2.backend.model.db.layer

import org.n1.av2.backend.model.db.site.enums.IceStrength
import org.n1.av2.backend.model.db.site.enums.LayerType

private const val STRENGTH = "strength"

abstract class IceLayer(
        id: String,
        type: LayerType,
        level: Int,
        name: String,
        note: String,
        var strength: IceStrength
) : Layer(id, type, level, name, note) {

    constructor(id: String, level: Int, defaultName: String, strength: IceStrength) :
            this(id, LayerType.ICE_PASSWORD, level, defaultName, "", strength)

    @Suppress("UNUSED_PARAMETER")
    var ice: Boolean
    set(ignore) {
        // Needed setter because creating the object from Mongo always called the setter.
    }
    get() {
        return true
    }

    override fun updateInternal(key: String, value: String): Boolean {
        when(key) {
            STRENGTH -> strength = IceStrength.valueOf(value)
            else -> return super.updateInternal(key, value)
        }
        return true
    }

}