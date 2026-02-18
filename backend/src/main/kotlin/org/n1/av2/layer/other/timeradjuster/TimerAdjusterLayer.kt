package org.n1.av2.layer.other.timeradjuster

import org.n1.av2.editor.SiteValidationException
import org.n1.av2.editor.ValidationContext
import org.n1.av2.layer.Layer
import org.n1.av2.platform.util.validateDuration
import org.n1.av2.site.entity.enums.LayerType

enum class TimerAdjustmentType { SPEED_UP, SLOW_DOWN }
enum class TimerAdjustmentRecurring { FIRST_ENTRY_ONLY, EVERY_ENTRY, EACH_HACKER_ONCE }

class TimerAdjusterLayer(
    id: String,
    @Suppress("unused") type: LayerType, // exists for consistency and to allow creation from db
    level: Int,
    name: String,
    note: String,
    var amount: String, // How much the timer is increased.
    var adjustmentType: TimerAdjustmentType,
    var recurring: TimerAdjustmentRecurring,
) : Layer(id, LayerType.TIMER_ADJUSTER, level, name, note) {

    constructor (id: String, level: Int, name: String, note: String, amount: String, adjustmentType: TimerAdjustmentType, recurring: TimerAdjustmentRecurring) :
        this(id, LayerType.TIMER_ADJUSTER, level, name, note, amount, adjustmentType, recurring)

    constructor(id: String, level: Int, defaultName: String) :
        this(id, LayerType.TIMER_ADJUSTER, level, defaultName, "", "5:00", TimerAdjustmentType.SPEED_UP, TimerAdjustmentRecurring.FIRST_ENTRY_ONLY)

    constructor(id: String, toClone: TimerAdjusterLayer) :
        this(id, LayerType.TIMER_ADJUSTER, toClone.level, toClone.name, toClone.note, toClone.amount, toClone.adjustmentType, toClone.recurring)

    @Suppress("unused")
    private fun validateIncrease(validationContext: ValidationContext) {
        val errorText = this.amount.validateDuration()
        if (errorText != null) throw SiteValidationException("increase $errorText")
    }


    override fun validationMethods(): Collection<(validationContext: ValidationContext) -> Unit> {
        return listOf(::validateIncrease)
    }

    override fun updateInternal(key: String, value: String): Boolean {
        when (key) {
            "AMOUNT" -> this.amount = value
            "ADJUSTMENT_TYPE" -> updateAdjustmentType(TimerAdjustmentType.valueOf(value))
            "ADJUSTMENT_RECURRING" -> this.recurring = TimerAdjustmentRecurring.valueOf(value)

            else -> return super.updateInternal(key, value)
        }
        return true
    }

    fun updateAdjustmentType(newType: TimerAdjustmentType) {
        if (this.adjustmentType == TimerAdjustmentType.SPEED_UP && this.name == "Timer accelerator" && newType == TimerAdjustmentType.SLOW_DOWN) {
            this.name = "Timer decelerator"
        }
        if (this.adjustmentType == TimerAdjustmentType.SLOW_DOWN && this.name == "Timer decelerator" && newType == TimerAdjustmentType.SPEED_UP) {
            this.name = "Timer accelerator"
        }

        this.adjustmentType = newType
    }

}
