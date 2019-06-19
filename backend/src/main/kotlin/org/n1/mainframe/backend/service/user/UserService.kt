package org.n1.mainframe.backend.service.user

import org.n1.mainframe.backend.model.hacker.HackerIcon
import org.n1.mainframe.backend.model.user.User
import org.n1.mainframe.backend.model.user.enums.UserType
import org.n1.mainframe.backend.repo.UserRepo
import org.n1.mainframe.backend.util.createId
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component
import java.util.*
import javax.annotation.PostConstruct

@Component
class UserService(
        val userRepo: UserRepo
) {

    val passwordEncoder = BCryptPasswordEncoder(4)

    fun findByName(userName: String): User? {
        return userRepo.findByNameIgnoreCase(userName)
    }

    fun getByName(userName: String): User {
        return findByName(userName) ?: throw UsernameNotFoundException("Invalid username or password.")
    }

    fun login(userName: String, password: String): User {
        val user = getByName(userName)
        if (passwordEncoder.matches(password, user.encodedPasscoded)) {
            return user
        }
        else {
            throw UsernameNotFoundException("Invalid username or password.")
        }
    }

    fun createUser(userInput: User, password: String): User {
        userInput.encodedPasscoded = passwordEncoder.encode(password )
        userRepo.save(userInput)
        return userInput
    }

    @PostConstruct
    fun init() {
        mandatoryUser("admin", "", UserType.ADMIN)
        mandatoryUser("gm", "", UserType.GM)
        mandatoryUser("hacker", "", UserType.HACKER, HackerIcon.CROCODILE)
        mandatoryUser("h", "", UserType.HACKER, HackerIcon.KOALA)

        mandatoryUser("Stalker", "", UserType.HACKER, HackerIcon.BEAR)
        mandatoryUser("Obsidian", "", UserType.HACKER, HackerIcon.BIRD_1)
        mandatoryUser("Paradox", "", UserType.HACKER, HackerIcon.BULL)
        mandatoryUser("Shade_zero", "", UserType.HACKER, HackerIcon.EAGLE)
        mandatoryUser("_eternity_", "", UserType.HACKER, HackerIcon.COBRA)
        mandatoryUser("BoltBishop", "", UserType.HACKER, HackerIcon.DRAGON_1)
        mandatoryUser("CryptoLaw", "", UserType.HACKER, HackerIcon.FROG)
        mandatoryUser("Moonshine", "", UserType.HACKER, HackerIcon.LION)
        mandatoryUser("Angler", "", UserType.HACKER, HackerIcon.SHARK)
        mandatoryUser("N1X", "", UserType.HACKER, HackerIcon.STINGRAY)
        mandatoryUser("Face.dread" , "", UserType.HACKER, HackerIcon.LIZARD)
        mandatoryUser("-=Silver=-", "", UserType.HACKER, HackerIcon.COBRA)
        mandatoryUser("C_H_I_E_F", "", UserType.HACKER, HackerIcon.WOLF)
        mandatoryUser(".Specter.", "", UserType.HACKER, HackerIcon.UNICORN)



    }

    fun mandatoryUser(userName: String, password: String, type: UserType, icon: HackerIcon = HackerIcon.NOT) {
        val user = findByName(userName)
        if (user == null) {
            val newUser = User(
                    name = userName,
                    type = type,
                    id = createUserId(),
            icon = icon
            )
            createUser(newUser, password)
        }
    }

    fun createUserId(): String {
        fun findExisting(candidate: String): Optional<User> {
            return userRepo.findById(candidate)
        }

        return createId("user", ::findExisting)
    }

    fun purgeAll() {
        userRepo.deleteAll()
        init()
    }

    fun getById(userId: String): User {
        return userRepo.findById(userId).orElseGet { error("${userId} not found") }
    }


}
