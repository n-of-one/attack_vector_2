package org.n1.av2.backend.entity.site.layer.other

import org.n1.av2.backend.entity.site.enums.LayerType
import org.n1.av2.backend.entity.site.layer.Layer
import org.n1.av2.backend.model.SiteRep
import org.n1.av2.backend.model.ui.ValidationException
import org.n1.av2.backend.service.site.toDuration
import java.time.Duration

private val MINIMUM_SHUTDOWN = Duration.ofMinutes(1)

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
            this(id, LayerType.TRIPWIRE, level, defaultName, "", "15:00", "01:00")

    @Suppress("UNUSED_PARAMETER")
    private fun validateCountdown(siteRep: SiteRep) {
        this.countdown.toDuration("countdown")
    }

    private fun validateShutdown(siteRep: SiteRep) {
        val duration = this.shutdown.toDuration("shutdown")

        if (duration < MINIMUM_SHUTDOWN) {
            throw ValidationException("Shutdown must be at least 01:00 (1 minute).")
        }
    }

    override fun validationMethods(): Collection<(siteRep: SiteRep) -> Unit> {
        return  listOf(::validateCountdown, ::validateShutdown )
    }

    override fun updateInternal(key: String, value: String): Boolean {
        when(key) {
            "COUNTDOWN" -> this.countdown = value
            "SHUTDOWN" -> this.shutdown = value
            else -> return super.updateInternal(key, value)
        }
        return true
    }
}
