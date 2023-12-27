package org.n1.av2.backend.entity.site.layer.other

import org.n1.av2.backend.entity.site.enums.LayerType
import org.n1.av2.backend.entity.site.layer.Layer
import org.n1.av2.backend.model.SiteRep
import org.n1.av2.backend.service.site.SiteValidationException

class TextLayer(
    id: String,
    type: LayerType,
    level: Int,
    name: String,
    note: String,
    var text: String
) : Layer(id, type, level, name, note) {

    constructor(id: String, level: Int, defaultName: String) :
            this(id, LayerType.TEXT, level, defaultName, "", "No data of value found.")

    private fun validateText(siteRep: SiteRep) {
        if (this.text.isEmpty()) throw SiteValidationException("Hacked text cannot be empty.")
    }

    override fun validationMethods(): Collection<(siteRep: SiteRep) -> Unit> {
        return  listOf(::validateText )
    }

    override fun updateInternal(key: String, value: String): Boolean {
        when(key) {
            "TEXT" -> text = value
            else -> return super.updateInternal(key, value)
        }
        return true
    }
}
