package org.n1.mainframe.backend.service.site

import org.n1.mainframe.backend.model.site.*
import org.n1.mainframe.backend.model.ui.ValidationException
import org.n1.mainframe.backend.service.ReduxActions
import org.n1.mainframe.backend.service.StompService
import org.springframework.stereotype.Service

@Service
class SiteValidationService(
        val stompService: StompService,
        val layoutService: LayoutService,
        val siteDataService: SiteDataService,
        val nodeService: NodeService,
        val connectionService: ConnectionService,
        val siteStateService: SiteStateService
) {

    fun validate(id: String) {
        val messages = ArrayList<SiteStateMessage>()
        val siteData = siteDataService.getById(id)
        val nodes = nodeService.getAll(id)

        validateSiteData(messages, siteData, nodes)
        validateNodes(messages, siteData, nodes)

        processValidationMessages(messages, id)
    }

    private fun validateSiteData(messages: MutableList<SiteStateMessage>, siteData: SiteData, nodes: List<Node>) {
        validate(messages) { validateHackTime(siteData) }
        validate(messages) { validateStartNode(siteData, nodes) }
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

    private fun validateNodes(messages: MutableList<SiteStateMessage>, siteData: SiteData, nodes: List<Node>) {
        if (nodes.isEmpty()) return

        val siteRep = SiteRep(nodes[0], nodes, siteData)

        siteRep.nodes.forEach { node ->
            siteRep.node = node
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