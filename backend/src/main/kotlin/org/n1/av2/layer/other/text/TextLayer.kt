package org.n1.av2.layer.other.text

import org.n1.av2.editor.SiteRep
import org.n1.av2.editor.SiteValidationException
import org.n1.av2.layer.Layer
import org.n1.av2.site.entity.enums.LayerType

class TextLayer(
    id: String,
    @Suppress("unused") type: LayerType, // exists for consistency and to allow creation from db
    level: Int,
    name: String,
    note: String,
    var text: String
) : Layer(id, LayerType.TEXT, level, name, note) {

    constructor(id: String, level: Int, name: String, note: String, text: String) :
        this(id, LayerType.TEXT, level, name, note, text)

    constructor(id: String, level: Int, defaultName: String) :
        this(id, LayerType.TEXT, level, defaultName, "", "No data of value found.")

    constructor(id: String, toClone: TextLayer) :
        this(id, LayerType.TEXT, toClone.level, toClone.name, toClone.note, toClone.text)


    private fun validateText(siteRep: SiteRep) {
        if (this.text.isEmpty()) throw SiteValidationException("Hacked text cannot be empty.")
    }

    override fun validationMethods(): Collection<(siteRep: SiteRep) -> Unit> {
        return listOf(::validateText)
    }

    override fun updateInternal(key: String, value: String): Boolean {
        when (key) {
            "TEXT" -> text = value
            else -> return super.updateInternal(key, value)
        }
        return true
    }
}
