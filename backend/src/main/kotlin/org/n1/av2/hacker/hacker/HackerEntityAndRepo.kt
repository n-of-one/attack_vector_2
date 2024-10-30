package org.n1.av2.hacker.hacker

import org.n1.av2.platform.iam.user.HackerIcon
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.repository.CrudRepository

@Document
data class Hacker(
    @Id val id: String,
    val hackerUserId: String,
    val icon: HackerIcon,
    val characterName: String,
    val skills: List<HackerSkill>
)
{
    fun hasSKill(requestType: HackerSkillType): Boolean {
        return skills.filter { it.type == requestType }.any()
    }

}

class HackerSkill(
    val type: HackerSkillType,
)

enum class HackerSkillType {
    CREATE_SITE,
    SCAN,
    SEARCH_SITE,
}


interface HackerRepo : CrudRepository<Hacker, String> {
    fun findByHackerUserId(hackerUserId: String): Hacker?
}
