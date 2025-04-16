package org.n1.av2.layer.other.keystore

import org.n1.av2.editor.SiteRep
import org.n1.av2.editor.SiteValidationException
import org.n1.av2.layer.Layer
import org.n1.av2.site.entity.enums.LayerType
import org.n1.av2.site.entity.findLayerById

class KeyStoreLayer(
    id: String,
    @Suppress("unused") type: LayerType, // exists for consistency and to allow creation from db
    level: Int,
    name: String,
    note: String,
    var iceLayerId: String?
) : Layer(id, LayerType.KEYSTORE, level, name, note) {

    constructor(id: String, level: Int, name: String, note: String, iceLayerId: String?) :
        this(id, LayerType.KEYSTORE, level, name, note, iceLayerId)

    constructor(id: String, level: Int, defaultName: String) :
        this(id, LayerType.KEYSTORE, level, defaultName, "", null)

    constructor(id: String, toClone: KeyStoreLayer) :
        this(id, LayerType.KEYSTORE, toClone.level, toClone.name, toClone.note, toClone.iceLayerId)

    private fun validateText(siteRep: SiteRep) {
        if (this.iceLayerId == null) throw SiteValidationException("Choose ICE that keystore provides password for.")
        findLayerById(this.iceLayerId!!, siteRep.nodes) ?: throw SiteValidationException("Choose ICE that keystore provides password for.")
    }

    override fun validationMethods(): Collection<(siteRep: SiteRep) -> Unit> {
        return listOf(::validateText)
    }

    override fun updateInternal(key: String, value: String): Boolean {
        when (key) {
            "ICE_LAYER_ID" -> iceLayerId = value
            else -> return super.updateInternal(key, value)
        }
        return true
    }

}
