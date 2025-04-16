package org.n1.av2.hacker.hacker

import org.n1.av2.hacker.skill.Skill
import org.n1.av2.hacker.skill.SkillType
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
    val skills: List<Skill>
) {
    fun hasSkill(requestType: SkillType): Boolean {
        return skills.any { it.type == requestType }
    }

    fun skillAsIntOrNull(requestType: SkillType): Int? {
        val skill = skills.find { it.type == requestType }
        val value = skill?.value?.toIntOrNull()
        return value
    }

    fun skillContainingValue(type: SkillType, value: String): Boolean {
        val matchingSkills = skills.filter { it.type == type }
        if  (matchingSkills.isEmpty()) return false

        return matchingSkills.any { it.value?.contains(value) == true }
    }

}


interface HackerRepo : CrudRepository<Hacker, String> {
    fun findByHackerUserId(hackerUserId: String): Hacker?
}
