package org.n1.av2.backend.model.db.layer

import org.n1.av2.backend.model.SiteRep
import org.n1.av2.backend.model.db.site.enums.IceStrength
import org.n1.av2.backend.model.db.site.enums.LayerType

class IceTangleLayer(
        id: String,
        type: LayerType,
        level: Int,
        name: String,
        note: String,
        strength: IceStrength

) : IceLayer(id, type, level, name, note, strength) {

    constructor(id: String, level: Int, defaultName: String) :
            this(id, LayerType.ICE_TANGLE, level, defaultName, "", IceStrength.AVERAGE)

    override fun validationMethods(): Collection<(siteRep: SiteRep) -> Unit> {
        return emptyList()
    }

    override fun updateInternal(key: String, value: String): Boolean {
        return super.updateInternal(key, value)
    }

}