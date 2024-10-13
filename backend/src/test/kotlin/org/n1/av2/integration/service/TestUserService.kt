package org.n1.av2.integration.service

import org.n1.av2.platform.iam.user.Hacker
import org.n1.av2.platform.iam.user.HackerIcon
import org.n1.av2.platform.iam.user.HackerSkill
import org.n1.av2.platform.iam.user.UserEntity
import org.n1.av2.platform.iam.user.UserEntityService
import org.n1.av2.platform.iam.user.UserType
import org.springframework.stereotype.Service

@Service
class TestUserService(
    private val userEntityService: UserEntityService,
) {

    private val defaultSkills = setOf(HackerSkill.SCAN, HackerSkill.SEARCH_SITE)

    fun createUserForTest(name: String, type: UserType = UserType.HACKER): UserEntity {
        val existingUser = userEntityService.findByNameIgnoreCase(name)
        if (existingUser != null) {
            return existingUser
        }

        val newUserEntity = UserEntity(
            id = testUserId(name),
            name = name,
            type = type,
            hacker = Hacker(
                icon = HackerIcon.COBRA,
                characterName = "char for ${name}",
                skills = defaultSkills
            ),
        )
        return userEntityService.save(newUserEntity)
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
