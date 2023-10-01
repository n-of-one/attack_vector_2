package org.n1.av2.backend.entity.site.layer.other

import org.n1.av2.backend.entity.site.enums.LayerType
import org.n1.av2.backend.entity.site.findLayerById
import org.n1.av2.backend.entity.site.layer.Layer
import org.n1.av2.backend.model.SiteRep
import org.n1.av2.backend.model.ui.ValidationException

private const val ICE_LAYER_ID = "iceLayerId"

class KeyStoreLayer(
    id: String,
    type: LayerType,
    level: Int,
    name: String,
    note: String,
    var iceLayerId: String?
) : Layer(id, type, level, name, note) {


    constructor(id: String, level: Int, defaultName: String) :
            this(id, LayerType.KEYSTORE, level, defaultName, "", null )

    private fun validateText(siteRep: SiteRep) {
        if (this.iceLayerId == null) throw ValidationException("Choose ICE that keystore provides password for.");
         findLayerById(this.iceLayerId!!, siteRep.nodes) ?: throw ValidationException("Choose ICE that keystore provides password for.")
    }

    override fun validationMethods(): Collection<(siteRep: SiteRep) -> Unit> {
        return  listOf(::validateText )
    }

    override fun updateInternal(key: String, value: String): Boolean {
        when(key) {
            ICE_LAYER_ID -> iceLayerId = value
            else -> return super.updateInternal(key, value)
        }
        return true
    }

}