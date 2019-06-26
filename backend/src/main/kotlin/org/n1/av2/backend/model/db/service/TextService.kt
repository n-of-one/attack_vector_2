package org.n1.av2.backend.model.db.service

import org.n1.av2.backend.model.SiteRep
import org.n1.av2.backend.model.db.site.enums.ServiceType
import org.n1.av2.backend.model.ui.ValidationException

private const val TEXT = "text"

class TextService(
        id: String,
        type: ServiceType,
        layer: Int,
        name: String,
        note: String,
        var text: String,
        hacked: Boolean
) : Service(id, type, layer, name, note, hacked) {

    constructor(id: String, layer: Int, defaultName: String) :
            this(id, ServiceType.TEXT, layer, defaultName, "", "No data of value found.", false)

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