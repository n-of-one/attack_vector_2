package org.n1.av2.layer.other.os

import org.n1.av2.editor.SiteValidationException
import org.n1.av2.editor.ValidationContext
import org.n1.av2.layer.Layer
import org.n1.av2.site.entity.enums.LayerType

class OsLayer(
    id: String,
    @Suppress("unused") type: LayerType, // exists for consistency and to allow creation from db
    level: Int,
    name: String,
    note: String,
    var nodeName: String
) : Layer(id, LayerType.OS, level, name, note) {

    constructor(id: String, level: Int, name: String, note: String, nodeName: String) :
        this(id, LayerType.OS, level, name, note, nodeName)

    constructor(id: String, defaultName: String) :
        this(id, LayerType.OS, 0, defaultName, "", "")

    constructor(id: String, toClone: OsLayer) :
        this(id, LayerType.OS, toClone.level, toClone.name, toClone.note, toClone.nodeName)

    private fun validateNetworkId(validationContext: ValidationContext) {

        if (validationContext.node.networkId.isBlank()) throw SiteValidationException("Network Id cannot be empty.")
        val nodesWithSameNetworkId = validationContext.nodes.filter { node ->
            node.networkId === validationContext.node.networkId
        }
        if (nodesWithSameNetworkId.size > 1) throw SiteValidationException("Duplicate network id: ${validationContext.node.networkId}")
    }

    override fun validationMethods(): Collection<(validationContext: ValidationContext) -> Unit> {
        return listOf(::validateNetworkId)
    }

    override fun updateInternal(key: String, value: String): Boolean {
        when (key) {
            "NODE_NAME" -> nodeName = value
            else -> return super.updateInternal(key, value)
        }
        return true
    }
}
