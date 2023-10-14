package org.n1.av2.backend.service.site

import org.n1.av2.backend.entity.site.*
import org.n1.av2.backend.model.SiteRep
import org.n1.av2.backend.model.ui.ServerActions
import org.n1.av2.backend.model.ui.ValidationException
import org.n1.av2.backend.service.StompService
import org.springframework.stereotype.Service
import java.time.Duration

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
        data.hackTime.toDuration("Hack time")
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
            stompService.toSite(id, ServerActions.SERVER_UPDATE_SITE_STATE, newState)
        }
    }
}

fun String.toDuration(type: String): Duration {
    val errorText = "${type} time must be \"0\" or in the format (hours):(minutes):(seconds) or (minutes):(second) for example: 0:12:00 or 12:00"
    if (this.trim() == "0") return Duration.ZERO

    val parts = this.split(":")

    if (parts.size < 2 || parts.size > 3) throw ValidationException(errorText)

    val normalizedParts = if (parts.size == 3) parts else listOf("0") + parts

    val hours = normalizedParts[0].toLongOrNull() ?: throw ValidationException(errorText)
    val minutes = normalizedParts[1].toLongOrNull() ?: throw ValidationException(errorText)
    val seconds = normalizedParts[2].toLongOrNull() ?: throw ValidationException(errorText)

    if (hours < 0 || minutes < 0 || seconds <0) {
        throw ValidationException("${type} time must be positive.")
    }

    return Duration.ofHours(hours).plusMinutes(minutes).plusSeconds(seconds)

}