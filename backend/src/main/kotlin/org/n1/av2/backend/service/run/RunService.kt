package org.n1.av2.backend.service.run

import org.n1.av2.backend.engine.TimedTaskRunner
import org.n1.av2.backend.entity.run.*
import org.n1.av2.backend.entity.site.NodeEntityService
import org.n1.av2.backend.entity.site.enums.LayerType
import org.n1.av2.backend.entity.site.layer.IceTangleLayer
import org.n1.av2.backend.entity.site.layer.Layer
import org.n1.av2.backend.entity.user.HackerIcon
import org.n1.av2.backend.entity.user.User
import org.n1.av2.backend.entity.user.UserEntityService
import org.n1.av2.backend.model.ui.ReduxActions
import org.n1.av2.backend.model.ui.SiteFull
import org.n1.av2.backend.service.StompService
import org.n1.av2.backend.service.layerhacking.ice.password.IceTangleService
import org.n1.av2.backend.service.patroller.PatrollerUiData
import org.n1.av2.backend.service.patroller.TracingPatrollerService
import org.n1.av2.backend.service.site.SiteService
import org.n1.av2.backend.service.terminal.SyntaxHighlightingService
import org.springframework.stereotype.Service

@Service
class RunService(
    private val runEntityService: RunEntityService,
    private val siteService: SiteService,
    private val layerStatusEntityService: LayerStatusEntityService,
    private val tracingPatrollerService: TracingPatrollerService,
    private val nodeStatusRepo: NodeStatusRepo,
    private val hackerStateEntityService: HackerStateEntityService,
    private val userEntityService: UserEntityService,
    private val timedTaskRunner: TimedTaskRunner,
    private val syntaxHighlightingService: SyntaxHighlightingService,
    private val stompService: StompService,
    private val runLinkEntityService: UserLinkEntityService,
    private val nodeEntityService: NodeEntityService,
    private val iceTangleService: IceTangleService,
) {

    class HackerPresence(
        val userId: String,
        val userName: String,
        val icon: HackerIcon,
        val nodeId: String?,
        val targetNodeId: String?,
        val activity: RunActivity,
        val locked: Boolean
    )

    fun enterRun(runId: String) {
        val run = runEntityService.getByRunId(runId)
        val thisHackerState = hackerStateEntityService.enterScan(run.siteId, runId)

        syntaxHighlightingService.sendForScan()
        stompService.toRun(runId, ReduxActions.SERVER_HACKER_ENTER_SCAN, toPresence(thisHackerState))

        val siteFull = siteService.getSiteFull(run.siteId)
        siteFull.sortNodeByDistance(run)

        siteFull.nodeStatuses = nodeStatusRepo.findByRunId(runId)
        siteFull.layerStatuses = layerStatusEntityService.getForRun(runId)
        val hackerPresences = getPresenceInRun(runId)
        val patrollers = tracingPatrollerService.getAllForRun(runId)

        class ScanAndSite(val run: Run, val site: SiteFull, val hackers: List<HackerPresence>, val patrollers: List<PatrollerUiData>)

        val scanAndSite = ScanAndSite(run, siteFull, hackerPresences, patrollers)
        stompService.reply(ReduxActions.SERVER_SCAN_FULL, scanAndSite)
    }

    private fun getPresenceInRun(runId: String): List<HackerPresence> {
        val hackersInRun = hackerStateEntityService.getHackersInRun(runId)

        return hackersInRun.map { state -> toPresence(state) }
    }

    private fun toPresence(state: HackerState): HackerPresence {
        val user = userEntityService.getById(state.userId)

        return HackerPresence(
            userId = user.id,
            userName = user.name,
            icon = user.icon,
            nodeId = state.currentNodeId,
            targetNodeId = state.currentNodeId,
            activity = state.runActivity,
            locked = state.locked
        )
    }

    fun leaveRun(hackerState: HackerState) {
        val runId = hackerState.runId ?: return // if somehow the user was already disconnected for another reason

        timedTaskRunner.removeAllFor(hackerState.userId)
        tracingPatrollerService.disconnected(hackerState.userId)

        class HackerLeaveNotification(val userId: String)
        stompService.toRun(runId, ReduxActions.SERVER_HACKER_LEAVE_SCAN, HackerLeaveNotification(hackerState.userId))
        stompService.reply(ReduxActions.SERVER_HACKER_DC, "-")

        hackerStateEntityService.leaveRun()
    }

    fun createRun(siteId: String, nodeScanById: MutableMap<String, NodeScan>, user: User): String {
        val run = runEntityService.create(siteId, nodeScanById, user.id)
        runLinkEntityService.createUserScan(run.runId, user)
        createIce(siteId, run)

        return run.runId
    }

    fun createIce(siteId: String, run: Run) {
        val nodes = nodeEntityService.getAll(siteId)
        nodes.forEach { node ->
            node.layers.forEach {layer ->
                setupLayer(layer, node.id, run.runId)
            }
        }
    }

    fun setupLayer(layer: Layer, nodeId: String, runId: String) {
        when (layer.type) {
            LayerType.ICE_TANGLE -> createTangleIce(layer, nodeId, runId)
            else -> return
        }
    }

    fun createTangleIce(layer: Layer, nodeId: String, runId: String) {
        if (layer !is IceTangleLayer) error("Wrong layer type/data for layer: ${layer.id}")

        val iceTangleStatus = iceTangleService.createTangleIce(layer, nodeId, runId)

        layerStatusEntityService.createLayerStatus(layer.id, runId, iceTangleStatus.id)
    }

}
