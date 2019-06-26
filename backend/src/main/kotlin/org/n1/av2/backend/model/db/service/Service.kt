package org.n1.av2.backend.model.db.service

import org.n1.av2.backend.model.SiteRep
import org.n1.av2.backend.model.db.site.enums.ServiceType
import org.n1.av2.backend.model.ui.ValidationException


private const val NAME = "name"
private const val NOTE = "note"

abstract class Service(
        val id: String,
        val type: ServiceType,
        var layer: Int,
        var name: String,
        var note: String,
        var hacked: Boolean
) {

    constructor(id: String, type: ServiceType, layer: Int, defaultName: String) :
            this(id, type, layer, defaultName, "", false)


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

    fun update(key: String, value: String) {
        val keyFound = updateInternal(key, value)
        if (!keyFound) error("Unknown key: ${key} for service type: ${type}")
    }

    open fun updateInternal(key: String, value: String): Boolean {
        when(key) {
            NAME -> name = value
            NOTE -> note = value
            else -> return false
        }
        return true
    }

}