package org.n1.av2.backend.entity.user

import org.n1.av2.backend.util.createId
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Component
import java.util.*
import javax.annotation.PostConstruct

@Component
class UserEntityService(
    private val userRepo: UserRepo,
) {


    fun findByNameIgnoreCase(userName: String): UserEntity? {
        return userRepo.findByNameIgnoreCase(userName)
    }

    fun getByName(userName: String): UserEntity {
        return findByNameIgnoreCase(userName) ?: throw UsernameNotFoundException("Username not found: ${userName}")
    }

    @PostConstruct
    fun createMandatoryUsers() {
        createUser("system", UserType.SYSTEM)
        createUser("admin", UserType.ADMIN)
        createUser("gm", UserType.GM)
        createUser("hacker", UserType.HACKER, HackerIcon.CROCODILE)
        createUser("Stalker", UserType.HACKER, HackerIcon.BEAR)
        createUser("Paradox", UserType.HACKER, HackerIcon.BULL)
        createUser("Angler", UserType.HACKER, HackerIcon.SHARK)
    }


    private fun createUser(userName: String, type: UserType, icon: HackerIcon = HackerIcon.NOT) {
        val user = findByNameIgnoreCase(userName)
        if (user != null) return

        val hacker = if (type == UserType.HACKER) Hacker(
            icon = icon,
            skill = HackerSkill(5, 0, 0),
            characterName = "unknown"
        )
        else null

        val newUserEntity = UserEntity(
            name = userName,
            type = type,
            id = createUserId(),
            hacker = hacker,
        )
        userRepo.save(newUserEntity)
    }

    fun createUserId(): String {
        fun findExisting(candidate: String): Optional<UserEntity> {
            return userRepo.findById(candidate)
        }

        return createId("user", ::findExisting)
    }

    fun getById(userId: String): UserEntity {
        return userRepo.findById(userId).orElseGet { error("${userId} not found") }
    }

    fun findAll(): List<UserEntity> {
        return userRepo.findAll().toList()
    }

    fun save(userEntity: UserEntity): UserEntity {
        if (userEntity.type == UserType.SYSTEM) error("Cannot change system user")
        return userRepo.save(userEntity)
    }

    fun delete(userId: String) {
        val user = getById(userId)
        userRepo.delete(user)
    }

    fun findByExternalId(externalId: String): UserEntity? {
        return userRepo.findByExternalId(externalId)
    }



    fun createUserForTest(name: String, type: UserType = UserType.HACKER): UserEntity {
        val existingUser = userRepo.findByNameIgnoreCase(name)
        if (existingUser != null) {
            return existingUser
        }

        val newUserEntity = UserEntity(
            id = testUserId(name),
            name = name,
            type = type,
            hacker = Hacker(
                icon = HackerIcon.COBRA,
                skill = HackerSkill(5, 0, 0),
                characterName = "char for ${name}"
            ),
        )
        return userRepo.save(newUserEntity)
    }

    fun deleteTestUserIfExists(name: String) {
        val id = testUserId(name)
        if (userRepo.existsById(id)) {
            userRepo.deleteById(id)
        }
    }


    private fun testUserId(name: String): String {
        return name
    }

    fun getSystemUser(): UserEntity {
        return userRepo.findByNameIgnoreCase("system") ?: error("System user not found")
    }

}
