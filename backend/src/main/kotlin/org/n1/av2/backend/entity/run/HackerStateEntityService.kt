package org.n1.av2.backend.entity.run

import org.n1.av2.backend.entity.site.NodeEntityService
import org.n1.av2.backend.entity.site.SitePropertiesEntityService
import org.n1.av2.backend.entity.user.SYSTEM_USEREntity
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

    fun enterScan(siteId: String, runId: String): HackerState {
        val userId = currentUserService.userId


        val newState = HackerState(
            userId = userId,
            connectionId = currentUserConnectionId(),
            runId = runId,
            siteId = siteId,
            currentNodeId = null,
            previousNodeId = null,
            targetNodeId = null,
            generalActivity = HackerGeneralActivity.RUNNING,
            runActivity = RunActivity.SCANNING,
            hookPatrollerId = null, locked = false
        )
        hackerStateRepo.save(newState)
        return newState
    }

    fun getHackersInRun(runId: String): List<HackerState> {
        return hackerStateRepo.findByRunId(runId)
    }

    fun retrieveForCurrentUser(): HackerState {
        if (currentUserService.userId == SYSTEM_USEREntity.id) {
            error("Trying to perform action for current user assuming it is a player, but current user SYSTEM for a system event")
        }
        return retrieve(currentUserService.userId)
    }

    fun retrieve(userId: String): HackerState {
        return hackerStateRepo.findByUserId(userId) ?: createLoggedOutState(userId, "no-connection")
    }

    fun startRun(userId: String, runId: String) {
        val scan = runEntityService.getByRunId(runId)
        val siteProperties = sitePropertiesEntityService.getBySiteId(scan.siteId)
        val nodes = nodeEntityService.getAll(scan.siteId)
        val startNode = nodes.find { it.networkId == siteProperties.startNodeNetworkId }!!

        val newState = HackerState(
            runId = runId,
            connectionId = currentUserConnectionId(),
            siteId = scan.siteId,
            userId = userId,
            currentNodeId = null,
            previousNodeId = null,
            targetNodeId = startNode.id,
            generalActivity = HackerGeneralActivity.RUNNING,
            runActivity = RunActivity.SCANNING,
            hookPatrollerId = null, locked = false
        )
        hackerStateRepo.save(newState)
    }

    fun startedRun(userId: String, runId: String): HackerStateRunning {
        val oldState = retrieve(userId)
        val newState = oldState.copy(
            runActivity = RunActivity.AT_NODE,
            currentNodeId = oldState.targetNodeId,
            targetNodeId = null
        )
        hackerStateRepo.save(newState)

        return newState.toRunState()
    }

    fun arriveAt(position: HackerStateRunning, nodeId: String) {
        val newPosition = position.toState().copy(
            runActivity = RunActivity.AT_NODE,
            currentNodeId = nodeId,
            previousNodeId = position.currentNodeId,
            targetNodeId = null
        )
        hackerStateRepo.save(newPosition)
    }

    fun lockHacker(hackerId: String, patrollerId: String) {
        val position = retrieve(hackerId)
        val newPosition = position.copy(
            locked = true,
            hookPatrollerId = patrollerId,
            runActivity = RunActivity.AT_NODE,
            targetNodeId = null
        )
        hackerStateRepo.save(newPosition)
    }

    fun leaveRun(userId: String) {
        val newState = HackerState(
            userId = userId,
            connectionId = currentUserConnectionId(),
            runId = null, siteId = null,
            currentNodeId = null,
            previousNodeId = null,
            targetNodeId = null,
            generalActivity = HackerGeneralActivity.ONLINE,
            runActivity = RunActivity.NA,
            hookPatrollerId = null,
            locked = false
        )
        hackerStateRepo.save(newState)

    }

    fun login(userId: String) {
        val newState = HackerState(
            userId = userId,
            connectionId = currentUserConnectionId(),
            runId = null, siteId = null,
            currentNodeId = null, previousNodeId = null,
            targetNodeId = null,
            generalActivity = HackerGeneralActivity.ONLINE,
            runActivity = RunActivity.NA,
            hookPatrollerId = null,
            locked = false
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
            targetNodeId = null,
            generalActivity = HackerGeneralActivity.OFFLINE,
            runActivity = RunActivity.NA,
            hookPatrollerId = null,
            locked = false
        )
    }

    fun findAllHackersInRun(runId: String): List<String> {
        return hackerStateRepo.findByRunId(runId).map { it.userId }
    }

    fun isOnline(userId: String): Boolean {
        val state = hackerStateRepo.findByUserId(userId) ?: return false
        return state.generalActivity != HackerGeneralActivity.OFFLINE
    }

    fun currentUserConnectionId(): String {
        return (SecurityContextHolder.getContext().authentication as UserPrincipal).connectionId
    }

}