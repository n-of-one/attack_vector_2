package org.n1.mainframe.backend.model.service

import com.fasterxml.jackson.annotation.JsonIgnore
import org.n1.mainframe.backend.model.site.Service
import org.n1.mainframe.backend.model.site.SiteRep
import org.n1.mainframe.backend.model.site.enums.ServiceType
import org.n1.mainframe.backend.model.ui.ValidationException

const val KEY_NAME = "name"
const val KEY_GM_NOTE = "gmNote"

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

    val gmNote: String
        @JsonIgnore
        get() {
            return data[KEY_GM_NOTE] ?: ""
        }

    private fun valideNetworkId(siteRep: SiteRep) {

        if (siteRep.node.networkId.isBlank()) throw ValidationException("Network Id cannot be empty.")
        val nodesWithSameNetworkId = siteRep.nodes.filter{ node ->
            node.networkId === siteRep.node.networkId
        }
        if (nodesWithSameNetworkId.size > 1) throw ValidationException("Duplicate network id: ${siteRep.node.networkId}")
    }
//
//    private fun validateName(siteRep: SiteRep) {
//        if (this.name.isEmpty()) throw ValidationException("Name cannot be empty.")
//    }
//
//    private fun validateGmNote(siteRep: SiteRep) {
//        if (this.gmNote.isEmpty()) throw ValidationException("Gm Note  cannot be empty.")
//    }

    override fun validationMethods(): Collection<(siteRep: SiteRep) -> Unit> {
        return  listOf(::valideNetworkId )
    }
}