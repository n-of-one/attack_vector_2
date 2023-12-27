package org.n1.av2.backend.entity.site.layer.other

import org.n1.av2.backend.entity.site.SiteStateMessageType
import org.n1.av2.backend.entity.site.enums.LayerType
import org.n1.av2.backend.entity.site.layer.Layer
import org.n1.av2.backend.model.SiteRep
import org.n1.av2.backend.service.site.SiteValidationException
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
    var coreLayerId: String? = null,

) : Layer(id, type, level, name, note) {

    constructor(id: String, level: Int, defaultName: String) :
            this(id, LayerType.TRIPWIRE, level, defaultName, "", "15:00", "01:00", null)

    @Suppress("UNUSED_PARAMETER")
    private fun validateCountdown(siteRep: SiteRep) {
        this.countdown.toDuration("countdown")
    }

    @Suppress("UNUSED_PARAMETER")
    private fun validateShutdown(siteRep: SiteRep) {
        val duration = this.shutdown.toDuration("shutdown")

        if (duration < MINIMUM_SHUTDOWN) {
            throw SiteValidationException("Shutdown must be at least 01:00 (1 minute).")
        }
    }

    private fun validateCoreLayerId(siteRep: SiteRep) {
        if (this.coreLayerId == null) throw SiteValidationException("Tripwire not connected to core, no way to reset.", SiteStateMessageType.INFO)
        val layer = siteRep.findLayer(this.coreLayerId!!) ?: throw SiteValidationException("Tripwire connected to core layer that was later removed.")
        if (layer !is CoreLayer) throw SiteValidationException("Trip wire connected to a layer that is not a core.")
    }

    override fun validationMethods(): Collection<(siteRep: SiteRep) -> Unit> {
        return  listOf(::validateCountdown, ::validateShutdown, ::validateCoreLayerId )
    }

    override fun updateInternal(key: String, value: String): Boolean {
        when(key) {
            "COUNTDOWN" -> this.countdown = value
            "SHUTDOWN" -> this.shutdown = value
            "CORE_LAYER_ID" -> this.coreLayerId = value
            else -> return super.updateInternal(key, value)
        }
        return true
    }
}
