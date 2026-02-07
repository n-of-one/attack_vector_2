package org.n1.av2.editor

import org.n1.av2.platform.config.ConfigItem
import org.n1.av2.platform.config.ConfigService
import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.platform.connection.ServerActions
import org.n1.av2.platform.iam.user.CurrentUserService
import org.n1.av2.platform.iam.user.UserType
import org.n1.av2.run.scanning.TraverseNode
import org.n1.av2.run.scanning.TraverseNodeService
import org.n1.av2.site.entity.*
import org.springframework.stereotype.Service

@Service
class SiteValidationService(
    private val connectionService: ConnectionService,
    private val sitePropertiesEntityService: SitePropertiesEntityService,
    private val nodeEntityService: NodeEntityService,
    private val siteEditorStateEntityService: SiteEditorStateEntityService,
    private val currentUserService: CurrentUserService,
    private val traverseNodeService: TraverseNodeService,
    private val sitePropertiesRepo: SitePropertiesRepo,
    private val nodeRepo: NodeRepo,
    private val configService: ConfigService,
) {

    fun validate(siteId: String): List<SiteStateMessage> {
        return validate(siteId, false)
    }

    fun validate(siteId: String, alwaysPersistResult: Boolean): List<SiteStateMessage> {
        val messages = ArrayList<SiteStateMessage>()
        val siteProperties = sitePropertiesEntityService.getBySiteId(siteId)
        val nodes = nodeEntityService.findBySiteId(siteId)
        val traverseNodesById = traverseNodeService.createTraverseNodes(siteId, nodes)

        validateSiteProperties(siteProperties, nodes, messages)
        validateNodes(siteProperties, nodes, messages)
        validateConnections(siteProperties, nodes, traverseNodesById, messages)

        processValidationMessages(messages, siteId, alwaysPersistResult)
        return messages
    }


    private fun validateSiteProperties(siteProperties: SiteProperties, nodes: List<Node>, messages: MutableList<SiteStateMessage>) {
        validate(messages) { validateSiteName(siteProperties)}
        validate(messages) { validateStartNode(siteProperties, nodes) }
        validate(messages) { validateSiteCreator(siteProperties) }
    }

    private fun validateSiteName(siteProperties: SiteProperties) {
        if (siteProperties.name.length > 40) {
            throw SiteValidationException("Site name too long to be displayed nicely.", SiteStateMessageType.INFO)
        }
    }

    private fun validateSiteCreator(siteProperties: SiteProperties) {
        if (siteProperties.purpose.isBlank() && currentUserService.userEntity.type != UserType.HACKER) {
            throw SiteValidationException("Please fill in the site's purpose.", SiteStateMessageType.INFO)
        }
    }

    private fun validateStartNode(data: SiteProperties, nodes: List<Node>) {
        if (data.startNodeNetworkId.isBlank()) {
            throw SiteValidationException("Set the start node network id.")
        }
        nodes.find { node -> node.networkId == data.startNodeNetworkId }
                ?: throw SiteValidationException("Start node network id not found: ${data.startNodeNetworkId}.")
    }

    private fun validateNodes(siteProperties: SiteProperties, nodes: List<Node>, messages: MutableList<SiteStateMessage>) {
        if (nodes.isEmpty()) return

        val minimumShutdownDuration = configService.getAsDuration(ConfigItem.DEV_MINIMUM_SHUTDOWN_DURATION)
        val validationContext = ValidationContext(nodes[0], nodes, siteProperties, sitePropertiesRepo, nodeRepo, minimumShutdownDuration)

        val networkIds = HashSet<String>()

        validationContext.nodes.forEach { node ->
            validationContext.node = node
            validateNetworkId(node, networkIds, messages)
            validateServices(node, validationContext, messages)
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

    private fun validateServices(node: Node, validationContext: ValidationContext, messages: MutableList<SiteStateMessage>) {
        node.layers.forEach { service ->
            service.allValidationMethods().forEach { validateMethod ->
                try {
                    validateMethod(validationContext)
                } catch (exception: SiteValidationException) {
                    val message = SiteStateMessage(exception.type, exception.message!!, node.id, service.id)
                    messages.add(message)
                }
            }
        }
    }

    private fun validate(messages: MutableList<SiteStateMessage>, method: () -> Unit) {
        try {
            method()
        } catch (exception: SiteValidationException) {
            val message = SiteStateMessage(exception.type, exception.message!!)
            messages.add(message)
        }
    }

    private fun validateConnections(siteProperties: SiteProperties, nodes: List<Node>, traverseNodesById: Map<String, TraverseNode>, messages: MutableList<SiteStateMessage> ) {
        val startNode = nodes.find { it.networkId == siteProperties.startNodeNetworkId } ?: return // start network-id invalid, another validation will report this.
        val startTraverseNode = traverseNodesById[startNode.id]!!

        startTraverseNode.fillDistanceFromHere(0)
        val unreachableNodes = traverseNodesById.values.filter { it.distance == null }
        unreachableNodes.forEach { node ->
            val osLayer = nodes.find {it.id == node.nodeId}!!.layers[0]
            val message = SiteStateMessage(SiteStateMessageType.ERROR, "Unreachable node: ${node.networkId}", node.nodeId, osLayer.id )
            messages.add(message)
        }
    }



    private fun processValidationMessages(messages: ArrayList<SiteStateMessage>, id: String, alwaysPersistResult: Boolean) {
        val ok = messages.none { it.type == SiteStateMessageType.ERROR }
        val oldState = siteEditorStateEntityService.getById(id)
        val newState = oldState.copy(messages = messages)

        if (alwaysPersistResult || oldState != newState) {
            siteEditorStateEntityService.save(newState)
            connectionService.toSite(id, ServerActions.SERVER_UPDATE_SITE_STATE, newState)
            val updatedSiteProperties = sitePropertiesEntityService.updateSiteOk(id, ok)
            connectionService.toSite(id, ServerActions.SERVER_UPDATE_SITE_DATA, updatedSiteProperties)
        }
    }
}



class SiteValidationException(message: String, val type: SiteStateMessageType = SiteStateMessageType.ERROR) : Exception(message)
