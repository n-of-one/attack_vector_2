package org.n1.av2.backend.entity.run

import org.n1.av2.backend.entity.user.User
import org.n1.av2.backend.service.CurrentUserService
import org.springframework.stereotype.Repository

@Repository
class UserRunLinkEntityService(
    private val userRunLinkRepo: UserRunLinkRepo,
    private val currentUserService: CurrentUserService,
) {

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

    fun findAllByUserId(userId: String): List<UserRunLink> {
        return userRunLinkRepo.findAllByUserId(userId)
    }

    fun deleteAllForRuns(runs: List<Run>) {
        runs.forEach { userRunLinkRepo.deleteAllByRunId(it.runId) }
    }

}