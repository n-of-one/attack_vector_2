package org.n1.av2.hacker.skill

import org.n1.av2.hacker.hacker.HackerEntityService
import org.n1.av2.platform.iam.user.CurrentUserService
import org.springframework.stereotype.Service

@Service
class SkillService(
    private val currentUserService: CurrentUserService,
    private val hackerEntityService: HackerEntityService,
) {

    fun currentUserHasSkill(type: SkillType): Boolean {
        val hacker = hackerEntityService.findForUserId(currentUserService.userId)
        return hacker.hasSkill(type)
    }
}
