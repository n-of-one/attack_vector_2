package org.n1.av2.backend.entity.site.layer.ice

import org.n1.av2.backend.entity.site.enums.IceStrength
import org.n1.av2.backend.entity.site.enums.LayerType

class WordSearchIceLayer(
    id: String,
    type: LayerType,
    level: Int,
    name: String,
    note: String,
    strength: IceStrength,
    hacked: Boolean,

) : IceLayer(id, type, level, name, note, strength, hacked) {

    constructor(id: String, level: Int, defaultName: String) :
            this(id, LayerType.WORD_SEARCH_ICE, level, defaultName, "", IceStrength.AVERAGE, false)

    constructor(id: String, toClone: WordSearchIceLayer) :
            this(id, LayerType.WORD_SEARCH_ICE, toClone.level, toClone.name, toClone.note, toClone.strength, toClone.hacked)

}
