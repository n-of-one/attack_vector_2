package org.n1.av2.run

import org.n1.av2.hacker.hackerstate.HackerActivity
import org.n1.av2.hacker.hackerstate.HackerState
import org.n1.av2.hacker.hackerstate.HackerStateEntityService
import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.platform.connection.ServerActions
import org.n1.av2.platform.engine.ScheduledTask
import org.n1.av2.platform.engine.UserTaskRunner
import org.n1.av2.run.entity.RunEntityService
import org.n1.av2.run.terminal.SyntaxHighlightingService
import org.n1.av2.run.terminal.inside.CommandMoveService
import org.n1.av2.run.timings.Timings
import org.n1.av2.run.timings.TimingsService
import org.n1.av2.site.entity.SiteProperties
import org.n1.av2.site.entity.SitePropertiesEntityService
import org.springframework.stereotype.Service

class UplinkConnectInfo(val sourceSiteName: String, val targetSiteName: String)

/**
 * Connects a hacker that is outside a site to its ingress node: the start node when attacking, the target node of an uplink.
 */
@Service
class RunIngressService(
    private val runEntityService: RunEntityService,
    private val runService: RunService,
    private val hackerStateEntityService: HackerStateEntityService,
    private val sitePropertiesEntityService: SitePropertiesEntityService,
    private val connectionService: ConnectionService,
    private val userTaskRunner: UserTaskRunner,
    private val timingsService: TimingsService,
    private val syntaxHighlightingService: SyntaxHighlightingService,
    private val commandMoveService: CommandMoveService,
) {

    fun connect(hackerState: HackerState, ingressNodeId: String, quick: Boolean, uplink: UplinkConnectInfo?) {
        val userId = hackerState.userId
        val run = runEntityService.getByRunId(hackerState.runId ?: error("Hacker ${userId} connects without run"))

        runService.revealNode(run, ingressNodeId)
        hackerStateEntityService.startConnecting(hackerState, ingressNodeId)

        val baseTimings = when {
            uplink != null -> timingsService.START_UPLINK
            quick -> timingsService.START_ATTACK_FAST
            else -> timingsService.START_ATTACK_SLOW
        }
        val timings = timingsService.skillAndConfigAdjusted(baseTimings, userId)

        @Suppress("unused")
        class StartAttack(val userId: String, val nodeId: String, val quick: Boolean, val timings: Timings, val uplink: UplinkConnectInfo?)
        connectionService.toRun(run.runId, ServerActions.SERVER_HACKER_START_ATTACK, StartAttack(userId, ingressNodeId, quick, timings, uplink))

        userTaskRunner.queueInTicksForSite("ingress-arrive", run.siteId, timings.ticksFor("main")) {
            ingressArrive(userId, run.runId, ingressNodeId)
        }
    }

    @ScheduledTask
    fun ingressArrive(userId: String, runId: String, ingressNodeId: String) {
        val hackerState = hackerStateEntityService.retrieveForUserId(userId)
        if (hackerState.runId != runId || hackerState.activity != HackerActivity.OUTSIDE || hackerState.ingressNodeId != ingressNodeId) {
            return // left the run or was disconnected during the connection sequence
        }

        val refusal = connectionRefusalReason(sitePropertiesEntityService.getBySiteId(hackerState.siteId!!))
        if (refusal != null) {
            runService.hackerDisconnect(hackerState, refusal)
            return
        }

        syntaxHighlightingService.sendForInside()
        hackerStateEntityService.arriveAtIngress(hackerState)
        commandMoveService.moveArrive(ingressNodeId, userId, runId)
    }
}

fun connectionRefusalReason(site: SiteProperties): String? {
    if (!site.hackable || !site.siteStructureOk) return "[warn]Connection refused.[/] [info]${site.name}[/] is not accepting connections."
    if (site.shutdownEnd != null) return "[warn]Connection refused.[/] [info]${site.name}[/] is shut down."
    return null
}
