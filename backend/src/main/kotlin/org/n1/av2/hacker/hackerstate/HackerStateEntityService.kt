package org.n1.av2.hacker.hackerstate

import org.n1.av2.platform.iam.UserPrincipal
import org.n1.av2.platform.iam.user.CurrentUserService
import org.n1.av2.platform.iam.user.SYSTEM_USER
import org.n1.av2.platform.iam.user.UserEntityService
import org.n1.av2.platform.util.TimeService
import org.n1.av2.run.entity.Run
import org.n1.av2.run.entity.RunEntityService
import org.n1.av2.site.entity.NodeEntityService
import org.n1.av2.site.entity.SitePropertiesEntityService
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import kotlin.jvm.optionals.getOrNull

@Service
class HackerStateEntityService(
    private val hackerStateRepo: HackerStateRepo,
    private val currentUserService: CurrentUserService,
    private val sitePropertiesEntityService: SitePropertiesEntityService,
    private val userEntityService: UserEntityService,
    private val nodeEntityService: NodeEntityService,
    private val runEntityService: RunEntityService,
    private val timeService: TimeService,
) {

    @EventListener(ApplicationReadyEvent::class)
    fun init() {
        // Remove all existing hacker states, we are free to change the data model without having to do data model version management.
        hackerStateRepo.deleteAll()

        // If the server starts, all hackers are logged out by definition.
        userEntityService.findAll().forEach { user ->
            val newState = createLoggedOutState(user.id, "no-connection")
            hackerStateRepo.save(newState)
        }
    }

    fun enterRun(siteId: String, userId: String, runId: String, connectionId: String): HackerState {

        val newState = HackerState(
            userId = userId,
            connectionId = connectionId,
            runId = runId,
            siteId = siteId,
            currentNodeId = null,
            previousNodeId = null,
            activity = HackerActivity.OUTSIDE,
            iceId = null,
            networkedConnectionId = null,
            iceConnectTimestamp = null,
        )
        hackerStateRepo.save(newState)
        return newState
    }

    fun startAttack(userId: String, run: Run) {
        val newState = HackerState(
            runId = run.runId,
            connectionId = currentUserService.connectionId,
            siteId = run.siteId,
            userId = userId,
            currentNodeId = null,
            previousNodeId = null,
            activity = HackerActivity.OUTSIDE,
            iceId = null,
            networkedConnectionId = null,
            iceConnectTimestamp = null,
        )
        hackerStateRepo.save(newState)
    }

    fun startedRun(userId: String, runId: String): String {
        val run = runEntityService.getByRunId(runId)
        val siteProperties = sitePropertiesEntityService.getBySiteId(run.siteId)
        val nodes = nodeEntityService.findBySiteId(run.siteId)
        val startNode = nodes.find { it.networkId == siteProperties.startNodeNetworkId }!!


        val oldState = retrieveForUserId(userId)
        val newState = oldState.copy(
            activity = HackerActivity.INSIDE,
            currentNodeId = startNode.id,
        )
        hackerStateRepo.save(newState)

        return startNode.id
    }

    fun arriveAt(state: HackerStateRunning, nodeId: String) {
        val newPosition = state.toState().copy(
            currentNodeId = nodeId,
            previousNodeId = state.currentNodeId,
        )
        hackerStateRepo.save(newPosition)
    }

    fun setConnectionForNetworkedApp(userPrincipal: UserPrincipal) {
        val newState = retrieveForUserId(userPrincipal.userId).copy(
            networkedConnectionId = userPrincipal.connectionId,
        )
        hackerStateRepo.save(newState)
    }

    fun enterIce(iceId: String) {
        val userPrincipal = SecurityContextHolder.getContext().authentication as UserPrincipal

        val newState = retrieveForUserId(userPrincipal.userId).copy(
            iceId = iceId,
            iceConnectTimestamp = timeService.now()
        )
        hackerStateRepo.save(newState)
    }

    fun disconnect(currentState: HackerState) {
        val newState = currentState.copy(
            currentNodeId = null,
            previousNodeId = null,
            activity = HackerActivity.OUTSIDE,
        )
        hackerStateRepo.save(newState)
    }

    fun leaveSite(currentState: HackerState) {
        val newState = currentState.copy(
            runId = null,
            siteId = null,
            currentNodeId = null,
            previousNodeId = null,
            activity = HackerActivity.ONLINE,
            iceId = null,
            networkedConnectionId = null,
        )
        hackerStateRepo.save(newState)
    }

    fun login(userId: String) {
        val newState = HackerState(
            userId = userId,
            connectionId = currentUserService.connectionId,
            runId = null, siteId = null,
            currentNodeId = null, previousNodeId = null,
            activity = HackerActivity.ONLINE,
            iceId = null,
            networkedConnectionId = null,
            iceConnectTimestamp = null,
        )
        hackerStateRepo.save(newState)
    }

    fun goOffline(userPrincipal: UserPrincipal) {
        val newState = createLoggedOutState(userPrincipal.userId, userPrincipal.connectionId)
        hackerStateRepo.save(newState)
    }

    fun leaveIce(userPrincipal: UserPrincipal) {
        val newState = retrieveForUserId(userPrincipal.userId).copy(
            iceId = null,
            networkedConnectionId = null,
            iceConnectTimestamp = null,
        )
        hackerStateRepo.save(newState)
    }

    private fun createLoggedOutState(userId: String, connectionId: String): HackerState {
        return HackerState(
            userId = userId,
            connectionId = connectionId,
            runId = null,
            siteId = null,
            currentNodeId = null,
            previousNodeId = null,
            activity = HackerActivity.OFFLINE,
            iceId = null,
            networkedConnectionId = null,
            iceConnectTimestamp = null,
        )
    }

    fun retrieveForCurrentUser(): HackerState {
        if (currentUserService.userId == SYSTEM_USER.id) {
            error("Trying to perform action for current user assuming it is a player, but current user SYSTEM for a system event")
        }
        return retrieveForUserId(currentUserService.userId)
    }

    fun retrieveForUserId(userId: String): HackerState {
        return hackerStateRepo.findById(userId).getOrNull() ?: createLoggedOutState(userId, "no-connection")
    }

    fun findByRunIdAndIceId(runId: String, iceId: String): List<HackerState> {
        return hackerStateRepo.findByRunIdAndIceId(runId, iceId)
    }

    fun findByIceId(iceId: String): List<HackerState> {
        return hackerStateRepo.findByIceId(iceId)
    }

    fun findAllHackersInRun(runId: String): List<HackerState> {
        return hackerStateRepo.findByRunId(runId)
    }

    fun findAllHackersInSite(siteId: String): List<HackerState> {
        return hackerStateRepo.findBySiteId(siteId)
    }

    fun isOnline(userId: String): Boolean {
        val state = hackerStateRepo.findById(userId).getOrNull() ?: return false
        return state.activity != HackerActivity.OFFLINE
    }


}
