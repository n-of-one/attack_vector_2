package org.n1.av2.backend.entity.run

import org.n1.av2.backend.entity.user.UserEntity
import org.n1.av2.backend.service.user.CurrentUserService
import org.springframework.stereotype.Service

@Service
class UserRunLinkEntityService(
    private val userRunLinkRepo: UserRunLinkRepo,
    private val currentUserService: CurrentUserService,
) {

    fun hasUserScan(userEntity: UserEntity, runId: String): Boolean {
        return userRunLinkRepo.findByUserIdAndRunId(userEntity.id, runId) != null
    }

    fun createUserScan(runId: String, userEntity: UserEntity) {
        val userRunLink = UserRunLink(userId = userEntity.id, runId = runId)
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

    fun deleteAllForRun(run: Run) {
        userRunLinkRepo.deleteAllByRunId(run.runId)
    }

    fun deleteAllForUser(userId: String) {
        userRunLinkRepo.deleteAllByUserId(userId)
    }

}