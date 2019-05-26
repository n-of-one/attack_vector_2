package org.n1.mainframe.backend.model.service

import com.fasterxml.jackson.annotation.JsonIgnore
import org.n1.mainframe.backend.model.site.Service
import org.n1.mainframe.backend.model.site.SiteRep
import org.n1.mainframe.backend.model.site.enums.ServiceType
import org.n1.mainframe.backend.model.ui.ValidationException

private const val KEY_NAME = "name"

class OsService(
        id: String,
        layer: Int = 0,
        type: ServiceType = ServiceType.OS,
        data: MutableMap<String, String> = HashMap(0)
) : Service(id, type, layer, data) {

    val name: String
        @JsonIgnore
        get() {
            return data[KEY_NAME] ?: ""
        }

    private fun valideNetworkId(siteRep: SiteRep) {

        if (siteRep.node.networkId.isBlank()) throw ValidationException("Network Id cannot be empty.")
        val nodesWithSameNetworkId = siteRep.nodes.filter{ node ->
            node.networkId === siteRep.node.networkId
        }
        if (nodesWithSameNetworkId.size > 1) throw ValidationException("Duplicate network id: ${siteRep.node.networkId}")
    }

    override fun validationMethods(): Collection<(siteRep: SiteRep) -> Unit> {
        return  listOf(::valideNetworkId )
    }
}