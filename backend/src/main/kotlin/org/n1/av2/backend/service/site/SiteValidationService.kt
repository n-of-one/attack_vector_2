package org.n1.av2.backend.service.site

import org.n1.av2.backend.model.SiteRep
import org.n1.av2.backend.model.db.site.Node
import org.n1.av2.backend.model.db.site.SiteData
import org.n1.av2.backend.model.db.site.SiteStateMessage
import org.n1.av2.backend.model.db.site.SiteStateMessageType
import org.n1.av2.backend.model.ui.ValidationException
import org.n1.av2.backend.model.ui.ReduxActions
import org.n1.av2.backend.service.StompService
import org.springframework.stereotype.Service

@Service
class SiteValidationService(
        val stompService: StompService,
        val siteDataService: SiteDataService,
        val nodeService: NodeService,
        val siteStateService: SiteStateService
) {

    fun validate(id: String) {
        val messages = ArrayList<SiteStateMessage>()
        val siteData = siteDataService.getBySiteId(id)
        val nodes = nodeService.getAll(id)

        validateSiteData(siteData, nodes, messages)
        validateNodes(siteData, nodes, messages)

        processValidationMessages(messages, id)
    }

    private fun validateSiteData(siteData: SiteData, nodes: List<Node>, messages: MutableList<SiteStateMessage>) {
        validate(messages) { validateHackTime(siteData) }
        validate(messages) { validateSiteName(siteData)}
        validate(messages) { validateStartNode(siteData, nodes) }
    }

    private fun validateSiteName(siteData: SiteData) {
        if (siteData.name.length > 40) {
            throw ValidationException("Site name too long to be displayed nicely.")
        }
    }

    private fun validateHackTime(data: SiteData) {
        val errorText = "time must be in the format (minutes):(seconds) for example: 12:00 "
        val parts = data.hackTime.split(":")
        if (parts.size != 2) throw ValidationException(errorText)
        parts[0].toIntOrNull() ?: throw ValidationException(errorText)
        parts[1].toIntOrNull() ?: throw ValidationException(errorText)
    }


    private fun validateStartNode(data: SiteData, nodes: List<Node>) {
        if (data.startNodeNetworkId.isBlank()) {
            throw ValidationException("Set the start node network id.")
        }
        nodes.find { node -> node.networkId == data.startNodeNetworkId }
                ?: throw ValidationException("Start node network id not found: ${data.startNodeNetworkId}.")
    }

    private fun validateNodes(siteData: SiteData, nodes: List<Node>, messages: MutableList<SiteStateMessage>) {
        if (nodes.isEmpty()) return

        val siteRep = SiteRep(nodes[0], nodes, siteData)

        val networkIds = HashSet<String>()

        siteRep.nodes.forEach { node ->
            siteRep.node = node
            validateNetworkId(node, networkIds, messages)
            validateServices(node, siteRep, messages)
        }
    }

    private fun validateNetworkId(node: Node, networkIds: MutableSet<String>, messages: MutableList<SiteStateMessage>) {
        val networkId = node.networkId.toLowerCase()
        if (networkIds.contains(networkId)) {
            val message = SiteStateMessage(SiteStateMessageType.ERROR, "Duplicate network id: ${networkId}", node.id, node.services[0].id)
            messages.add(message)
        }
        else {
            networkIds.add(networkId)
        }
    }

    private fun validateServices(node: Node, siteRep: SiteRep, messages: MutableList<SiteStateMessage>) {
        node.services.forEach { service ->
            service.allValidationMethods().forEach { validateMethod ->
                try {
                    validateMethod(siteRep)
                } catch (exception: ValidationException) {
                    val message = SiteStateMessage(SiteStateMessageType.ERROR, exception.errorMessage, node.id, service.id)
                    messages.add(message)
                }
            }
        }
    }

    private fun validate(messages: MutableList<SiteStateMessage>, method: () -> Unit) {
        try {
            method()
        } catch (exception: ValidationException) {
            val message = SiteStateMessage(SiteStateMessageType.ERROR, exception.errorMessage)
            messages.add(message)
        }
    }

    private fun processValidationMessages(messages: ArrayList<SiteStateMessage>, id: String) {
        val ok = messages.filter { it.type == SiteStateMessageType.ERROR }.none()
        val oldState = siteStateService.getById(id)
        val newState = oldState.copy(ok = ok, messages = messages)

        if (oldState != newState) {
            siteStateService.save(newState)
            stompService.toSite(id, ReduxActions.SERVER_UPDATE_SITE_STATE, newState)
        }
    }
}