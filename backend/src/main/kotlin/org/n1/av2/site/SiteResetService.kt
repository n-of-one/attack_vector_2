package org.n1.av2.site

import org.n1.av2.layer.ice.common.IceLayer
import org.n1.av2.layer.ice.common.IceService
import org.n1.av2.layer.other.keystore.KeyStoreLayer
import org.n1.av2.layer.other.keystore.KeystoreService
import org.n1.av2.layer.other.tripwire.TimerEntityService
import org.n1.av2.layer.other.tripwire.TripwireLayer
import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.platform.connection.ServerActions
import org.n1.av2.platform.engine.CalledBySystem
import org.n1.av2.platform.engine.TaskEngine
import org.n1.av2.platform.engine.TaskIdentifiers
import org.n1.av2.run.RunService
import org.n1.av2.site.entity.NodeEntityService
import org.n1.av2.site.entity.SitePropertiesEntityService
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Service
import java.time.ZonedDateTime
import javax.annotation.PostConstruct

// To prevent circular bean dependencies
@Configuration
class InitSiteResetService(
    val siteResetService: SiteResetService,
    val runService: RunService
) {

    @PostConstruct
    fun postConstruct() {
        siteResetService.runService = runService
    }
}

@Service
class SiteResetService(
    private val taskEngine: TaskEngine,
    private val timerEntityService: TimerEntityService,
    private val nodeEntityService: NodeEntityService,
    private val keystoreService: KeystoreService,
    private val connectionService: ConnectionService,
    private val iceService: IceService,
    private val sitePropertiesEntityService: SitePropertiesEntityService,
) {

    lateinit var runService: RunService

    fun refreshSite(siteId: String, shutdownDuration: String) {
        resetSite(siteId)
        shutdownFinished(siteId)
        timerEntityService.deleteBySiteId(siteId)
    }

    @CalledBySystem
    fun resetSite(siteId: String) {
        taskEngine.removeAll(TaskIdentifiers(null, siteId, null))
        val nodes = nodeEntityService.getAll(siteId)
        nodes.forEach { node ->
            node.layers.forEach { layer ->
                if (layer is IceLayer) {
                    layer.hacked = false
                }
                if (layer is KeyStoreLayer) {
                    keystoreService.deleteIcePassword(layer.iceLayerId)
                }
                if (layer is TripwireLayer) {
                    val timer = timerEntityService.deleteByLayerId(layer.id)
                    if (timer != null) {
                        connectionService.toSite(siteId, ServerActions.SERVER_COMPLETE_TIMER, "timerId" to timer.id)
                    }
                }
            }
            nodeEntityService.save(node)
        }
        iceService.deleteIce(siteId)

        runService.updateRunLinksForResetSite(siteId)
    }

    fun shutdownFinished(siteId: String) {
        val properties = sitePropertiesEntityService.getBySiteId(siteId)
        val shutdownProperties = properties.copy(shutdownEnd = null)
        sitePropertiesEntityService.save(shutdownProperties)
        connectionService.toSite(siteId, ServerActions.SERVER_SITE_SHUTDOWN_FINISH)
    }

    fun shutdownSite(siteId: String, shutdownEndTime: ZonedDateTime) {
        val properties = sitePropertiesEntityService.getBySiteId(siteId)
        val shutdownProperties = properties.copy(shutdownEnd = shutdownEndTime)
        sitePropertiesEntityService.save(shutdownProperties)
        connectionService.toSite(siteId, ServerActions.SERVER_SITE_SHUTDOWN_START)
    }
}
