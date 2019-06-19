package org.n1.av2.backend.model.db.service

import org.n1.av2.backend.model.db.site.SiteRep
import org.n1.av2.backend.model.db.site.enums.ServiceType
import org.n1.av2.backend.model.ui.ValidationException

private const val NODE_NAME = "nodeName"

class OsService(
        id: String,
        type: ServiceType,
        layer: Int,
        name: String,
        note: String,
        var nodeName: String
) : Service(id, type, layer, name, note) {

    constructor(id: String, defaultName: String) :
            this(id, ServiceType.OS, 0, defaultName, "", "")

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
            NODE_NAME -> nodeName = value
            else -> return super.updateInternal(key, value)
        }
        return true
    }

}