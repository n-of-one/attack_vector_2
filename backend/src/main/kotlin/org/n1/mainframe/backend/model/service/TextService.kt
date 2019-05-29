package org.n1.mainframe.backend.model.service

import com.fasterxml.jackson.annotation.JsonIgnore
import org.n1.mainframe.backend.model.site.SiteRep
import org.n1.mainframe.backend.model.site.enums.ServiceType
import org.n1.mainframe.backend.model.ui.ValidationException

private const val KEY_TEXT = "text"

class TextService(
        id: String,
        type: ServiceType,
        layer: Int,
        data: MutableMap<String, String>
) : Service(id, type, layer, data) {

    constructor(id: String, layer: Int, defaultName: String) : this(id, ServiceType.TEXT, layer, HashMap()) {
        setDefaultName(defaultName)
    }


    val text: String
        @JsonIgnore
        get() {
            return data[KEY_TEXT] ?: ""
        }

    @Suppress("UNUSED_PARAMETER")
    private fun validateText(siteRep: SiteRep) {
        if (this.text.isEmpty()) throw ValidationException("Hacked text cannot be empty.")
    }

    override fun validationMethods(): Collection<(siteRep: SiteRep) -> Unit> {
        return  listOf(::validateText )
    }
}