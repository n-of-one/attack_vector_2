package org.n1.av2.backend.service.run

import org.n1.av2.backend.engine.SYSTEM_USER_ID
import org.n1.av2.backend.model.db.run.HackerGeneralActivity
import org.n1.av2.backend.model.db.run.HackerState
import org.n1.av2.backend.model.db.run.HackerStateRunning
import org.n1.av2.backend.model.db.run.RunActivity
import org.n1.av2.backend.model.db.user.UserType
import org.n1.av2.backend.repo.HackerStateRepo
import org.n1.av2.backend.service.CurrentUserService
import org.n1.av2.backend.service.scan.ScanService
import org.n1.av2.backend.service.site.NodeService
import org.n1.av2.backend.service.site.SiteDataService
import org.n1.av2.backend.service.user.UserService
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct

@Service
class HackerStateService(
        private val hackerStateRepo: HackerStateRepo,
        private val currentUserService: CurrentUserService,
        private val siteDataService: SiteDataService,
        private val userService: UserService,
        private val nodeService: NodeService,
        private val scanService: ScanService
) {

    private val logger = mu.KotlinLogging.logger {}

    @PostConstruct
    fun init() {
        // If the server starts, all hackers are logged out by definition.

        userService.findAll().forEach { user ->
            if (user.type ==  UserType.HACKER || user.type ==  UserType.HACKER) {
                val newState = createLoggedOutState(user.id)
                hackerStateRepo.save(newState)
            }
        }
    }

    fun enterScan(siteId: String, runId: String): HackerState {
        val userId = currentUserService.userId

        val newState = HackerState(
                userId = userId,
                runId = runId,
                siteId = siteId,
                currentNodeId = null,
                previousNodeId = null,
                targetNodeId = null,
                generalActivity = HackerGeneralActivity.RUNNING,
                runActivity = RunActivity.SCANNING,
                hookPatrollerId = null, locked = false)
        hackerStateRepo.save(newState)
        return newState
    }

    fun getHackersInRun(runId: String): List<HackerState> {
        return hackerStateRepo.findByRunId(runId)
    }

    fun retrieveForCurrentUser(): HackerState {
        if (currentUserService.userId == SYSTEM_USER_ID) {
            error("Trying to perform action for current user assuming it is a player, but current user SYSTEM for a system event")
        }
        return retrieve(currentUserService.userId)
    }

    fun retrieve(userId: String): HackerState {
        return hackerStateRepo.findByUserId(userId) ?: error("HackerPosition not found for ${userId}")
    }

    fun startRun(userId: String, runId: String) {
        val scan = scanService.getByRunId(runId)
        val siteData = siteDataService.getBySiteId(scan.siteId)
        val nodes = nodeService.getAll(scan.siteId)
        val startNode = nodes.find { it.networkId == siteData.startNodeNetworkId }!!

        val newState = HackerState(
                runId = runId,
                siteId = scan.siteId,
                userId = userId,
                currentNodeId = null,
                previousNodeId = null,
                targetNodeId = startNode.id,
                generalActivity = HackerGeneralActivity.RUNNING,
                runActivity = RunActivity.SCANNING,
                hookPatrollerId = null, locked = false)
        hackerStateRepo.save(newState)
    }

    fun startedRun(userId: String, runId: String): HackerStateRunning {
        val oldState = retrieve(userId)
        val newState = oldState.copy(runActivity = RunActivity.AT_NODE, currentNodeId = oldState.targetNodeId, targetNodeId = null)
        hackerStateRepo.save(newState)

        return newState.toRunState()
    }

    fun arriveAt(position: HackerStateRunning, nodeId: String) {
        val newPosition = position.toState().copy(runActivity = RunActivity.AT_NODE,
                currentNodeId = nodeId, previousNodeId = position.currentNodeId, targetNodeId = null)
        hackerStateRepo.save(newPosition)
    }

    fun purgeAll() {
        hackerStateRepo.deleteAll()
        init()
    }

    fun snapBack(oldState: HackerStateRunning) {
        val newState = oldState.toState().copy(targetNodeId = oldState.currentNodeId)
        hackerStateRepo.save(newState)
    }

    fun lockHacker(hackerId: String, patrollerId: String) {
        val position = retrieve(hackerId)
        val newPosition = position.copy(locked = true, hookPatrollerId = patrollerId, runActivity = RunActivity.AT_NODE, targetNodeId = null)
        hackerStateRepo.save(newPosition)
    }

    fun leaveRun() {
        val newState = HackerState(
                userId = currentUserService.userId,
                runId = null, siteId = null,
                currentNodeId = null, previousNodeId = null, targetNodeId = null,
                generalActivity = HackerGeneralActivity.RUNNING,
                runActivity = RunActivity.NA,
                hookPatrollerId = null, locked = false)
        hackerStateRepo.save(newState)
    }

    fun login(userId: String) {
        val newState = HackerState(
                userId = userId,
                runId = null, siteId = null,
                currentNodeId = null, previousNodeId = null, targetNodeId = null,
                generalActivity = HackerGeneralActivity.ONLINE,
                runActivity = RunActivity.NA,
                hookPatrollerId = null, locked = false)
        hackerStateRepo.save(newState)
    }

    fun goOffline(state: HackerState) {
        val newState = createLoggedOutState(state.userId)
        hackerStateRepo.save(newState)
    }

    private fun createLoggedOutState(userId: String): HackerState {
        return HackerState(userId = userId, runId = null, siteId = null, currentNodeId = null, previousNodeId = null, targetNodeId = null,
                generalActivity = HackerGeneralActivity.OFFLINE, runActivity = RunActivity.NA, hookPatrollerId = null, locked = false)
    }

}