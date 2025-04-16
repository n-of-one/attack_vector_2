package org.n1.av2.hacker.skill

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Document
data class SkillUse(
    @Id val id: String,
    @Indexed val userId: String,
    @Indexed val siteId: String,
    val skillType: SkillType,
)

@Repository
interface SkillUseRepo : MongoRepository<SkillUse, String> {
    fun findByUserIdAndSkillTypeAndSiteId(userId: String, skillType: SkillType,siteId: String): SkillUse?
    fun findBySiteId(siteId: String) {}
}
