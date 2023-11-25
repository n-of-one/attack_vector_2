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
        createUser("h", "h@mailinator.com", UserType.HACKER, HackerIcon.KOALA)
        createUser("Stalker", "stalker@mailinator.com", UserType.HACKER, HackerIcon.BEAR)
        createUser("Obsidian", "obsidian@mailinator.com", UserType.HACKER, HackerIcon.BIRD_1)
        createUser("Paradox", "paradox@mailinator.com", UserType.HACKER, HackerIcon.BULL)
        createUser("Shade_zero", "Shade_zero@mailinator.com", UserType.HACKER, HackerIcon.EAGLE)
        createUser("eternity", "eternity@mailinator.com", UserType.HACKER, HackerIcon.COBRA)
        createUser("BoltBishop", "boltbishop@mailinator.com", UserType.HACKER, HackerIcon.DRAGON_1)
        createUser("CryptoLaw", "cryptoLaw@mailinator.com", UserType.HACKER, HackerIcon.FROG)
        createUser("Moonshine", "moonshine@mailinator.com", UserType.HACKER, HackerIcon.LION)
        createUser("Angler", "angler@mailinator.com", UserType.HACKER, HackerIcon.SHARK)
        createUser("N1X", "N1X@mailinator.com", UserType.HACKER, HackerIcon.STINGRAY)
        createUser("Face.dread", "face.dread@mailinator.com", UserType.HACKER, HackerIcon.LIZARD)
        createUser("Silver", "silver@mailinator.com", UserType.HACKER, HackerIcon.COBRA)
        createUser("C_H_I_E_F", "c_h_i_e_f@mailinator.com", UserType.HACKER_MANAGER, HackerIcon.WOLF)
        createUser("Specter", "specter@mailinator.com", UserType.HACKER, HackerIcon.UNICORN)
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


}
