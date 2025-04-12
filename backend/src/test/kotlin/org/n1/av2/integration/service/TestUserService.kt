package org.n1.av2.integration.service

import org.n1.av2.hacker.hacker.HackerEntityService
import org.n1.av2.hacker.skill.Skill
import org.n1.av2.hacker.skill.SkillType.SCAN
import org.n1.av2.hacker.skill.SkillType.SEARCH_SITE
import org.n1.av2.platform.iam.user.HackerIcon
import org.n1.av2.platform.iam.user.UserEntity
import org.n1.av2.platform.iam.user.UserEntityService
import org.n1.av2.platform.iam.user.UserType
import org.springframework.stereotype.Service

@Service
class TestUserService(
    private val userEntityService: UserEntityService,
    private val hackerEntityService: HackerEntityService,
) {

    private val defaultSkills = listOf(Skill(SCAN), Skill(SEARCH_SITE))

    fun createHackerForTest(name: String, type: UserType = UserType.HACKER): UserEntity {
        val existingUser = userEntityService.findByNameIgnoreCase(name)
        if (existingUser != null) {
            return existingUser
        }
        val user = userEntityService.createUser(name, UserType.HACKER)
        hackerEntityService.createHacker(user, HackerIcon.CAT, name, defaultSkills)

        return user
    }

    fun deleteTestUserIfExists(name: String) {
        val id = testUserId(name)
        try {
            userEntityService.getById(id) // fails if user does not exist
            userEntityService.delete(id)
        }
        catch (_: Exception) {
            return // user did not exist
        }
    }

    private fun testUserId(name: String): String {
        return name
    }

}
