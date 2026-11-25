package org.n1.av2.hacker.hackerstate

import org.n1.av2.platform.iam.UserPrincipal
import org.n1.av2.platform.iam.user.CurrentUserService
import org.n1.av2.platform.iam.user.SYSTEM_USER
import org.n1.av2.platform.iam.user.UserEntityService
import org.n1.av2.platform.util.TimeService
import org.n1.av2.run.entity.Run
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import kotlin.jvm.optionals.getOrNull

@Service
class HackerStateEntityService(
    private val hackerStateRepo: HackerStateRepo,
    private val currentUserService: CurrentUserService,
    private val userEntityService: UserEntityService,
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

    fun enterRunOutside(siteId: String, userId: String, runId: String, connectionId: String): HackerState {

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

    // The hacker stays outside until the connection sequence ends, see [arriveAtIngress].
    fun startConnecting(currentState: HackerState, ingressNodeId: String) {
        val newState = currentState.copy(
            currentNodeId = null,
            previousNodeId = null,
            activity = HackerActivity.OUTSIDE,
            ingressNodeId = ingressNodeId,
            uplinkActivity = UplinkActivity.NONE,
        )
        hackerStateRepo.save(newState)
    }

    fun arriveAtIngress(currentState: HackerState): HackerState {
        val newState = currentState.copy(
            currentNodeId = currentState.ingressNodeId ?: error("Hacker ${currentState.userId} arrives without ingress node"),
            previousNodeId = null,
            activity = HackerActivity.INSIDE,
        )
        hackerStateRepo.save(newState)
        return newState
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
            ingressNodeId = null,
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
            ingressNodeId = null,
            uplinkReturns = emptyList(),
            uplinkReturnNodeId = null,
            uplinkActivity = UplinkActivity.NONE,
        )
        hackerStateRepo.save(newState)
    }

    fun prepareUplinkConnect(currentState: HackerState, run: Run, ingressNodeId: String, uplinkReturns: List<UplinkReturn>) {
        val newState = switchedRunState(currentState, run).copy(
            ingressNodeId = ingressNodeId,
            uplinkReturns = uplinkReturns,
            uplinkReturnNodeId = null,
            uplinkActivity = UplinkActivity.CONNECTING,
        )
        hackerStateRepo.save(newState)
    }

    fun prepareUplinkReturn(currentState: HackerState, run: Run, uplinkReturn: UplinkReturn, remainingUplinkReturns: List<UplinkReturn>) {
        val newState = switchedRunState(currentState, run).copy(
            ingressNodeId = uplinkReturn.ingressNodeId,
            uplinkReturns = remainingUplinkReturns,
            uplinkReturnNodeId = uplinkReturn.nodeId,
            uplinkActivity = UplinkActivity.RETURNING,
        )
        hackerStateRepo.save(newState)
    }

    private fun switchedRunState(currentState: HackerState, run: Run) = currentState.copy(
        runId = run.runId,
        siteId = run.siteId,
        currentNodeId = null,
        previousNodeId = null,
        activity = HackerActivity.OUTSIDE,
        iceId = null,
        networkedConnectionId = null,
    )

    fun prepareUplinkReturnOutside(currentState: HackerState, run: Run) {
        val newState = switchedRunState(currentState, run).copy(
            ingressNodeId = null,
            uplinkReturns = emptyList(),
            uplinkReturnNodeId = null,
            uplinkActivity = UplinkActivity.RETURNING_OUTSIDE,
        )
        hackerStateRepo.save(newState)
    }

    fun updateConnectionId(currentState: HackerState, connectionId: String): HackerState {
        val newState = currentState.copy(connectionId = connectionId)
        hackerStateRepo.save(newState)
        return newState
    }

    // Entering site straight inside, at the uplink node. The ingress node is the one used when the hacker first connected to this site.
    fun enterRunInsideBackFromUplinkedSite(currentState: HackerState, connectionId: String): HackerState {
        val newState = currentState.copy(
            connectionId = connectionId,
            currentNodeId = currentState.uplinkReturnNodeId ?: error("Hacker ${currentState.userId} returns without uplink node"),
            previousNodeId = null,
            activity = HackerActivity.INSIDE,
            uplinkReturnNodeId = null,
            uplinkActivity = UplinkActivity.NONE,
        )
        hackerStateRepo.save(newState)
        return newState
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
