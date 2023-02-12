package org.n1.av2.backend.entity.site.layer

import org.n1.av2.backend.entity.site.enums.LayerType
import org.n1.av2.backend.model.SiteRep
import org.n1.av2.backend.model.ui.ValidationException

private const val TEXT = "text"

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

    @Suppress("UNUSED_PARAMETER")
    private fun validateText(siteRep: SiteRep) {
        if (this.text.isEmpty()) throw ValidationException("Hacked text cannot be empty.")
    }

    override fun validationMethods(): Collection<(siteRep: SiteRep) -> Unit> {
        return  listOf(::validateText )
    }

    override fun updateInternal(key: String, value: String): Boolean {
        when(key) {
            TEXT -> text = value
            else -> return super.updateInternal(key, value)
        }
        return true
    }

}