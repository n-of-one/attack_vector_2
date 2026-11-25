package org.n1.av2.layer.other.uplink

import org.n1.av2.editor.SiteStateMessageType
import org.n1.av2.editor.SiteValidationException
import org.n1.av2.editor.ValidationContext
import org.n1.av2.layer.Layer
import org.n1.av2.layer.LayerEditContext
import org.n1.av2.site.entity.enums.LayerType

const val TARGET_SITE_ID = "TARGET_SITE_ID"

class UplinkLayer(
    id: String,
    @Suppress("unused") type: LayerType, // exists for consistency and to allow creation from db
    level: Int,
    name: String,
    note: String,
    var targetSiteId: String?,
    var targetSiteName: String?, // kept in sync on edit and site rename, so the hacker view and export need no lookup
) : Layer(id, LayerType.UPLINK, level, name, note) {

    constructor(id: String, level: Int, name: String, note: String, targetSiteId: String?, targetSiteName: String?) :
        this(id, LayerType.UPLINK, level, name, note, targetSiteId, targetSiteName)

    constructor(id: String, level: Int, defaultName: String) :
        this(id, LayerType.UPLINK, level, defaultName, "", null, null)

    constructor(id: String, toClone: UplinkLayer) :
        this(id, LayerType.UPLINK, toClone.level, toClone.name, toClone.note, toClone.targetSiteId, toClone.targetSiteName)

    @Suppress("unused")
    private fun validateTarget(validationContext: ValidationContext) {
        val targetSiteId = this.targetSiteId ?: throw SiteValidationException("Uplink has no target site.")
        val target = validationContext.sitePropertiesRepo.findBySiteId(targetSiteId)
            ?: throw SiteValidationException("Uplink target site '${targetSiteName ?: "?"}' does not exist.")
        if (target.siteId == validationContext.siteProperties.siteId) throw SiteValidationException("Uplink cannot target its own site.")
        if (!target.internal) {
            throw SiteValidationException("Uplink target site '${target.name}' is not internal, it can also be reached from outside.", SiteStateMessageType.INFO)
        }
    }

    override fun validationMethods(): Collection<(validationContext: ValidationContext) -> Unit> {
        return listOf(::validateTarget)
    }

    override fun updateInternal(key: String, value: String, context: LayerEditContext): Boolean {
        if (key != TARGET_SITE_ID) return super.updateInternal(key, value, context)

        val previousTargetSiteId = targetSiteId
        targetSiteId = value.ifBlank { null }
        targetSiteName = targetSiteId?.let { context.services.sitePropertiesEntityService.getBySiteId(it).name }
        validateTargetsAfterSave(listOfNotNull(previousTargetSiteId, targetSiteId), context)
        return true
    }

    override fun onRemove(context: LayerEditContext) {
        validateTargetsAfterSave(listOfNotNull(targetSiteId), context)
    }

    // An internal site is only valid while an uplink connects to it.
    private fun validateTargetsAfterSave(siteIds: List<String>, context: LayerEditContext) {
        context.afterSave {
            siteIds.distinct()
                .filter { it != context.siteId } // the editor validates the edited site itself
                .forEach { context.services.siteValidationService.validate(it) }
        }
    }
}
