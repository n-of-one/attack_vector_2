package org.n1.av2.layer.ice.common

import org.n1.av2.layer.Layer
import org.n1.av2.layer.ice.common.IceLayerFields.STRENGTH
import org.n1.av2.site.entity.enums.IceStrength
import org.n1.av2.site.entity.enums.LayerType

enum class IceLayerFields { STRENGTH }

abstract class IceLayer(
    id: String,
    type: LayerType,
    level: Int,
    name: String,
    note: String,
    var strength: IceStrength,
    var hacked: Boolean,

    // Ice layers can be changed by hackers. The original (if set) contains the layer as part of the site design.
    // If a site resets, the changed layer is replaced with the original and the value of original is set to null.
    val original: IceLayer? = null,
    ) : Layer(id, type, level, name, note) {

    @Suppress("unused")
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
