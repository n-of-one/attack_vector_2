package org.n1.av2.layer.other.tripwire

import org.n1.av2.editor.SiteRep
import org.n1.av2.editor.SiteStateMessageType
import org.n1.av2.editor.SiteValidationException
import org.n1.av2.layer.Layer
import org.n1.av2.layer.other.core.CoreLayer
import org.n1.av2.platform.util.toDuration
import org.n1.av2.platform.util.validateDuration
import org.n1.av2.site.entity.enums.LayerType
import java.time.Duration

val MINIMUM_SHUTDOWN: Duration = Duration.ofMinutes(1)

class TripwireLayer(
    id: String,
    type: LayerType,
    level: Int,
    name: String,
    note: String,
    var countdown: String, // shutdown activates after a duration of $countdown
    var shutdown: String, // shut down lasts for a duration of $shutdown
    var coreLayerId: String? = null,

    ) : Layer(id, type, level, name, note) {

    constructor(id: String, level: Int, defaultName: String) :
            this(id, LayerType.TRIPWIRE, level, defaultName, "", "15:00", "01:00", null)

    constructor(id: String, toClone: TripwireLayer) :
            this(id, LayerType.TRIPWIRE, toClone.level, toClone.name, toClone.note, toClone.countdown, toClone.shutdown, toClone.coreLayerId)

    @Suppress("UNUSED_PARAMETER")
    private fun validateCountdown(siteRep: SiteRep) {
        val errorText = this.countdown.validateDuration()
        if (errorText != null) throw SiteValidationException("countdown $errorText")
    }

    @Suppress("UNUSED_PARAMETER")
    private fun validateShutdown(siteRep: SiteRep) {
        val errorText = this.shutdown.validateDuration()
        if (errorText != null) throw SiteValidationException("countdown $shutdown")

        val duration = this.shutdown.toDuration()

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
