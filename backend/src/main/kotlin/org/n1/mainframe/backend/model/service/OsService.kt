package org.n1.mainframe.backend.model.service

import com.fasterxml.jackson.annotation.JsonIgnore
import org.n1.mainframe.backend.model.site.SiteRep
import org.n1.mainframe.backend.model.site.enums.ServiceType
import org.n1.mainframe.backend.model.ui.ValidationException

private const val KEY_NODE_NAME = "nodeName"

class OsService(
        id: String,
        type: ServiceType,
        layer: Int,
        data: MutableMap<String, String>
) : Service(id, type, layer, data) {

    constructor(id: String, defaultName: String) : this(id, ServiceType.OS, 0, HashMap()) {
        setDefaultName(defaultName)
    }

    val nodeName: String
        @JsonIgnore
        get() {
            return data[KEY_NODE_NAME] ?: ""
        }

    private fun valideNetworkId(siteRep: SiteRep) {

        if (siteRep.node.networkId.isBlank()) throw ValidationException("Network Id cannot be empty.")
        val nodesWithSameNetworkId = siteRep.nodes.filter { node ->
            node.networkId === siteRep.node.networkId
        }
        if (nodesWithSameNetworkId.size > 1) throw ValidationException("Duplicate network id: ${siteRep.node.networkId}")
    }

    override fun validationMethods(): Collection<(siteRep: SiteRep) -> Unit> {
        return listOf(::valideNetworkId)
    }
}