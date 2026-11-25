package org.n1.av2.layer

import org.n1.av2.editor.SiteValidationService
import org.n1.av2.layer.app.status_light.StatusLightService
import org.n1.av2.layer.other.networkswitch.NetworkSwitchService
import org.n1.av2.site.SiteService
import org.n1.av2.site.entity.Node
import org.n1.av2.site.entity.SitePropertiesEntityService
import org.springframework.stereotype.Component

@Component
class LayerEditServices(
    val siteService: SiteService,
    val sitePropertiesEntityService: SitePropertiesEntityService,
    val siteValidationService: SiteValidationService,
    val networkSwitchService: NetworkSwitchService,
    val statusLightService: StatusLightService,
)

/**
 * Passed to a layer that is edited or removed in the editor, so it can handle its own side effects.
 * Side effects that need the saved state go in [afterSave]: they run after the node is saved and its site is validated.
 */
class LayerEditContext(
    val siteId: String,
    val node: Node,
    val services: LayerEditServices,
    val nodeDeleted: Boolean = false,
) {
    private val afterSaveActions = mutableListOf<() -> Unit>()

    fun afterSave(action: () -> Unit) {
        afterSaveActions.add(action)
    }

    fun runAfterSave() {
        afterSaveActions.forEach { it() }
    }
}
