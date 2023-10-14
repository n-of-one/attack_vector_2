package org.n1.av2.backend.entity.site.layer.other

import org.n1.av2.backend.entity.site.enums.LayerType
import org.n1.av2.backend.entity.site.layer.Layer
import org.n1.av2.backend.model.SiteRep
import org.n1.av2.backend.model.ui.ValidationException
import org.n1.av2.backend.service.site.toDuration

private const val COUNTDOWN = "countdown"
private const val SHUTDOWN = "shutdown"

class TripwireLayer(
    id: String,
    type: LayerType,
    level: Int,
    name: String,
    note: String,
    var countdown: String,
    var shutdown: String,

) : Layer(id, type, level, name, note) {

    constructor(id: String, level: Int, defaultName: String) :
            this(id, LayerType.TRIPWIRE, level, defaultName, "", "15:00", "0:00")


    @Suppress("UNUSED_PARAMETER")
    private fun validateCountdown(siteRep: SiteRep) {
        this.countdown.toDuration("countdown")
    }

    private fun validateShutdown(siteRep: SiteRep) {
        this.shutdown.toDuration("shutdown")
    }


    override fun validationMethods(): Collection<(siteRep: SiteRep) -> Unit> {
        return  listOf(::validateCountdown, ::validateShutdown )
    }

    override fun updateInternal(key: String, value: String): Boolean {
        when(key) {
            COUNTDOWN -> this.countdown = value
            SHUTDOWN -> this.shutdown = value
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