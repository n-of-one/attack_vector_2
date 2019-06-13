package org.n1.mainframe.backend.model.service

import com.fasterxml.jackson.annotation.JsonIgnore
import org.n1.mainframe.backend.model.site.SiteRep
import org.n1.mainframe.backend.model.site.enums.ServiceType
import org.n1.mainframe.backend.model.ui.ValidationException

private const val TEXT = "text"

class TextService(
        id: String,
        type: ServiceType,
        layer: Int,
        name: String,
        note: String,
        var text: String
) : Service(id, type, layer, name, note) {

    constructor(id: String, layer: Int, defaultName: String) :
            this(id, ServiceType.TEXT, layer, defaultName, "", "No data of value found.")

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