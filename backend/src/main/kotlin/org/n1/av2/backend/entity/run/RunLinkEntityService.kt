package org.n1.av2.backend.entity.run

import org.n1.av2.backend.entity.user.UserEntity
import org.n1.av2.backend.service.user.CurrentUserService
import org.springframework.stereotype.Service

@Service
class RunLinkEntityService(
    private val runLinkRepo: RunLinkRepo,
    private val currentUserService: CurrentUserService,
) {

    fun hasUserRunLink(userEntity: UserEntity, runId: String): Boolean {
        return runLinkRepo.findByUserIdAndRunId(userEntity.id, runId) != null
    }

    fun createRunLink(runId: String, userEntity: UserEntity) {
        val runLink = RunLink(userId = userEntity.id, runId = runId)
        runLinkRepo.save(runLink)
    }

    fun deleteRunLink(runId: String) {
        runLinkRepo.deleteByUserIdAndRunId(currentUserService.userId, runId)
    }

    fun getUserIdsOfRun(runId: String): Collection<String> {
        val userScans = runLinkRepo.findAllByRunId(runId)
        return userScans.map { it.userId }
    }

    fun findAllByUserId(userId: String): List<RunLink> {
        return runLinkRepo.findAllByUserId(userId)
    }

    fun deleteAllForRun(run: Run) {
        runLinkRepo.deleteAllByRunId(run.runId)
    }

    fun deleteAllForUser(userId: String) {
        runLinkRepo.deleteAllByUserId(userId)
    }

}