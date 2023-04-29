package org.n1.av2.backend.entity.site.layer

import org.n1.av2.backend.entity.site.enums.IceStrength
import org.n1.av2.backend.entity.site.enums.LayerType

class NetwalkIceLayer(
    id: String,
    type: LayerType,
    level: Int,
    name: String,
    note: String,
    strength: IceStrength

) : IceLayer(id, type, level, name, note, strength) {

    constructor(id: String, level: Int, defaultName: String) :
            this(id, LayerType.NETWALK_ICE, level, defaultName, "", IceStrength.AVERAGE)

}