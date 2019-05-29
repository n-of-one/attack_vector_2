package org.n1.mainframe.backend.model.service

import com.fasterxml.jackson.annotation.JsonIgnore
import org.n1.mainframe.backend.model.site.SiteRep
import org.n1.mainframe.backend.model.site.enums.ServiceType
import org.n1.mainframe.backend.model.ui.ValidationException


private const val SERVICE_NAME = "name"
private const val NOTE = "note"

abstract class Service(
        val id: String,
        val type: ServiceType,
        var layer: Int,
        val data: MutableMap<String, String>
) {

    constructor(id: String, type: ServiceType, layer: Int, data: MutableMap<String, String>, defaultName: String) :
            this(id, type, layer, data) {
        data[SERVICE_NAME] = defaultName
    }

    fun setDefaultName(name: String) {
        data[SERVICE_NAME] = name
    }

    val name: String
        @JsonIgnore
        get() {
            return data[SERVICE_NAME] ?: ""
        }

    val note: String
        @JsonIgnore
        get() {
            return data[NOTE] ?: ""
        }

    @Suppress("UNUSED_PARAMETER")
    private fun validateName(siteRep: SiteRep) {
        if (this.name.isEmpty()) throw ValidationException("Service name cannot be empty.")
    }

    abstract fun validationMethods(): Collection<(siteRep: SiteRep) -> Unit>

    fun allValidationMethods(): Collection<(siteRep: SiteRep) -> Unit> {
        val methods = validationMethods().toMutableList()
        methods.add(::validateName)
        return methods
    }


}