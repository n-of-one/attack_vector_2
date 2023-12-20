package org.n1.av2.backend.service.site

import org.n1.av2.backend.engine.CalledBySystem
import org.n1.av2.backend.engine.TaskEngine
import org.n1.av2.backend.engine.TaskIdentifiers
import org.n1.av2.backend.entity.service.TimerEntityService
import org.n1.av2.backend.entity.site.*
import org.n1.av2.backend.entity.site.layer.ice.IceLayer
import org.n1.av2.backend.entity.site.layer.other.KeyStoreLayer
import org.n1.av2.backend.entity.site.layer.other.TripwireLayer
import org.n1.av2.backend.model.iam.UserPrincipal
import org.n1.av2.backend.model.ui.ServerActions
import org.n1.av2.backend.model.ui.SiteFull
import org.n1.av2.backend.service.layerhacking.ice.IceService
import org.n1.av2.backend.service.layerhacking.service.KeystoreService
import org.n1.av2.backend.service.util.StompService
import org.n1.av2.backend.util.ServerFatal
import org.springframework.stereotype.Service
import java.time.ZonedDateTime

@Service
class SiteService(
    private val stompService: StompService,
    private val layoutEntityService: LayoutEntityService,
    private val sitePropertiesEntityService: SitePropertiesEntityService,
    private val nodeEntityService: NodeEntityService,
    private val connectionEntityService: ConnectionEntityService,
    private val siteEditorStateEntityService: SiteEditorStateEntityService,
    private val iceService: IceService,
    private val keystoreService: KeystoreService,
    private val timerEntityService: TimerEntityService,
    private val taskEngine: TaskEngine,
) {

    fun createSite(name: String): String {
        val id = sitePropertiesEntityService.createId()
        sitePropertiesEntityService.create(id, name)
        layoutEntityService.create(id)
        siteEditorStateEntityService.create(id)
        return id
    }


    fun getSiteFull(siteId: String): SiteFull {
        val siteProperties = sitePropertiesEntityService.getBySiteId(siteId)
        val layout = layoutEntityService.getBySiteId(siteId)
        val nodes = nodeEntityService.getAll(siteId).toMutableList()
        val startNodeId = findStartNode(siteProperties.startNodeNetworkId, nodes)?.id
        val connections = connectionEntityService.getAll(siteId)
        val state = siteEditorStateEntityService.getById(siteId)

        return SiteFull(siteId, siteProperties, layout, nodes, connections, state, startNodeId)
    }

    fun findStartNode(startNodeNetworkId: String, nodes: List<Node>): Node? {
        return nodes.find { node -> node.networkId == startNodeNetworkId }
    }

    fun removeSite(siteId: String, userPrincipal: UserPrincipal) {
        stompService.toSite(siteId, ServerActions.SERVER_ERROR, ServerFatal(false, "Site removed by ${userPrincipal.userEntity.name}, please close browser window."))
        sitePropertiesEntityService.delete(siteId)
        layoutEntityService.delete(siteId)
        nodeEntityService.deleteAllForSite(siteId)
        connectionEntityService.deleteAllForSite(siteId)
        siteEditorStateEntityService.delete(siteId)

        iceService.deleteIce(siteId)
    }

    fun refreshSite(siteId: String, shutdownDuration: String) {
        resetSite(siteId)
        shutdownFinished(siteId)
        timerEntityService.deleteBySiteId(siteId)
    }

    @CalledBySystem
    fun resetSite(siteId: String) {
        taskEngine.removeAll(TaskIdentifiers(null, siteId, null))
        val nodes = nodeEntityService.getAll(siteId)
        nodes.forEach {node ->
            node.layers.forEach { layer ->
                if (layer is IceLayer) { layer.hacked = false }
                if (layer is KeyStoreLayer) { keystoreService.deleteIcePassword(layer.iceLayerId) }
                if (layer is TripwireLayer) {
                    val timer = timerEntityService.deleteByLayerId(layer.id)
                    if (timer != null) {
                        stompService.toSite(siteId, ServerActions.SERVER_COMPLETE_TIMER, "timerId" to timer.id)
                    }
                }
            }
            nodeEntityService.save(node)
        }
        iceService.deleteIce(siteId)
    }

    fun shutdownSite(siteId: String, shutdownEndTime: ZonedDateTime) {
        val properties = sitePropertiesEntityService.getBySiteId(siteId)
        val shutdownProperties = properties.copy(shutdownEnd = shutdownEndTime)
        sitePropertiesEntityService.save(shutdownProperties)
        stompService.toSite(siteId, ServerActions.SERVER_SITE_SHUTDOWN_START)
    }

    fun shutdownFinished(siteId: String) {
        val properties = sitePropertiesEntityService.getBySiteId(siteId)
        val shutdownProperties = properties.copy(shutdownEnd = null)
        sitePropertiesEntityService.save(shutdownProperties)
        stompService.toSite(siteId, ServerActions.SERVER_SITE_SHUTDOWN_FINISH)
    }

    fun findNeighboringNodeIds(node: Node): List<String> {
        val connections = connectionEntityService.findByNodeId(node.id)
        return connections.map { connection ->
            if (connection.fromId == node.id) connection.toId else connection.fromId
        }
    }
}