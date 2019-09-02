package org.n1.av2.backend.model.db.layer

import org.n1.av2.backend.model.SiteRep
import org.n1.av2.backend.model.db.site.enums.IceStrength
import org.n1.av2.backend.model.db.site.enums.LayerType
import org.n1.av2.backend.model.ui.ValidationException

private const val STRENGTH = "strength"

class IceTangleLayer(
        id: String,
        type: LayerType,
        level: Int,
        name: String,
        note: String,
        var strength: IceStrength

) : IceLayer(id, type, level, name, note) {

    constructor(id: String, level: Int, defaultName: String) :
            this(id, LayerType.ICE_TANGLE, level, defaultName, "", IceStrength.AVERAGE)


    override fun validationMethods(): Collection<(siteRep: SiteRep) -> Unit> {
        return emptyList()
    }

    override fun updateInternal(key: String, value: String): Boolean {
        when(key) {
            STRENGTH -> strength = IceStrength.valueOf(value)
            else -> return super.updateInternal(key, value)
        }
        return true
    }

}