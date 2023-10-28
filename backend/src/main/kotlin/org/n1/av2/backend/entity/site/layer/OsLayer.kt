package org.n1.av2.backend.entity.site.layer

import org.n1.av2.backend.entity.site.enums.LayerType
import org.n1.av2.backend.model.SiteRep
import org.n1.av2.backend.model.ui.ValidationException

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

    private fun validateNetworkId(siteRep: SiteRep) {

        if (siteRep.node.networkId.isBlank()) throw ValidationException("Network Id cannot be empty.")
        val nodesWithSameNetworkId = siteRep.nodes.filter { node ->
            node.networkId === siteRep.node.networkId
        }
        if (nodesWithSameNetworkId.size > 1) throw ValidationException("Duplicate network id: ${siteRep.node.networkId}")
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
