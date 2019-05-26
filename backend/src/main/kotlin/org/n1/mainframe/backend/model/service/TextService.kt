package org.n1.mainframe.backend.model.service

import com.fasterxml.jackson.annotation.JsonIgnore
import org.n1.mainframe.backend.model.site.Service
import org.n1.mainframe.backend.model.site.SiteRep
import org.n1.mainframe.backend.model.site.enums.ServiceType
import org.n1.mainframe.backend.model.ui.ValidationException

private const val KEY_NAME = "name"
private const val KEY_TEXT = "text"

class TextService(
        id: String,
        layer: Int,
        type: ServiceType,
        data: MutableMap<String, String>
) : Service(id, type, layer, data) {

    constructor(id: String, layer: Int) : this(id, layer, ServiceType.TEXT, HashMap()) {
        data[KEY_NAME] = "Data vault"
        data[KEY_TEXT] = "No data of interest found."
    }

    val name: String
        @JsonIgnore
        get() {
            return data[KEY_NAME] ?: throw error("name cannot be empty for ${id}")
        }

    val text: String
        @JsonIgnore
        get() {
            return data[KEY_TEXT] ?: throw error("text cannot be empty for ${id}")
        }

    private fun validateName(siteRep: SiteRep) {
        if (this.name.isEmpty()) throw ValidationException("Name cannot be empty.")
    }

    private fun validateText(siteRep: SiteRep) {
        if (this.text.isEmpty()) throw ValidationException("Hacked text cannot be empty.")
    }

    override fun validationMethods(): Collection<(siteRep: SiteRep) -> Unit> {
        return  listOf(::validateName, ::validateText )
    }
}