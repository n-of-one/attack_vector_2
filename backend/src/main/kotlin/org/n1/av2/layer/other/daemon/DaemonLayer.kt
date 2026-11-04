package org.n1.av2.layer.other.daemon

import org.n1.av2.editor.SiteValidationException
import org.n1.av2.editor.ValidationContext
import org.n1.av2.hacker.skill.TRAIT_KEY_PATTERN
import org.n1.av2.layer.Layer
import org.n1.av2.layer.model.TriggerRecurrence
import org.n1.av2.site.entity.enums.LayerType

/**
 * Shows a text in the terminal of a hacker that enters the node. Optionally only for hackers that have a TRAIT skill with the required key.
 */
class DaemonLayer(
    id: String,
    @Suppress("unused") type: LayerType, // exists for consistency and to allow creation from db
    level: Int,
    name: String,
    note: String,
    var text: String,
    var requiredTrait: String?,
    var recurring: TriggerRecurrence,
) : Layer(id, LayerType.DAEMON, level, name, note) {

    constructor(id: String, level: Int, name: String, note: String, text: String, requiredTrait: String?, recurring: TriggerRecurrence) :
        this(id, LayerType.DAEMON, level, name, note, text, requiredTrait, recurring)

    constructor(id: String, level: Int, defaultName: String) :
        this(id, LayerType.DAEMON, level, defaultName, "", "Connection logged.", null, TriggerRecurrence.EACH_HACKER_ONCE)

    constructor(id: String, toClone: DaemonLayer) :
        this(id, LayerType.DAEMON, toClone.level, toClone.name, toClone.note, toClone.text, toClone.requiredTrait, toClone.recurring)

    @Suppress("unused")
    private fun validateText(validationContext: ValidationContext) {
        if (this.text.isBlank()) throw SiteValidationException("Daemon text cannot be empty.")
    }

    @Suppress("unused")
    private fun validateRequiredTrait(validationContext: ValidationContext) {
        val requiredTrait = this.requiredTrait ?: return
        if (!TRAIT_KEY_PATTERN.matches(requiredTrait)) {
            throw SiteValidationException("Daemon required trait can only contain letters, digits, '.', '-' and '_'.")
        }
    }

    override fun validationMethods(): Collection<(validationContext: ValidationContext) -> Unit> {
        return listOf(::validateText, ::validateRequiredTrait)
    }

    override fun updateInternal(key: String, value: String): Boolean {
        when (key) {
            "TEXT" -> this.text = value
            "REQUIRED_TRAIT" -> this.requiredTrait = value.trim().ifEmpty { null }
            "RECURRING" -> this.recurring = TriggerRecurrence.valueOf(value)
            else -> return super.updateInternal(key, value)
        }
        return true
    }
}
