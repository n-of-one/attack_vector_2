package org.n1.av2.layer.other.script

import org.n1.av2.editor.SiteRep
import org.n1.av2.editor.SiteStateMessageType
import org.n1.av2.editor.SiteValidationException
import org.n1.av2.layer.Layer
import org.n1.av2.site.entity.enums.LayerType

class ScriptInteractionLayer(
    id: String,
    @Suppress("unused") type: LayerType, // exists for consistency and to allow creation from db
    level: Int,
    name: String,
    note: String,
    var interactionKey: String,
    var message: String,
) : Layer(id, LayerType.SCRIPT_INTERACTION, level, name, note) {

    constructor(id: String, level: Int, name: String, note: String, interactionKey: String, message: String) :
        this(id, LayerType.SCRIPT_INTERACTION, level, name, note, interactionKey, message)

    constructor(id: String, level: Int, defaultName: String) :
        this(id, LayerType.SCRIPT_INTERACTION, level, defaultName, "", "", "")

    constructor(id: String, toClone: ScriptInteractionLayer) :
        this(id, LayerType.SCRIPT_INTERACTION, toClone.level, toClone.name, toClone.note, toClone.interactionKey, toClone.message)


    @Suppress("unused")
    private fun validateText(siteRep: SiteRep) {
        if (this.interactionKey.isEmpty()) throw SiteValidationException("The interaction key cannot be empty.")
    }

    @Suppress("unused")
    private fun validateMessage(siteRep: SiteRep) {
        if (this.message.isEmpty()) throw SiteValidationException("With an empty message, this layer does not do anything.", SiteStateMessageType.INFO)
    }

    override fun validationMethods(): Collection<(siteRep: SiteRep) -> Unit> {
        return listOf(::validateText, ::validateMessage)
    }

    override fun updateInternal(key: String, value: String): Boolean {
        when (key) {
            "INTERACTION_KEY" -> interactionKey = value
            "MESSAGE" -> message = value
            else -> return super.updateInternal(key, value)
        }
        return true
    }
}
