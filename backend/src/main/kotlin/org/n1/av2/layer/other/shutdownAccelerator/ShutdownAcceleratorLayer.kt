package org.n1.av2.layer.other.shutdownAccelerator

import org.n1.av2.editor.SiteValidationException
import org.n1.av2.editor.ValidationContext
import org.n1.av2.layer.Layer
import org.n1.av2.platform.util.validateDuration
import org.n1.av2.site.entity.enums.LayerType

class ShutdownAcceleratorLayer(
    id: String,
    @Suppress("unused") type: LayerType, // exists for consistency and to allow creation from db
    level: Int,
    name: String,
    note: String,
    var increase: String, // How much the timer is increased.
) : Layer(id, LayerType.SHUTDOWN_ACCELERATOR, level, name, note) {

    constructor (id: String, level: Int, name: String, note: String, increase: String, ) :
        this(id, LayerType.SHUTDOWN_ACCELERATOR, level, name, note, increase)

    constructor(id: String, level: Int, defaultName: String) :
        this(id, LayerType.SHUTDOWN_ACCELERATOR, level, defaultName, "", "5:00")

    constructor(id: String, toClone: ShutdownAcceleratorLayer) :
        this(id, LayerType.SHUTDOWN_ACCELERATOR, toClone.level, toClone.name, toClone.note, toClone.increase)

    @Suppress("unused")
    private fun validateIncrease(validationContext: ValidationContext) {
        val errorText = this.increase.validateDuration()
        if (errorText != null) throw SiteValidationException("increase $errorText")
    }


    override fun validationMethods(): Collection<(validationContext: ValidationContext) -> Unit> {
        return listOf(::validateIncrease)
    }

    override fun updateInternal(key: String, value: String): Boolean {
        when (key) {
            "INCREASE" -> this.increase = value

            else -> return super.updateInternal(key, value)
        }
        return true
    }

}
