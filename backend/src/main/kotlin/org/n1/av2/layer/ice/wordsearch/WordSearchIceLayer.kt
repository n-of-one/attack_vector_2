package org.n1.av2.layer.ice.wordsearch

import org.n1.av2.layer.ice.common.IceLayer
import org.n1.av2.site.entity.enums.IceStrength
import org.n1.av2.site.entity.enums.LayerType

class WordSearchIceLayer(
    id: String,
    type: LayerType,
    level: Int,
    name: String,
    note: String,
    strength: IceStrength,
    hacked: Boolean,
    original: IceLayer? = null,
    ) : IceLayer(id, type, level, name, note, strength, hacked, original) {

    constructor(id: String, level: Int, defaultName: String) :
            this(id, LayerType.WORD_SEARCH_ICE, level, defaultName, "", IceStrength.AVERAGE, false)

    constructor(id: String, toClone: WordSearchIceLayer) :
            this(id, LayerType.WORD_SEARCH_ICE, toClone.level, toClone.name, toClone.note, toClone.strength, toClone.hacked)

}
