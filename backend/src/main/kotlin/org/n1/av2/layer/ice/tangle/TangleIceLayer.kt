package org.n1.av2.layer.ice.tangle

import org.n1.av2.layer.ice.common.IceLayer
import org.n1.av2.site.entity.enums.IceStrength
import org.n1.av2.site.entity.enums.LayerType

class TangleIceLayer(
    id: String,
    type: LayerType,
    level: Int,
    name: String,
    note: String,
    strength: IceStrength,
    hacked: Boolean,
    var clusters: Int?,

    ) : IceLayer(id, type, level, name, note, strength, hacked) {


    constructor(id: String, level: Int, defaultName: String) :
            this(id, LayerType.TANGLE_ICE, level, defaultName, "", IceStrength.AVERAGE, false, 1)

    constructor(id: String, toClone: TangleIceLayer) :
            this(id, LayerType.TANGLE_ICE, toClone.level, toClone.name, toClone.note, toClone.strength, toClone.hacked, 1)

    override fun updateInternal(key: String, value: String): Boolean {
        if (key == "CLUSTERS") {
            clusters = value.toInt()
            return true
        }
        return super.updateInternal(key, value)
    }
}
