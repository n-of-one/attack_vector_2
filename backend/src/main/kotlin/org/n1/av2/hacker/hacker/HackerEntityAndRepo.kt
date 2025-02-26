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
) {
    fun hasSkill(requestType: HackerSkillType): Boolean {
        return skills.any { it.type == requestType }
    }

    fun skillAsIntOrNull(requestType: HackerSkillType): Int? {
        val skill = skills.find { it.type == requestType }
        val value = skill?.value?.toIntOrNull()
        return value
    }

}



interface HackerRepo : CrudRepository<Hacker, String> {
    fun findByHackerUserId(hackerUserId: String): Hacker?
}
