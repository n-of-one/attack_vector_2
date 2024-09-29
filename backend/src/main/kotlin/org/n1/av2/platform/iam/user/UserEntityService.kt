package org.n1.av2.platform.iam.user

import org.n1.av2.platform.util.createId
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Component
import java.util.*
import kotlin.jvm.optionals.getOrNull

val defaultSkills = setOf(HackerSkill.SEARCH_SITE, HackerSkill.SCAN)

@Component("UserEntityService")
class UserEntityService(
    private val userEntityRepo: UserEntityRepo,
) {


    fun findByNameIgnoreCase(userName: String): UserEntity? {
        return userEntityRepo.findByNameIgnoreCase(userName)
    }

    fun getByName(userName: String): UserEntity {
        return findByNameIgnoreCase(userName) ?: throw UsernameNotFoundException("Username not found: ${userName}")
    }

    fun createDefaultUser(userName: String, type: UserType, icon: HackerIcon = HackerIcon.NOT) {
        val user = findByNameIgnoreCase(userName)
        if (user != null) return

        val hacker = if (type == UserType.HACKER) Hacker(
            icon = icon,
            characterName = "unknown",
            skills = defaultSkills
        )
        else null

        val newUserEntity = UserEntity(
            name = userName,
            type = type,
            id = createUserId(),
            hacker = hacker,
        )
        userEntityRepo.save(newUserEntity)
    }

    fun createUserId(): String {
        fun findExisting(candidate: String): Optional<UserEntity> {
            return userEntityRepo.findById(candidate)
        }

        return createId("user", ::findExisting)
    }

    fun getById(userId: String): UserEntity {
        return userEntityRepo.findById(userId).orElseGet { error("${userId} not found") }
    }

    fun searchById(userId: String): UserEntity? {
        return userEntityRepo.findById(userId).getOrNull()
    }

    fun findAll(): List<UserEntity> {
        return userEntityRepo.findAll().toList()
    }

    fun save(userEntity: UserEntity): UserEntity {
        if (userEntity.type == UserType.SYSTEM) error("Cannot change system user")
        return userEntityRepo.save(userEntity)
    }

    fun delete(userId: String) {
        val user = getById(userId)
        userEntityRepo.delete(user)
    }

    fun findByExternalId(externalId: String): UserEntity? {
        return userEntityRepo.findByExternalId(externalId)
    }



    fun createUserForTest(name: String, type: UserType = UserType.HACKER): UserEntity {
        val existingUser = userEntityRepo.findByNameIgnoreCase(name)
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
        return userEntityRepo.save(newUserEntity)
    }

    fun deleteTestUserIfExists(name: String) {
        val id = testUserId(name)
        if (userEntityRepo.existsById(id)) {
            userEntityRepo.deleteById(id)
        }
    }


    private fun testUserId(name: String): String {
        return name
    }

    fun getSystemUser(): UserEntity {
        return userEntityRepo.findByNameIgnoreCase("system") ?: error("System user not found")
    }

}
