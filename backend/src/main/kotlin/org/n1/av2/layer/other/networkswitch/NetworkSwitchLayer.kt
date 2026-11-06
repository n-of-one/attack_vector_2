package org.n1.av2.layer.other.networkswitch

import org.n1.av2.editor.SiteValidationException
import org.n1.av2.editor.ValidationContext
import org.n1.av2.layer.Layer
import org.n1.av2.site.entity.enums.LayerType

const val PATH_LABEL_FOR = "PATH_LABEL_FOR_"
const val CHOSEN = "CHOSEN"

val PATH_LABEL_PATTERN = Regex("^[A-Za-z0-9_.-]+$")

data class NetworkSwitchPath(
    val label: String,
    val nodeId: String,
)

/**
 * Lets a hacker choose one of the connections to the nodes in [paths]. The choice is permanent: it survives site resets.
 */
class NetworkSwitchLayer(
    id: String,
    @Suppress("unused") type: LayerType, // exists for consistency and to allow creation from db
    level: Int,
    name: String,
    note: String,
    var paths: MutableList<NetworkSwitchPath>,
    var chosenNodeId: String?,
) : Layer(id, LayerType.NETWORK_SWITCH, level, name, note) {

    constructor(id: String, level: Int, name: String, note: String, paths: MutableList<NetworkSwitchPath>, chosenNodeId: String?) :
        this(id, LayerType.NETWORK_SWITCH, level, name, note, paths, chosenNodeId)

    constructor(id: String, level: Int, defaultName: String) :
        this(id, LayerType.NETWORK_SWITCH, level, defaultName, "", mutableListOf(), null)

    // A copy is a new site, so the choice is not copied.
    constructor(id: String, toClone: NetworkSwitchLayer) :
        this(id, LayerType.NETWORK_SWITCH, toClone.level, toClone.name, toClone.note, toClone.paths.toMutableList(), null)

    fun pathFor(nodeId: String): NetworkSwitchPath? = paths.find { it.nodeId == nodeId }

    @Suppress("unused")
    private fun validatePathCount(validationContext: ValidationContext) {
        if (paths.size < 2) throw SiteValidationException("Network switch needs at least 2 paths.")
    }

    @Suppress("unused")
    private fun validatePaths(validationContext: ValidationContext) {
        val nodeId = validationContext.node.id
        paths.forEach { path ->
            if (!PATH_LABEL_PATTERN.matches(path.label)) {
                throw SiteValidationException("Network switch path label '${path.label}' can only contain letters, digits, '.', '-' and '_'.")
            }
            val connected = validationContext.connections.any {
                (it.fromId == nodeId && it.toId == path.nodeId) || (it.toId == nodeId && it.fromId == path.nodeId)
            }
            if (!connected) throw SiteValidationException("Network switch path '${path.label}' leads to a node that is not connected to this node.")
        }
        val duplicateLabel = paths.groupBy { it.label.lowercase() }.entries.find { it.value.size > 1 }?.key
        if (duplicateLabel != null) throw SiteValidationException("Network switch has more than one path with label '$duplicateLabel'.")
    }

    override fun validationMethods(): Collection<(validationContext: ValidationContext) -> Unit> {
        return listOf(::validatePathCount, ::validatePaths)
    }

    override fun updateInternal(key: String, value: String): Boolean {
        if (key.startsWith(PATH_LABEL_FOR)) {
            updatePathLabel(key.substringAfter(PATH_LABEL_FOR), value.trim())
            return true
        }
        if (key == CHOSEN) {
            val nodeId = value.ifBlank { null }
            if (nodeId != null && pathFor(nodeId) == null) error("Not a path of this network switch: $nodeId")
            chosenNodeId = nodeId
            return true
        }
        return super.updateInternal(key, value)
    }

    private fun updatePathLabel(nodeId: String, label: String) {
        val index = paths.indexOfFirst { it.nodeId == nodeId }
        if (label.isEmpty()) {
            if (index >= 0) paths.removeAt(index)
            if (chosenNodeId == nodeId) chosenNodeId = null
            return
        }
        val path = NetworkSwitchPath(label, nodeId)
        if (index >= 0) paths[index] = path else paths.add(path)
    }
}
