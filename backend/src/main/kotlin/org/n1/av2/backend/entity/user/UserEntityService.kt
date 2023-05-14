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


    fun findByNameIgnoreCase(userName: String): User? {
        return userRepo.findByNameIgnoreCase(userName)
    }

    fun getByName(userName: String): User {
        return findByNameIgnoreCase(userName) ?: throw UsernameNotFoundException("Username not found: ${userName}")
    }

    @PostConstruct
    fun createMandatoryUsers() {
        mandatoryUser("admin", "", UserType.ADMIN)
//        mandatoryUser("gm", "", UserType.GM)
//        mandatoryUser("hacker", "hacker@mailinator.com", UserType.HACKER, HackerIcon.CROCODILE)
//        mandatoryUser("h", "h@mailinator.com", UserType.HACKER, HackerIcon.KOALA)
//        mandatoryUser("Stalker", "stalker@mailinator.com", UserType.HACKER, HackerIcon.BEAR)
//        mandatoryUser("Obsidian", "obsidian@mailinator.com", UserType.HACKER, HackerIcon.BIRD_1)
//        mandatoryUser("Paradox", "paradox@mailinator.com", UserType.HACKER, HackerIcon.BULL)
//        mandatoryUser("Shade_zero", "Shade_zero@mailinator.com", UserType.HACKER, HackerIcon.EAGLE)
//        mandatoryUser("eternity", "eternity@mailinator.com", UserType.HACKER, HackerIcon.COBRA)
//        mandatoryUser("BoltBishop", "boltbishop@mailinator.com", UserType.HACKER, HackerIcon.DRAGON_1)
//        mandatoryUser("CryptoLaw", "cryptoLaw@mailinator.com", UserType.HACKER, HackerIcon.FROG)
//        mandatoryUser("Moonshine", "moonshine@mailinator.com", UserType.HACKER, HackerIcon.LION)
//        mandatoryUser("Angler", "angler@mailinator.com", UserType.HACKER, HackerIcon.SHARK)
//        mandatoryUser("N1X", "N1X@mailinator.com", UserType.HACKER, HackerIcon.STINGRAY)
//        mandatoryUser("Face.dread", "face.dread@mailinator.com", UserType.HACKER, HackerIcon.LIZARD)
//        mandatoryUser("Silver", "silver@mailinator.com", UserType.HACKER, HackerIcon.COBRA)
        mandatoryUser("C_H_I_E_F", "c_h_i_e_f@mailinator.com", UserType.HACKER_MANAGER, HackerIcon.WOLF)
        mandatoryUser("Specter", "specter@mailinator.com", UserType.HACKER, HackerIcon.UNICORN)
    }


    fun mandatoryUser(userName: String, email: String, type: UserType, icon: HackerIcon = HackerIcon.NOT) {
        val user = findByNameIgnoreCase(userName)
        if (user != null) return

        val hacker = if (type == UserType.HACKER_MANAGER || type == UserType.HACKER) Hacker(
            icon = icon,
            skill = HackerSkill(5, 0, 0),
            playerName = "anonymous",
            characterName = "unknown"
        )
        else null

        val newUser = User(
            name = userName,
            type = type,
            id = createUserId(),
            hacker = hacker,
            email = email
        )
        userRepo.save(newUser)
    }

    fun createUserId(): String {
        fun findExisting(candidate: String): Optional<User> {
            return userRepo.findById(candidate)
        }

        return createId("user", ::findExisting)
    }

    fun getById(userId: String): User {
        return userRepo.findById(userId).orElseGet { error("${userId} not found") }
    }

    fun findAll(): List<User> {
        return userRepo.findAll().toList()
    }

    fun save(user: User): User {
        return userRepo.save(user)
    }

    fun delete(userId: String) {
        val user = getById(userId)
        userRepo.delete(user)
    }


}
