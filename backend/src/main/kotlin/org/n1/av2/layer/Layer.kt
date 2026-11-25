package org.n1.av2.layer

import org.n1.av2.editor.SiteValidationException
import org.n1.av2.editor.ValidationContext
import org.n1.av2.site.entity.enums.LayerType


const val NAME = "name"
const val NOTE = "note"

abstract class Layer(
    val id: String,
    val type: LayerType,
    var level: Int,
    var name: String,
    var note: String
) {

    constructor(id: String, type: LayerType, level: Int, defaultName: String) :
            this(id, type, level, defaultName, "")


    @Suppress("unused")
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

    fun update(key: String, value: String, context: LayerEditContext) {
        val keyFound = updateInternal(key, value, context)
        if (!keyFound) error("Unknown key: ${key} for service type: ${type}")
    }

    // Override this variant for edits with side effects.
    open fun updateInternal(key: String, value: String, context: LayerEditContext): Boolean {
        return updateInternal(key, value)
    }

    // Called before the layer is removed, also when its node is deleted. Throw a ValidationException to prevent the removal.
    open fun onRemove(context: LayerEditContext) {
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
