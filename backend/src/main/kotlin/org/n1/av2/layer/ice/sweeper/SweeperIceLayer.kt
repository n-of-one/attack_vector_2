package org.n1.av2.layer.ice.sweeper

import org.n1.av2.layer.ice.common.IceLayer
import org.n1.av2.site.entity.enums.IceStrength
import org.n1.av2.site.entity.enums.LayerType

class SweeperIceLayer(
    id: String,
    @Suppress("unused") type: LayerType, // exists for consistency and to allow creation from db
    level: Int,
    name: String,
    note: String,
    strength: IceStrength,
    hacked: Boolean,
    original: IceLayer? = null,
) : IceLayer(id, LayerType.SWEEPER_ICE, level, name, note, strength, hacked, original) {

    constructor(id: String, level: Int, name: String, note: String, strength: IceStrength, hacked: Boolean, original: IceLayer?) :
        this(id, LayerType.SWEEPER_ICE, level, name, note, strength, hacked, original)

    constructor(id: String, level: Int, defaultName: String) :
        this(id, LayerType.SWEEPER_ICE, level, defaultName, "", IceStrength.AVERAGE, false)

    constructor(id: String, toClone: SweeperIceLayer) :
        this(id, LayerType.SWEEPER_ICE, toClone.level, toClone.name, toClone.note, toClone.strength, toClone.hacked, toClone.original)
}
