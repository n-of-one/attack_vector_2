package org.n1.av2.backend.model.db.layer

import org.n1.av2.backend.model.SiteRep
import org.n1.av2.backend.model.db.site.enums.LayerType
import org.n1.av2.backend.model.ui.ValidationException

private const val MINUTES = "minutes"
private const val SECONDS = "seconds"


class TimerTriggerLayer(
        id: String,
        type: LayerType,
        level: Int,
        name: String,
        note: String,
        var minutes: Int,
        var seconds: Int
) : Layer(id, type, level, name, note) {

    constructor(id: String, level: Int, defaultName: String) :
            this(id, LayerType.TIMER_TRIGGER, level, defaultName, "", 15, 0)


    @Suppress("UNUSED_PARAMETER")
    private fun validatePositiveTriggerTime(siteRep: SiteRep) {
        if (this.seconds == 0 && this.minutes == 0) throw ValidationException("Time trigger must have a delay of at least one second.")
    }

    override fun validationMethods(): Collection<(siteRep: SiteRep) -> Unit> {
        return  listOf(::validatePositiveTriggerTime )
    }

    override fun updateInternal(key: String, value: String): Boolean {
        when(key) {
            MINUTES -> this.minutes = toInt(value)
            SECONDS -> this.seconds = toInt(value)
            else -> return super.updateInternal(key, value)
        }
        return true
    }

    private fun toInt(value: String): Int {
        try {
            return Math.max(0, value.toInt())
        }
        catch (exception: NumberFormatException) {
            return 0
        }
    }


}