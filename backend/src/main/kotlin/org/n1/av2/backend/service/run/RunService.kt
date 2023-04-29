package org.n1.av2.backend.service.run

import org.n1.av2.backend.engine.TimedTaskRunner
import org.n1.av2.backend.entity.run.*
import org.n1.av2.backend.entity.site.NodeEntityService
import org.n1.av2.backend.entity.site.SitePropertiesEntityService
import org.n1.av2.backend.entity.site.enums.LayerType
import org.n1.av2.backend.entity.site.layer.*
import org.n1.av2.backend.entity.user.HackerIcon
import org.n1.av2.backend.entity.user.User
import org.n1.av2.backend.entity.user.UserEntityService
import org.n1.av2.backend.model.ui.NotyMessage
import org.n1.av2.backend.model.ui.NotyType
import org.n1.av2.backend.model.ui.ServerActions
import org.n1.av2.backend.model.ui.SiteFull
import org.n1.av2.backend.service.CurrentUserService
import org.n1.av2.backend.service.StompService
import org.n1.av2.backend.service.layerhacking.ice.netwalk.NetwalkIceService
import org.n1.av2.backend.service.layerhacking.ice.password.PasswordService
import org.n1.av2.backend.service.layerhacking.ice.slow.SlowIceService
import org.n1.av2.backend.service.layerhacking.ice.tangle.TangleService
import org.n1.av2.backend.service.layerhacking.ice.wordsearch.WordSearchService
import org.n1.av2.backend.service.patroller.PatrollerUiData
import org.n1.av2.backend.service.patroller.TracingPatrollerService
import org.n1.av2.backend.service.scan.ScanInfoService
import org.n1.av2.backend.service.site.SiteService
import org.n1.av2.backend.service.terminal.SyntaxHighlightingService
import org.springframework.stereotype.Service

@Service
class RunService(
    private val runEntityService: RunEntityService,
    private val currentUserService: CurrentUserService,
    private val siteService: SiteService,
    private val layerStatusEntityService: LayerStatusEntityService,
    private val tracingPatrollerService: TracingPatrollerService,
    private val nodeStatusEntityService: NodeStatusEntityService,
    private val hackerStateEntityService: HackerStateEntityService,
    private val userEntityService: UserEntityService,
    private val timedTaskRunner: TimedTaskRunner,
    private val syntaxHighlightingService: SyntaxHighlightingService,
    private val stompService: StompService,
    private val userRunLinkEntityService: UserRunLinkEntityService,
    private val nodeEntityService: NodeEntityService,
    private val sitePropertiesEntityService: SitePropertiesEntityService,
    private val scanInfoService: ScanInfoService,
    private val tangleService: TangleService,
    private val passwordService: PasswordService,
    private val wordSearchService: WordSearchService,
    private val netwalkIceService: NetwalkIceService,
    private val slowIceService: SlowIceService,
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
        stompService.toRun(runId, ServerActions.SERVER_HACKER_ENTER_SCAN, toPresence(thisHackerState))

        val siteFull = siteService.getSiteFull(run.siteId)
        siteFull.sortNodeByDistance(run)

        siteFull.nodeStatuses = nodeStatusEntityService.findByRunId(runId)
        siteFull.layerStatuses = layerStatusEntityService.getForRun(runId)
        val hackerPresences = getPresenceInRun(runId)
        val patrollers = tracingPatrollerService.getAllForRun(runId)

        class ScanAndSite(val run: Run, val site: SiteFull, val hackers: List<HackerPresence>, val patrollers: List<PatrollerUiData>)

        val scanAndSite = ScanAndSite(run, siteFull, hackerPresences, patrollers)
        stompService.reply(ServerActions.SERVER_SCAN_FULL, scanAndSite)
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
        stompService.toRun(runId, ServerActions.SERVER_HACKER_LEAVE_SCAN, HackerLeaveNotification(hackerState.userId))
        stompService.reply(ServerActions.SERVER_HACKER_DC, "-")

        hackerStateEntityService.leaveRun(hackerState.userId)
    }

    fun startNewRun(siteName: String) {
        val siteProperties = sitePropertiesEntityService.findByName(siteName)
        if (siteProperties == null) {
            stompService.replyMessage(NotyMessage(NotyType.NEUTRAL, "Error", "Site '${siteName}' not found"))
            return
        }

        val nodeScans = scanInfoService.createNodeScans(siteProperties.siteId)

        val user = currentUserService.user

        val runId = createRun(siteProperties.siteId, nodeScans, user)

        data class ScanSiteResponse(val runId: String, val siteId: String)
        val response = ScanSiteResponse(runId, siteProperties.siteId)
        stompService.reply(ServerActions.SERVER_SITE_DISCOVERED, response)
        scanInfoService.sendScanInfosOfPlayer() // to update the scans in the home screen
    }

    fun createRun(siteId: String, nodeScanById: MutableMap<String, NodeScan>, user: User): String {
        val runId = runEntityService.createRunId()

        // create ice can fail, so do that first. If it fails, nothing is persisted yet.
        createIce(siteId, runId)

        val run = runEntityService.create(runId, siteId, nodeScanById, user.id)
        userRunLinkEntityService.createUserScan(run.runId, user)

        return run.runId
    }

    fun createIce(siteId: String, runId: String) {
        val nodes = nodeEntityService.getAll(siteId)
        nodes.forEach { node ->
            node.layers.forEach {layer ->
                setupLayer(layer, node.id, runId)
            }
        }
    }

    fun setupLayer(layer: Layer, nodeId: String, runId: String) {
        when (layer.type) {
            LayerType.TANGLE_ICE -> createTangleIce(layer, nodeId, runId)
            LayerType.PASSWORD_ICE -> createPasswordIce(layer, nodeId, runId)
            LayerType.WORD_SEARCH_ICE -> createWordSearchIce(layer, nodeId, runId)
            LayerType.NETWALK_ICE ->createNetwalkIce(layer, nodeId, runId)
            LayerType.SLOW_ICE -> createUnhackableIce(layer, nodeId, runId)
            else -> return
        }
    }

    private fun createUnhackableIce(layer: Layer, nodeId: String, runId: String) {
        if (layer !is SlowIceLayer) error("Wrong layer type/data for layer: ${layer.id}")
        val slowIceStatus = slowIceService.createIce(layer, nodeId, runId)
        layerStatusEntityService.createLayerStatus(layer.id, runId, slowIceStatus.id)
    }

    private fun createNetwalkIce(layer: Layer, nodeId: String, runId: String) {
        if (layer !is NetwalkIceLayer) error("Wrong layer type/data for layer: ${layer.id}")
        val netwalkIceStatus = netwalkIceService.createIce(layer, nodeId, runId)
        layerStatusEntityService.createLayerStatus(layer.id, runId, netwalkIceStatus.id)
    }


    private fun createWordSearchIce(layer: Layer, nodeId: String, runId: String) {
        if (layer !is WordSearchIceLayer) error("Wrong layer type/data for layer: ${layer.id}")
        val wordSearchIceStatus = wordSearchService.createIce(layer, nodeId, runId)
        layerStatusEntityService.createLayerStatus(layer.id, runId, wordSearchIceStatus.id)
    }

    fun createTangleIce(layer: Layer, nodeId: String, runId: String) {
        if (layer !is TangleIceLayer) error("Wrong layer type/data for layer: ${layer.id}")
        val iceTangleStatus = tangleService.createTangleIce(layer, nodeId, runId)
        layerStatusEntityService.createLayerStatus(layer.id, runId, iceTangleStatus.id)
    }

    fun createPasswordIce(layer: Layer, nodeId: String, runId: String) {
        if (layer !is PasswordIceLayer) error("Wrong layer type/data for layer: ${layer.id}")
        val passwordIceStatus = passwordService.createStatus(layer, nodeId, runId)
        layerStatusEntityService.createLayerStatus(layer.id, runId, passwordIceStatus.id)
    }

    fun deleteSite(siteId: String) {
        val site = sitePropertiesEntityService.getBySiteId(siteId)
        val runs = runEntityService.findAllForSiteId(siteId)

        userRunLinkEntityService.deleteAllForRuns(runs)

        runs.map { run -> hackerStateEntityService.findAllHackersInRun(run.runId) }
            .flatten()
            .onEach { userId: String ->
                scanInfoService.sendScanInfosOfPlayer(userId)
                hackerStateEntityService.leaveRun(userId)
                timedTaskRunner.removeAllFor(userId)
            }

        runs.forEach { run ->
            stompService.toRun(run.runId, ServerActions.SERVER_NOTIFICATION, NotyMessage( NotyType.ERROR, "Error", "Lost network connection to site: ${site.name}" ) )
            stompService.toRun(run.runId, ServerActions.SERVER_HACKER_DC, "-")
        }

        layerStatusEntityService.deleteAllForRuns(runs)
        nodeStatusEntityService.deleteAllForRuns(runs)

        tracingPatrollerService.deleteAllForRuns(runs)

        tangleService.deleteAllForRuns(runs)
        passwordService.deleteAllForRuns(runs)
        wordSearchService.deleteAllForRuns(runs)
        netwalkIceService.deleteAlLForRuns(runs)
        slowIceService.deleteAllForRuns(runs)
    }
}
