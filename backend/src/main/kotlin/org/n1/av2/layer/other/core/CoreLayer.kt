package org.n1.av2.layer.other.core

import org.n1.av2.layer.Layer
import org.n1.av2.site.entity.enums.LayerType

class CoreLayer(
    id: String,
    @Suppress("unused") type: LayerType, // exists for consistency and to allow creation from db
    level: Int,
    name: String,
    note: String,
    var revealNetwork: Boolean,
) : Layer(id, LayerType.CORE, level, name, note) {

    constructor(id: String, level: Int, name: String, note: String, revealNetwork: Boolean) :
        this(id, LayerType.CORE, level, name, note, revealNetwork)

    constructor(id: String, level: Int, defaultName: String) :
            this(id, LayerType.CORE, level, defaultName, "", false)

    constructor(id: String, toClone: CoreLayer) :
            this(id, LayerType.CORE, toClone.level, toClone.name, toClone.note, toClone.revealNetwork)

    override fun updateInternal(key: String, value: String): Boolean {
        when(key) {
            "REVEAL_NETWORK" -> revealNetwork = value.toBoolean()
            else -> return super.updateInternal(key, value)
        }
        return true
    }
}
