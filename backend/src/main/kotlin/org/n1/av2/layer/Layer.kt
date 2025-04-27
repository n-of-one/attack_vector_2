package org.n1.av2.layer

import org.n1.av2.editor.SiteValidationException
import org.n1.av2.editor.ValidationContext
import org.n1.av2.site.entity.enums.LayerType


const val NAME = "name"
const val NOTE = "note"

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
    private fun validateName(validationContext: ValidationContext) {
        if (this.name.isEmpty()) throw SiteValidationException("Service name cannot be empty.")
    }

    open fun validationMethods(): Collection<(validationContext: ValidationContext) -> Unit> {
        return emptyList()
    }

    fun allValidationMethods(): Collection<(validationContext: ValidationContext) -> Unit> {
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
