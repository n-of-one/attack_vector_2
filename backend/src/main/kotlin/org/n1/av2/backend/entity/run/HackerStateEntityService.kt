package org.n1.av2.backend.entity.run

import org.n1.av2.backend.entity.site.NodeEntityService
import org.n1.av2.backend.entity.site.SitePropertiesEntityService
import org.n1.av2.backend.entity.user.SYSTEM_USER
import org.n1.av2.backend.entity.user.UserEntityService
import org.n1.av2.backend.model.iam.UserPrincipal
import org.n1.av2.backend.service.user.CurrentUserService
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service

@Service
class HackerStateEntityService(
    private val hackerStateRepo: HackerStateRepo,
    private val currentUserService: CurrentUserService,
    private val sitePropertiesEntityService: SitePropertiesEntityService,
    private val userEntityService: UserEntityService,
    private val nodeEntityService: NodeEntityService,
    private val runEntityService: RunEntityService
) {

    private val logger = mu.KotlinLogging.logger {}

    @EventListener(ApplicationReadyEvent::class)
    fun init() {
        // If the server starts, all hackers are logged out by definition.

        userEntityService.findAll().forEach { user ->
            val newState = createLoggedOutState(user.id, "no-connection")
            hackerStateRepo.save(newState)
        }
    }

    fun enterSite(siteId: String, runId: String): HackerState {
        val userId = currentUserService.userId

        val newState = HackerState(
            userId = userId,
            connectionId = currentUserConnectionId(),
            runId = runId,
            siteId = siteId,
            currentNodeId = null,
            previousNodeId = null,
            activity = HackerActivity.OUTSIDE,
            masked = true // TODO  - unmasked is not automatically reset at start of hack
        )
        hackerStateRepo.save(newState)
        return newState
    }

    fun getHackersInRun(runId: String): List<HackerState> {
        return hackerStateRepo.findByRunId(runId)
    }

    fun retrieveForCurrentUser(): HackerState {
        if (currentUserService.userId == SYSTEM_USER.id) {
            error("Trying to perform action for current user assuming it is a player, but current user SYSTEM for a system event")
        }
        return retrieve(currentUserService.userId)
    }

    fun retrieve(userId: String): HackerState {
        return hackerStateRepo.findByUserId(userId) ?: createLoggedOutState(userId, "no-connection")
    }

    fun startRun(userId: String, run: Run ) {
        val newState = HackerState(
            runId = run.runId,
            connectionId = currentUserConnectionId(),
            siteId = run.siteId,
            userId = userId,
            currentNodeId = null,
            previousNodeId = null,
            activity = HackerActivity.OUTSIDE,
            masked = true
        )
        hackerStateRepo.save(newState)
    }

    fun startedRun(userId: String, runId: String): HackerStateRunning {
        val run = runEntityService.getByRunId(runId)
        val siteProperties = sitePropertiesEntityService.getBySiteId(run.siteId)
        val nodes = nodeEntityService.getAll(run.siteId)
        val startNode = nodes.find { it.networkId == siteProperties.startNodeNetworkId }!!


        val oldState = retrieve(userId)
        val newState = oldState.copy(
            activity = HackerActivity.INSIDE,
            currentNodeId = startNode.id,
        )
        hackerStateRepo.save(newState)

        return newState.toRunState()
    }

    fun arriveAt(position: HackerStateRunning, nodeId: String) {
        val newPosition = position.toState().copy(
            currentNodeId = nodeId,
            previousNodeId = position.currentNodeId,
        )
        hackerStateRepo.save(newPosition)
    }

    fun lockHacker(hackerId: String, patrollerId: String) {
        val currentState = retrieve(hackerId)
        val newState = currentState.copy(
            masked = false,
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
        )
        hackerStateRepo.save(newState)
    }

    fun login(userId: String) {
        val newState = HackerState(
            userId = userId,
            connectionId = currentUserConnectionId(),
            runId = null, siteId = null,
            currentNodeId = null, previousNodeId = null,
            activity = HackerActivity.ONLINE,
            masked = true
        )
        hackerStateRepo.save(newState)
    }

    fun goOffline(userPrincipal: UserPrincipal) {
        val newState = createLoggedOutState(userPrincipal.userId, userPrincipal.connectionId)
        hackerStateRepo.save(newState)
    }

    private fun createLoggedOutState(userId: String, connectionId: String): HackerState {
        return HackerState(
            userId = userId,
            connectionId = connectionId,
            runId = null, siteId = null,
            currentNodeId = null,
            previousNodeId = null,
            activity = HackerActivity.OFFLINE,
            masked = true
        )
    }

    fun findAllHackersInRun(runId: String): List<HackerState> {
        return hackerStateRepo.findByRunId(runId)
    }

    fun findAllHackersInSite(siteId: String): List<HackerState> {
        return hackerStateRepo.findBySiteId(siteId)
    }

    fun isOnline(userId: String): Boolean {
        val state = hackerStateRepo.findByUserId(userId) ?: return false
        return state.activity != HackerActivity.OFFLINE
    }

    fun currentUserConnectionId(): String {
        return (SecurityContextHolder.getContext().authentication as UserPrincipal).connectionId
    }

}