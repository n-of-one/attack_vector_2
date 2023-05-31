package org.n1.av2.backend.entity.site.layer.other

import org.n1.av2.backend.entity.site.enums.LayerType
import org.n1.av2.backend.entity.site.layer.Layer
import org.n1.av2.backend.model.SiteRep
import org.n1.av2.backend.model.ui.ValidationException

private const val MINUTES = "minutes"
private const val SECONDS = "seconds"


class TimerTriggerLayer(
    id: String,
    type: LayerType,
    level: Int,
    name: String,
    note: String,
    var minutes: Long,
    var seconds: Long
) : Layer(id, type, level, name, note) {

    constructor(id: String, level: Int, defaultName: String) :
            this(id, LayerType.TIMER_TRIGGER, level, defaultName, "", 15, 0)


    @Suppress("UNUSED_PARAMETER")
    private fun validatePositiveTriggerTime(siteRep: SiteRep) {
        if (this.seconds == 0L && this.minutes == 0L) throw ValidationException("Time trigger must have a delay of at least one second.")
    }

    override fun validationMethods(): Collection<(siteRep: SiteRep) -> Unit> {
        return  listOf(::validatePositiveTriggerTime )
    }

    override fun updateInternal(key: String, value: String): Boolean {
        when(key) {
            MINUTES -> this.minutes = toNumberOrZero(value)
            SECONDS -> this.seconds = toNumberOrZero(value)
            else -> return super.updateInternal(key, value)
        }
        return true
    }

    private fun toNumberOrZero(value: String): Long {
        try {
            return Math.max(0, value.toLong())
        }
        catch (exception: NumberFormatException) {
            return 0
        }
    }


}