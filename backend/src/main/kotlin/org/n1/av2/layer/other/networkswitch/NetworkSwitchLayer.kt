package org.n1.av2.layer.other.networkswitch

import org.n1.av2.editor.SiteValidationException
import org.n1.av2.editor.ValidationContext
import org.n1.av2.layer.Layer
import org.n1.av2.layer.other.core.CoreLayer
import org.n1.av2.layer.other.core.CoreLinkedLayer
import org.n1.av2.layer.other.core.updateCoreLink
import org.n1.av2.site.entity.NodeEntityService.Companion.deriveNodeIdFromLayerId
import org.n1.av2.site.entity.enums.LayerType
import kotlin.jvm.optionals.getOrElse

const val ROUTE_NAME_FOR = "ROUTE_NAME_FOR_"
const val ROUTE_SWITCHED_FOR = "ROUTE_SWITCHED_FOR_"
const val LOCKED_ROUTE_NODE_ID = "LOCKED_ROUTE_NODE_ID"

val ROUTE_NAME_PATTERN = Regex("^[A-Za-z0-9_.-]+$")

data class NetworkSwitchRoute(
    val name: String,
    val nodeId: String,
)

/**
 * Lets a hacker choose one of the connections to the nodes in [routes]. The choice survives site resets, only hacking the linked core
 * (or the GM in the editor) unlocks it.
 */
class NetworkSwitchLayer(
    id: String,
    @Suppress("unused") type: LayerType, // exists for consistency and to allow creation from db
    level: Int,
    name: String,
    note: String,
    var routes: MutableList<NetworkSwitchRoute>,
    var lockedRouteNodeId: String?,
    override var coreLayerId: String? = null,
    override var coreSiteId: String? = null,
    override var coreSiteName: String? = null,
    override var coreNetworkId: String? = null,
    override var coreLayerLevel: Int? = null,
) : Layer(id, LayerType.NETWORK_SWITCH, level, name, note), CoreLinkedLayer {

    constructor(id: String, level: Int, name: String, note: String, routes: MutableList<NetworkSwitchRoute>, lockedRouteNodeId: String?,
                coreLayerId: String? = null, coreSiteId: String? = null,
                coreSiteName: String? = null, coreNetworkId: String? = null, coreLayerLevel: Int? = null) :
        this(id, LayerType.NETWORK_SWITCH, level, name, note, routes, lockedRouteNodeId, coreLayerId, coreSiteId, coreSiteName, coreNetworkId, coreLayerLevel)

    constructor(id: String, level: Int, defaultName: String) :
        this(id, LayerType.NETWORK_SWITCH, level, defaultName, "", mutableListOf(), null)

    // A copy is a new site, so the choice is not copied.
    constructor(id: String, toClone: NetworkSwitchLayer) :
        this(id, LayerType.NETWORK_SWITCH, toClone.level, toClone.name, toClone.note, toClone.routes.toMutableList(), null,
            toClone.coreLayerId, toClone.coreSiteId, toClone.coreSiteName, toClone.coreNetworkId, toClone.coreLayerLevel)

    fun routeFor(nodeId: String): NetworkSwitchRoute? = routes.find { it.nodeId == nodeId }

    @Suppress("unused")
    private fun validateRouteCount(validationContext: ValidationContext) {
        if (routes.size < 2) throw SiteValidationException("Network switch needs at least 2 routes.")
    }

    @Suppress("unused")
    private fun validateRoutes(validationContext: ValidationContext) {
        val nodeId = validationContext.node.id
        routes.forEach { route ->
            if (route.name.isEmpty()) {
                val networkId = validationContext.nodes.find { it.id == route.nodeId }?.networkId ?: "?"
                throw SiteValidationException("Network switch route → $networkId needs a route name.")
            }
            if (!ROUTE_NAME_PATTERN.matches(route.name)) {
                throw SiteValidationException("Network switch route name '${route.name}' can only contain letters, digits, '.', '-' and '_'.")
            }
            val connected = validationContext.connections.any {
                (it.fromId == nodeId && it.toId == route.nodeId) || (it.toId == nodeId && it.fromId == route.nodeId)
            }
            if (!connected) throw SiteValidationException("Network switch route '${route.name}' leads to a node that is not connected to this node.")
        }
        val duplicateName = routes.filter { it.name.isNotEmpty() }.groupBy { it.name.lowercase() }.entries.find { it.value.size > 1 }?.key
        if (duplicateName != null) throw SiteValidationException("Network switch has more than one route named '$duplicateName'.")
    }

    @Suppress("unused")
    private fun validateCoreLayerId(validationContext: ValidationContext) {
        val coreLayerId = this.coreLayerId ?: return
        val node = validationContext.nodeRepo.findById(deriveNodeIdFromLayerId(coreLayerId)).getOrElse {
            throw SiteValidationException("Network switch connected to core layer that was later removed.")
        }
        if (node.layers.none { it.id == coreLayerId && it is CoreLayer }) {
            throw SiteValidationException("Network switch connected to a layer that is not a core.")
        }
    }

    override fun validationMethods(): Collection<(validationContext: ValidationContext) -> Unit> {
        return listOf(::validateRouteCount, ::validateRoutes, ::validateCoreLayerId)
    }

    override fun updateInternal(key: String, value: String): Boolean {
        if (updateCoreLink(key, value)) return true
        if (key.startsWith(ROUTE_NAME_FOR)) {
            updateRouteName(key.substringAfter(ROUTE_NAME_FOR), value.trim())
            return true
        }
        if (key.startsWith(ROUTE_SWITCHED_FOR)) {
            updateRouteSwitched(key.substringAfter(ROUTE_SWITCHED_FOR), value.toBoolean())
            return true
        }
        if (key == LOCKED_ROUTE_NODE_ID) {
            val nodeId = value.ifBlank { null }
            if (nodeId != null && routeFor(nodeId) == null) error("Not a route of this network switch: $nodeId")
            lockedRouteNodeId = nodeId
            return true
        }
        return super.updateInternal(key, value)
    }

    // Typing a name for a route that is not switched yet also switches it.
    private fun updateRouteName(nodeId: String, routeName: String) {
        val index = routes.indexOfFirst { it.nodeId == nodeId }
        val route = NetworkSwitchRoute(routeName, nodeId)
        if (index >= 0) {
            routes[index] = route
        } else if (routeName.isNotEmpty()) {
            routes.add(route)
        }
    }

    private fun updateRouteSwitched(nodeId: String, switched: Boolean) {
        if (switched) {
            if (routeFor(nodeId) == null) routes.add(NetworkSwitchRoute("", nodeId))
            return
        }
        routes.removeIf { it.nodeId == nodeId }
        if (lockedRouteNodeId == nodeId) lockedRouteNodeId = null
    }
}
