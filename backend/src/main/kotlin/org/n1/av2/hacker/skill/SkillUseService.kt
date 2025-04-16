package org.n1.av2.hacker.skill

import org.n1.av2.hacker.hacker.Hacker
import org.n1.av2.platform.util.createId
import org.springframework.stereotype.Service

@Service
class SkillUseService(
    private val skillUseRepo: SkillUseRepo,
) {

    fun canUseSkillOnSite(hacker: Hacker, type: SkillType, siteId: String): Boolean {
        val previousUseOnSite = skillUseRepo.findByUserIdAndSkillTypeAndSiteId(hacker.hackerUserId, type, siteId)
        return (previousUseOnSite == null)  // can only use once
    }

    fun skillUsed(hacker: Hacker, type: SkillType, siteId: String) {
        val id = createId("use-", skillUseRepo::findById)
        val currentUse = SkillUse(id, hacker.hackerUserId, siteId, type)
        skillUseRepo.save(currentUse)
    }

    fun resetSite(siteId: String) {
        val usesForSite = skillUseRepo.findBySiteId(siteId)

    }
}
