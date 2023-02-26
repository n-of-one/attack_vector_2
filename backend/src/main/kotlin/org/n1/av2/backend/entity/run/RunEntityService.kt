package org.n1.av2.backend.entity.run

import org.n1.av2.backend.entity.site.SiteProperties
import org.n1.av2.backend.entity.user.User
import org.n1.av2.backend.service.CurrentUserService
import org.n1.av2.backend.service.TimeService
import org.n1.av2.backend.util.createId
import org.springframework.stereotype.Service

@Service
class RunEntityService(
    private val runRepo: RunRepo,
    private val userRunLinkRepo: UserRunLinkRepo,
    private val time: TimeService,
    private val currentUserService: CurrentUserService
) {

    fun getByRunId(runId: String): Run {
        return runRepo.findByRunId(runId) ?: error("${runId} not found")
    }

    fun createScan(siteProperties: SiteProperties, nodeScanById: MutableMap<String, NodeScan>, user: User): String {
        val runId = createId("run") { candidate: String -> runRepo.findByRunId(candidate) }
        val run = Run(
            scanStartTime = time.now(),
            runId = runId,
            siteId = siteProperties.siteId,
            nodeScanById = nodeScanById,
            initiatorId = user.id
        )
        runRepo.save(run)

        val userId = currentUserService.userId
        val userRunLink = UserRunLink(userId = userId, runId = runId)
        userRunLinkRepo.save(userRunLink)

        return runId
    }

    fun save(run: Run) {
        runRepo.save(run)
    }

    fun getAll(userId: String): List<Run> {
        val userScans = userRunLinkRepo.findAllByUserId(userId)
        val runIds = userScans.map { it.runId }
        return runRepo.findByRunIdIn(runIds)
    }

    fun hasUserScan(user: User, runId: String): Boolean {
        return userRunLinkRepo.findByUserIdAndRunId(user.id, runId) != null
    }

    fun createUserScan(runId: String, user: User) {
        val userRunLink = UserRunLink(userId = user.id, runId = runId)
        userRunLinkRepo.save(userRunLink)
    }

    fun deleteUserScan(runId: String) {
        userRunLinkRepo.deleteByUserIdAndRunId(currentUserService.userId, runId)
    }

    fun getUsersOfScan(runId: String): Collection<String> {
        val userScans = userRunLinkRepo.findAllByRunId(runId)
        return userScans.map { it.userId }
    }
}

