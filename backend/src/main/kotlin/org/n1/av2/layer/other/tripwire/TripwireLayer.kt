package org.n1.av2.layer.other.tripwire

import org.n1.av2.editor.SiteStateMessageType
import org.n1.av2.editor.SiteValidationException
import org.n1.av2.editor.ValidationContext
import org.n1.av2.layer.Layer
import org.n1.av2.layer.other.core.CoreLayer
import org.n1.av2.platform.util.toDuration
import org.n1.av2.platform.util.toHumanTime
import org.n1.av2.platform.util.validateDuration
import org.n1.av2.site.entity.NodeEntityService.Companion.deriveNodeIdFromLayerId
import org.n1.av2.site.entity.enums.LayerType
import java.time.Duration
import kotlin.jvm.optionals.getOrElse

class TripwireLayer(
    id: String,
    @Suppress("unused") type: LayerType, // exists for consistency and to allow creation from db
    level: Int,
    name: String,
    note: String,
    var countdown: String, // shutdown activates after a duration of $countdown
    var shutdown: String, // shut down lasts for a duration of $shutdown
    var coreLayerId: String? = null,
    var coreSiteId: String? = null,
) : Layer(id, LayerType.TRIPWIRE, level, name, note) {

    constructor (id: String, level: Int, name: String, note: String, countdown: String, shutdown: String, coreLayerId: String?, coreSiteId: String?) :
        this(id, LayerType.TRIPWIRE, level, name, note, countdown, shutdown, coreLayerId, coreSiteId)

    constructor(id: String, level: Int, defaultName: String) :
        this(id, LayerType.TRIPWIRE, level, defaultName, "", "15:00", "01:00", null)

    constructor(id: String, toClone: TripwireLayer) :
        this(id, LayerType.TRIPWIRE, toClone.level, toClone.name, toClone.note, toClone.countdown, toClone.shutdown, toClone.coreLayerId, toClone.coreSiteId)

    @Suppress("unused")
    private fun validateCountdown(validationContext: ValidationContext) {
        val errorText = this.countdown.validateDuration()
        if (errorText != null) throw SiteValidationException("countdown $errorText")
    }

    @Suppress("unused")
    private fun validateShutdown(validationContext: ValidationContext) {
        val errorText = this.shutdown.validateDuration()
        if (errorText != null) throw SiteValidationException("countdown $shutdown")

        val duration = this.shutdown.toDuration()

        if (duration < validationContext.minimumShutdownDuration) {
            throw SiteValidationException("Shutdown must be at least ${validationContext.minimumShutdownDuration.toHumanTime()}.")
        }
    }

    @Suppress("unused")
    private fun validateCoreSiteId(validationContext: ValidationContext) {
        if (this.coreSiteId == null) return
        validationContext.sitePropertiesRepo.findBySiteId(this.coreSiteId!!)
            ?: throw SiteValidationException("Tripwire connected to site that was later removed.")
    }

    private fun validateCoreLayerId(validationContext: ValidationContext) {
        if (this.coreLayerId == null) throw SiteValidationException("Tripwire not connected to core, no way to reset.", SiteStateMessageType.INFO)

        val nodeId = deriveNodeIdFromLayerId(this.coreLayerId!!)
        val node = validationContext.nodeRepo.findById(nodeId).getOrElse {
            throw SiteValidationException("Tripwire connected to core layer that was later removed.")
        }
        val layer = node.getLayerById(this.coreLayerId!!)
        if (layer !is CoreLayer) throw SiteValidationException("Trip wire connected to a layer that is not a core.")
    }

    override fun validationMethods(): Collection<(validationContext: ValidationContext) -> Unit> {
        return listOf(::validateCountdown, ::validateShutdown, ::validateCoreLayerId)
    }

    override fun updateInternal(key: String, value: String): Boolean {
        when (key) {
            "COUNTDOWN" -> this.countdown = value
            "SHUTDOWN" -> this.shutdown = value
            "CORE_LAYER_ID" -> this.coreLayerId = if (!value.isEmpty()) value else null
            "CORE_SITE_ID" -> {
                this.coreSiteId = value
                this.coreLayerId = null // site changed, so current core layer is not valid in new site
            }

            else -> return super.updateInternal(key, value)
        }
        return true
    }

}
