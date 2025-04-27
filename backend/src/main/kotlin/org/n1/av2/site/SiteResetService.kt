package org.n1.av2.site

import org.n1.av2.hacker.hackerstate.HackerActivity
import org.n1.av2.hacker.hackerstate.HackerStateEntityService
import org.n1.av2.hacker.skill.SkillService
import org.n1.av2.layer.ice.common.IceLayer
import org.n1.av2.layer.ice.common.IceService
import org.n1.av2.layer.other.keystore.KeyStoreLayer
import org.n1.av2.layer.other.keystore.KeystoreService
import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.platform.connection.ConnectionService.TerminalReceive
import org.n1.av2.platform.connection.ServerActions
import org.n1.av2.platform.connection.ServerActions.SERVER_TERMINAL_RECEIVE
import org.n1.av2.platform.engine.CalledBySystem
import org.n1.av2.platform.engine.TaskEngine
import org.n1.av2.platform.util.toHumanTime
import org.n1.av2.run.RunService
import org.n1.av2.run.terminal.TERMINAL_MAIN
import org.n1.av2.site.entity.NodeEntityService
import org.n1.av2.site.entity.SitePropertiesEntityService
import org.n1.av2.statistics.IceStatisticsService
import org.n1.av2.timer.TimerService
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.ZonedDateTime
import javax.annotation.PostConstruct

// To prevent circular bean dependencies
@Configuration
class InitSiteResetService(
    val siteResetService: SiteResetService,
    val runService: RunService,
    val timerService: TimerService,
) {

    @PostConstruct
    fun postConstruct() {
        siteResetService.runService = runService
        siteResetService.timerService = timerService
    }
}

@Service
class SiteResetService(
    private val taskEngine: TaskEngine,
    private val nodeEntityService: NodeEntityService,
    private val keystoreService: KeystoreService,
    private val connectionService: ConnectionService,
    private val iceService: IceService,
    private val sitePropertiesEntityService: SitePropertiesEntityService,
    private val hackerStateEntityService: HackerStateEntityService,
    private val iceStatisticsService: IceStatisticsService,
    private val skillService: SkillService,
) {

    lateinit var runService: RunService
    lateinit var timerService: TimerService

    fun refreshSite(siteId: String) {
        resetSite(siteId, Duration.ZERO)
        shutdownFinished(siteId)
        timerService.removeTimersForTargetSite(siteId)
    }

    @CalledBySystem
    fun resetSite(siteId: String, shutdownDuration: Duration) {
        taskEngine.removeAll(mapOf("siteId" to siteId))
        val nodes = nodeEntityService.findBySiteId(siteId)
        nodes.forEach { node ->
            node.layers.forEach { layer ->
                if (layer is IceLayer) {
                    layer.hacked = false
                }
                if (layer is KeyStoreLayer) {
                    keystoreService.deleteIcePassword(layer.iceLayerId)
                }
            }
            nodeEntityService.save(node)
        }
        iceService.resetIceForSite(siteId)

        timerService.removeTimersForTargetSite(siteId)

        runService.updateRunLinksForResetSite(siteId)

        iceStatisticsService.siteReset(siteId)

        skillService.resetSite(siteId)

        val hackerStates = hackerStateEntityService.findAllHackersInSite(siteId)
        connectionService.toSite(
            siteId,
            SERVER_TERMINAL_RECEIVE,
            TerminalReceive(TERMINAL_MAIN, arrayOf("[info]Site down for maintenance for ${shutdownDuration.toHumanTime()}."))
        )
        hackerStates
            .filter { it.activity == HackerActivity.INSIDE }
            .forEach { hackerState ->
                runService.hackerDisconnect(hackerState, "Disconnected (server abort)")
            }


    }

    fun shutdownFinished(siteId: String) {
        val properties = sitePropertiesEntityService.getBySiteId(siteId)
        val shutdownProperties = properties.copy(
            shutdownEnd = null,
            alertnessTimerAdjustment = null,
        )
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
