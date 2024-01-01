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
        createUser("admin", "", UserType.ADMIN)
        createUser("gm", "", UserType.GM)
        createUser("hacker", "hacker@mailinator.com", UserType.HACKER, HackerIcon.CROCODILE)
        createUser("Stalker", "stalker@mailinator.com", UserType.HACKER, HackerIcon.BEAR)
        createUser("Paradox", "paradox@mailinator.com", UserType.HACKER, HackerIcon.BULL)
        createUser("Angler", "angler@mailinator.com", UserType.HACKER, HackerIcon.SHARK)
    }


    fun createUser(userName: String, email: String, type: UserType, icon: HackerIcon = HackerIcon.NOT) {
        val user = findByNameIgnoreCase(userName)
        if (user != null) return

        val hacker = if (type == UserType.HACKER_MANAGER || type == UserType.HACKER) Hacker(
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
            email = email,
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
        return userRepo.save(userEntity)
    }

    fun delete(userId: String) {
        val user = getById(userId)
        userRepo.delete(user)
    }

    fun findByExternalId(externalId: String): UserEntity? {
        return userRepo.findByExternalId(externalId)
    }



    fun createUserForTest(name: String) {
        val newUserEntity = UserEntity(
            id = testUserId(name),
            name = name,
            type = UserType.HACKER,
            hacker = Hacker(
                icon = HackerIcon.COBRA,
                skill = HackerSkill(5, 0, 0),
                characterName = "char for ${name}"
            ),
            email = "",
        )
        userRepo.save(newUserEntity)
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

}
