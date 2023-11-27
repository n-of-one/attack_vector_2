package org.n1.av2.backend.entity.site.layer

import org.n1.av2.backend.entity.site.enums.LayerType
import org.n1.av2.backend.model.SiteRep
import org.n1.av2.backend.model.ui.ValidationException


private const val NAME = "name"
private const val NOTE = "note"

open class Layer(
    val id: String,
    val type: LayerType,
    var level: Int,
    var name: String,
    var note: String
) {

    constructor(id: String, type: LayerType, level: Int, defaultName: String) :
            this(id, type, level, defaultName, "")


    @Suppress("UNUSED_PARAMETER")
    private fun validateName(siteRep: SiteRep) {
        if (this.name.isEmpty()) throw ValidationException("Service name cannot be empty.")
    }

    open fun validationMethods(): Collection<(siteRep: SiteRep) -> Unit> {
        return emptyList()
    }

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