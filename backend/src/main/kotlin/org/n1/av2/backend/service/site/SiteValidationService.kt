package org.n1.av2.backend.service.site

import org.n1.av2.backend.entity.site.*
import org.n1.av2.backend.model.SiteRep
import org.n1.av2.backend.model.ui.ReduxActions
import org.n1.av2.backend.model.ui.ValidationException
import org.n1.av2.backend.service.StompService
import org.springframework.stereotype.Service

@Service
class SiteValidationService(
    val stompService: StompService,
    val sitePropertiesEntityService: SitePropertiesEntityService,
    val nodeEntityService: NodeEntityService,
    val siteEditorStateEntityService: SiteEditorStateEntityService
) {

    fun validate(id: String) {
        val messages = ArrayList<SiteStateMessage>()
        val siteProperties = sitePropertiesEntityService.getBySiteId(id)
        val nodes = nodeEntityService.getAll(id)

        validateSiteProperties(siteProperties, nodes, messages)
        validateNodes(siteProperties, nodes, messages)

        processValidationMessages(messages, id)
    }

    private fun validateSiteProperties(siteProperties: SiteProperties, nodes: List<Node>, messages: MutableList<SiteStateMessage>) {
        validate(messages) { validateHackTime(siteProperties) }
        validate(messages) { validateSiteName(siteProperties)}
        validate(messages) { validateStartNode(siteProperties, nodes) }
    }

    private fun validateSiteName(siteProperties: SiteProperties) {
        if (siteProperties.name.length > 40) {
            throw ValidationException("Site name too long to be displayed nicely.")
        }
    }

    private fun validateHackTime(data: SiteProperties) {
        val errorText = "time must be in the format (minutes):(seconds) for example: 12:00 "
        val parts = data.hackTime.split(":")
        if (parts.size != 2) throw ValidationException(errorText)
        parts[0].toIntOrNull() ?: throw ValidationException(errorText)
        parts[1].toIntOrNull() ?: throw ValidationException(errorText)
    }


    private fun validateStartNode(data: SiteProperties, nodes: List<Node>) {
        if (data.startNodeNetworkId.isBlank()) {
            throw ValidationException("Set the start node network id.")
        }
        nodes.find { node -> node.networkId == data.startNodeNetworkId }
                ?: throw ValidationException("Start node network id not found: ${data.startNodeNetworkId}.")
    }

    private fun validateNodes(siteProperties: SiteProperties, nodes: List<Node>, messages: MutableList<SiteStateMessage>) {
        if (nodes.isEmpty()) return

        val siteRep = SiteRep(nodes[0], nodes, siteProperties)

        val networkIds = HashSet<String>()

        siteRep.nodes.forEach { node ->
            siteRep.node = node
            validateNetworkId(node, networkIds, messages)
            validateServices(node, siteRep, messages)
        }
    }

    private fun validateNetworkId(node: Node, networkIds: MutableSet<String>, messages: MutableList<SiteStateMessage>) {
        val networkId = node.networkId.lowercase()
        if (networkIds.contains(networkId)) {
            val message = SiteStateMessage(SiteStateMessageType.ERROR, "Duplicate network id: ${networkId}", node.id, node.layers[0].id)
            messages.add(message)
        }
        else {
            networkIds.add(networkId)
        }
    }

    private fun validateServices(node: Node, siteRep: SiteRep, messages: MutableList<SiteStateMessage>) {
        node.layers.forEach { service ->
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
        val oldState = siteEditorStateEntityService.getById(id)
        val newState = oldState.copy(ok = ok, messages = messages)

        if (oldState != newState) {
            siteEditorStateEntityService.save(newState)
            stompService.toSite(id, ReduxActions.SERVER_UPDATE_SITE_STATE, newState)
        }
    }
}