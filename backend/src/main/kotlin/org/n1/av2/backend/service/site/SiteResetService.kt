package org.n1.av2.backend.service.site

import org.n1.av2.backend.engine.CalledBySystem
import org.n1.av2.backend.engine.TaskEngine
import org.n1.av2.backend.engine.TaskIdentifiers
import org.n1.av2.backend.entity.service.TimerEntityService
import org.n1.av2.backend.entity.site.NodeEntityService
import org.n1.av2.backend.entity.site.SitePropertiesEntityService
import org.n1.av2.backend.entity.site.layer.ice.IceLayer
import org.n1.av2.backend.entity.site.layer.other.KeyStoreLayer
import org.n1.av2.backend.entity.site.layer.other.TripwireLayer
import org.n1.av2.backend.model.ui.ServerActions
import org.n1.av2.backend.service.layerhacking.ice.IceService
import org.n1.av2.backend.service.layerhacking.service.KeystoreService
import org.n1.av2.backend.service.run.RunService
import org.n1.av2.backend.service.util.StompService
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
    private val stompService: StompService,
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
                        stompService.toSite(siteId, ServerActions.SERVER_COMPLETE_TIMER, "timerId" to timer.id)
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
        stompService.toSite(siteId, ServerActions.SERVER_SITE_SHUTDOWN_FINISH)
    }

    fun shutdownSite(siteId: String, shutdownEndTime: ZonedDateTime) {
        val properties = sitePropertiesEntityService.getBySiteId(siteId)
        val shutdownProperties = properties.copy(shutdownEnd = shutdownEndTime)
        sitePropertiesEntityService.save(shutdownProperties)
        stompService.toSite(siteId, ServerActions.SERVER_SITE_SHUTDOWN_START)
    }
}
