package org.n1.av2.backend.entity.site.layer.ice

import org.n1.av2.backend.entity.site.enums.IceStrength
import org.n1.av2.backend.entity.site.enums.LayerType
import org.n1.av2.backend.entity.site.layer.Layer
import org.n1.av2.backend.entity.site.layer.ice.IceLayerFields.STRENGTH

enum class IceLayerFields { STRENGTH }

sealed class IceLayer(
    id: String,
    type: LayerType,
    level: Int,
    name: String,
    note: String,
    var strength: IceStrength,
    var hacked: Boolean,
    ) : Layer(id, type, level, name, note) {

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
            STRENGTH.name -> strength = IceStrength.valueOf(value)
            else -> return super.updateInternal(key, value)
        }
        return true
    }
}
