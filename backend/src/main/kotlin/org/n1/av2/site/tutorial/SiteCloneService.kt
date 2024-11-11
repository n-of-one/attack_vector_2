package org.n1.av2.site.tutorial

import org.n1.av2.editor.*
import org.n1.av2.layer.Layer
import org.n1.av2.layer.app.status_light.StatusLightLayer
import org.n1.av2.layer.ice.netwalk.NetwalkIceLayer
import org.n1.av2.layer.ice.password.PasswordIceLayer
import org.n1.av2.layer.ice.sweeper.SweeperIceLayer
import org.n1.av2.layer.ice.tangle.TangleIceLayer
import org.n1.av2.layer.ice.tar.TarIceLayer
import org.n1.av2.layer.ice.wordsearch.WordSearchIceLayer
import org.n1.av2.layer.other.core.CoreLayer
import org.n1.av2.layer.other.keystore.KeyStoreLayer
import org.n1.av2.layer.other.os.OsLayer
import org.n1.av2.layer.other.text.TextLayer
import org.n1.av2.layer.other.tripwire.TripwireLayer
import org.n1.av2.platform.iam.user.CurrentUserService
import org.n1.av2.platform.iam.user.UserEntity
import org.n1.av2.site.entity.*
import org.springframework.stereotype.Service

@Service
class SiteCloneService(
    private val sitePropertiesEntityService: SitePropertiesEntityService,
    private val nodeEntityService: NodeEntityService,
    private val connectionEntityService: ConnectionEntityService,
    private val siteEditorStateEntityService: SiteEditorStateEntityService,
    private val siteValidationService: SiteValidationService,
    private val currentUserService: CurrentUserService,
) {

    fun cloneSite(sourceSideProperties: SiteProperties, targetSiteName: String, owner: UserEntity): String {
        val sourceSiteId = sourceSideProperties.siteId

        val siteId = cloneSiteProperties(targetSiteName, owner)

        val sourceNodes = nodeEntityService.getAll(sourceSiteId)
        val nodesBySourceId = cloneNodes(sourceNodes, siteId)

        val sourceConnections = connectionEntityService.getAll(sourceSiteId)
        cloneConnections(sourceConnections, siteId, nodesBySourceId)

        createEditorState(siteId)
        return siteId
    }

    private fun createEditorState(siteId: String): List<SiteStateMessage> {
        siteEditorStateEntityService.create(siteId)
        return siteValidationService.validate(siteId)
    }

    private fun cloneSiteProperties(targetSiteName: String, owner: UserEntity): String {
        val siteId = sitePropertiesEntityService.createId()
        val siteProperties = SiteProperties(
            siteId = siteId,
            name = targetSiteName,
            description = "Tutorial",
            purpose = "tutorial",
            startNodeNetworkId = "00",
            ownerUserId = owner.id,
            hackable = true,
        )
        sitePropertiesEntityService.save(siteProperties)
        return siteId
    }

    private fun cloneNodes(sourceNodes: List<Node>, siteId: String): Map<String, Node> {
        val nodesBySourceId = HashMap<String, Node>()
        val layersBySourceLayerId = HashMap<String, Layer>()

        sourceNodes.forEach { sourceNode ->
            val nodeData = AddNode(
                siteId = siteId,
                type = sourceNode.type,
                x = sourceNode.x,
                y = sourceNode.y,
                networkId = sourceNode.networkId,
            )
            val targetNode = nodeEntityService.createNode(nodeData)
            targetNode.layers.clear() // remove auto created OS layer
            nodesBySourceId[sourceNode.id] = targetNode
        }

        sourceNodes.forEach { sourceNode ->
            sourceNode.layers.forEach { sourceLayer ->
                val targetNode = nodesBySourceId[sourceNode.id]!!

                val targetLayer = createLayerCopy(sourceLayer, targetNode)
                targetNode.layers.add(targetLayer)
                layersBySourceLayerId[sourceLayer.id] = targetLayer
            }
        }

        layersBySourceLayerId.values.forEach { layer ->
            updateLayersDependingOnOtherLayers(layer, layersBySourceLayerId)
        }

        nodesBySourceId.values.forEach { nodeEntityService.save(it) }
        return nodesBySourceId
    }

    private fun cloneConnections(
        sourceConnections: List<Connection>, siteId: String, nodesBySourceId: Map<String, Node>
    ) {
        sourceConnections.forEach { sourceConnection ->
            val addConnection = AddConnection(
                siteId = siteId,
                fromId = nodesBySourceId[sourceConnection.fromId]!!.id,
                toId = nodesBySourceId[sourceConnection.toId]!!.id,
            )
            connectionEntityService.createConnection(addConnection)
        }
    }

    private fun createLayerCopy(sourceLayer: Layer, targetNode: Node): Layer {
        val id = nodeEntityService.createLayerId(targetNode)

        return when (sourceLayer) {
            is OsLayer -> OsLayer(nodeEntityService.createOsLayerId(targetNode.id), sourceLayer)
            is TextLayer -> TextLayer(id, sourceLayer)
            is CoreLayer -> CoreLayer(id, sourceLayer)
            is KeyStoreLayer -> KeyStoreLayer(id, sourceLayer)
            is StatusLightLayer -> StatusLightLayer(id, sourceLayer)
            is TripwireLayer -> TripwireLayer(id, sourceLayer)
            is NetwalkIceLayer -> NetwalkIceLayer(id, sourceLayer)
            is PasswordIceLayer -> PasswordIceLayer(id, sourceLayer)
            is TangleIceLayer -> TangleIceLayer(id, sourceLayer)
            is TarIceLayer -> TarIceLayer(id, sourceLayer)
            is WordSearchIceLayer -> WordSearchIceLayer(id, sourceLayer)
            is SweeperIceLayer -> SweeperIceLayer(id, sourceLayer)
            else -> error("Unknown layer type: ${sourceLayer.type}, ${sourceLayer.javaClass}")
        }
    }

    private fun updateLayersDependingOnOtherLayers(layer: Layer, layersBySourceLayerId: Map<String, Layer>) {
        if (layer is TripwireLayer && layer.coreLayerId != null) {
            layer.coreLayerId = layersBySourceLayerId[layer.coreLayerId]?.id
        }
        if (layer is KeyStoreLayer && layer.iceLayerId != null) {
            layer.iceLayerId = layersBySourceLayerId[layer.iceLayerId]?.id
        }
    }
}
