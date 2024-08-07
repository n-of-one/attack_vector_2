package org.n1.av2.layer.ice.sweeper

import org.n1.av2.layer.ice.common.IceLayer
import org.n1.av2.site.entity.enums.IceStrength
import org.n1.av2.site.entity.enums.LayerType

class SweeperIceLayer(
    id: String,
    type: LayerType,
    level: Int,
    name: String,
    note: String,
    strength: IceStrength,
    hacked: Boolean

) : IceLayer(id, type, level, name, note, strength, hacked) {

    constructor(id: String, level: Int, defaultName: String) :
            this(id, LayerType.SWEEPER_ICE, level, defaultName, "", IceStrength.AVERAGE, false)

    constructor(id: String, toClone: SweeperIceLayer) :
            this(id, LayerType.SWEEPER_ICE, toClone.level, toClone.name, toClone.note, toClone.strength, toClone.hacked)
}
