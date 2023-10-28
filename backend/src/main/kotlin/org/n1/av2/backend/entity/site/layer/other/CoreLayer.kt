package org.n1.av2.backend.entity.site.layer.other

import org.n1.av2.backend.entity.site.enums.LayerType
import org.n1.av2.backend.entity.site.layer.Layer

class CoreLayer(
    id: String,
    type: LayerType,
    level: Int,
    name: String,
    note: String,
    var revealNetwork: Boolean,
) : Layer(id, type, level, name, note) {

    constructor(id: String, level: Int, defaultName: String) :
            this(id, LayerType.CORE, level, defaultName, "", false)

    override fun updateInternal(key: String, value: String): Boolean {
        when(key) {
            "REVEAL_NETWORK" -> revealNetwork = value.toBoolean()
            else -> return super.updateInternal(key, value)
        }
        return true
    }
}