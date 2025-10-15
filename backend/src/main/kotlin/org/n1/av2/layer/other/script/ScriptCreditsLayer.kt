package org.n1.av2.layer.other.script

import org.n1.av2.editor.SiteStateMessageType.INFO
import org.n1.av2.editor.SiteValidationException
import org.n1.av2.editor.ValidationContext
import org.n1.av2.layer.Layer
import org.n1.av2.site.entity.enums.LayerType

class ScriptCreditsLayer(
    id: String,
    @Suppress("unused") type: LayerType, // exists for consistency and to allow creation from db
    level: Int,
    name: String,
    note: String,

    var amount: Int,
    var stolen: Boolean = true,

) : Layer(id, LayerType.SCRIPT_CREDITS, level, name, note) {

    constructor(id: String, level: Int, name: String, note: String, amount: Int, stolen: Boolean):
        this(id, LayerType.SCRIPT_CREDITS, level, name, note, amount, stolen)

    constructor(id: String, level: Int, defaultName: String) :
        this(id, LayerType.SCRIPT_CREDITS, level, defaultName, "", 0, false)

    constructor(id: String, toClone: ScriptCreditsLayer) :
        this(id, LayerType.SCRIPT_CREDITS, toClone.level, toClone.name, toClone.note, toClone.amount, toClone.stolen)


    @Suppress("unused")
    private fun validateAmount(validationContext: ValidationContext) {
        if (this.amount < 0) throw SiteValidationException("The value cannot be negative.")
        if (this.amount ==0) throw SiteValidationException("Value of Credits Source not yet set, currently at 0 credits.", INFO)
    }

    @Suppress("unused")
    private fun validateCreditStolen(validationContext: ValidationContext) {
        if (this.stolen) throw SiteValidationException("Data stolen, sold for credits.", INFO)
    }

    override fun validationMethods(): Collection<(validationContext: ValidationContext) -> Unit> {
        return listOf(::validateAmount, ::validateCreditStolen)
    }

    override fun updateInternal(key: String, value: String): Boolean {
        when (key) {
            "AMOUNT" -> this.amount = value.toInt()
            "STOLEN" -> this.stolen = value.toBoolean()
            else -> return super.updateInternal(key, value)
        }
        return true
    }
}
