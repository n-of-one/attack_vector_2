package org.n1.av2.backend.service.run

import mu.KLogging
import org.n1.av2.backend.engine.SYSTEM_USER_ID
import org.n1.av2.backend.model.db.run.HackerGeneralActivity
import org.n1.av2.backend.model.db.run.HackerSpecificActivity
import org.n1.av2.backend.model.db.run.HackerState
import org.n1.av2.backend.model.db.run.HackerStateRunning
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

    companion object : KLogging()

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
                specificActivity = HackerSpecificActivity.SCANNING,
                locked = false)
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
                currentNodeId = startNode.id,
                previousNodeId = null,
                targetNodeId = null,
                generalActivity = HackerGeneralActivity.RUNNING,
                specificActivity = HackerSpecificActivity.STARTING,
                locked = false)
        hackerStateRepo.save(newState)
    }

    fun startedRun(userId: String, runId: String): HackerStateRunning {
        val scan = scanService.getByRunId(runId)
        val siteData = siteDataService.getBySiteId(scan.siteId)
        val nodes = nodeService.getAll(scan.siteId)
        val startNode = nodes.find { it.networkId == siteData.startNodeNetworkId }!!

        val newState = HackerState(
                runId = runId,
                siteId = scan.siteId,
                userId = userId,
                currentNodeId = startNode.id,
                previousNodeId = null,
                targetNodeId = null,
                generalActivity = HackerGeneralActivity.RUNNING,
                specificActivity = HackerSpecificActivity.STARTING,
                locked = false)
        hackerStateRepo.save(newState)

        return newState.toRunState()
    }

    fun saveInTransit(runState: HackerStateRunning, toNodeId: String) {
        val newPosition = runState.toState().copy(specificActivity = HackerSpecificActivity.MOVING, targetNodeId = toNodeId)
        hackerStateRepo.save(newPosition)
    }

    fun arriveAt(position: HackerStateRunning, nodeId: String) {
        val newPosition = position.toState().copy(specificActivity = HackerSpecificActivity.AT_NODE, currentNodeId = nodeId, previousNodeId = position.currentNodeId, targetNodeId = null)
        hackerStateRepo.save(newPosition)
    }

    fun purgeAll() {
        hackerStateRepo.deleteAll()
        init()
    }

    fun lockHacker(hackerId: String) {
        val position = retrieve(hackerId)
        val newPosition = position.copy(locked = true, specificActivity = HackerSpecificActivity.AT_NODE, targetNodeId = null)
        hackerStateRepo.save(newPosition)
    }

    fun hackersAt(nodeId: String, runId: String): List<String> {
        return hackerStateRepo
                .findByCurrentNodeIdAndRunId(nodeId, runId)
                .map { it.userId }
    }

    fun leaveRun() {
        val newState = HackerState(
                userId = currentUserService.userId,
                runId = null, siteId = null,
                currentNodeId = null, previousNodeId = null, targetNodeId = null,
                generalActivity = HackerGeneralActivity.RUNNING,
                specificActivity = HackerSpecificActivity.STARTING,
                locked = false)
        hackerStateRepo.save(newState)
    }

    fun login(userId: String) {
        val newState = HackerState(
                userId = userId,
                runId = null, siteId = null,
                currentNodeId = null, previousNodeId = null, targetNodeId = null,
                generalActivity = HackerGeneralActivity.ONLINE,
                specificActivity = HackerSpecificActivity.NA,
                locked = false)
        hackerStateRepo.save(newState)
    }

    fun goOffline(state: HackerState) {
        val newState = createLoggedOutState(state.userId)
        hackerStateRepo.save(newState)
    }

    private fun createLoggedOutState(userId: String): HackerState {
        return HackerState(userId = userId, runId = null, siteId = null, currentNodeId = null, previousNodeId = null, targetNodeId = null,
                generalActivity = HackerGeneralActivity.OFFLINE, specificActivity = HackerSpecificActivity.NA, locked = false)
    }


}