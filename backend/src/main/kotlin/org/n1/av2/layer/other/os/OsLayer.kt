package org.n1.av2.layer.other.os

import org.n1.av2.editor.SiteRep
import org.n1.av2.editor.SiteValidationException
import org.n1.av2.layer.Layer
import org.n1.av2.site.entity.enums.LayerType

class OsLayer(
    id: String,
    type: LayerType,
    level: Int,
    name: String,
    note: String,
    var nodeName: String
) : Layer(id, type, level, name, note) {

    constructor(id: String, defaultName: String) :
            this(id, LayerType.OS, 0, defaultName, "", "")

    constructor(id: String, toClone: OsLayer) :
            this(id, LayerType.OS, toClone.level, toClone.name, toClone.note, toClone.nodeName)

    private fun validateNetworkId(siteRep: SiteRep) {

        if (siteRep.node.networkId.isBlank()) throw SiteValidationException("Network Id cannot be empty.")
        val nodesWithSameNetworkId = siteRep.nodes.filter { node ->
            node.networkId === siteRep.node.networkId
        }
        if (nodesWithSameNetworkId.size > 1) throw SiteValidationException("Duplicate network id: ${siteRep.node.networkId}")
    }

    override fun validationMethods(): Collection<(siteRep: SiteRep) -> Unit> {
        return listOf(::validateNetworkId)
    }

    override fun updateInternal(key: String, value: String): Boolean {
        when(key) {
            "NODE_NAME" -> nodeName = value
            else -> return super.updateInternal(key, value)
        }
        return true
    }
}
