package org.n1.mainframe.backend.service.user

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
        return userRepo.findByName(userName).orElseGet { null }
    }

    fun getByName(userName: String): User {
        return findByName(userName) ?: throw UsernameNotFoundException("Invalid username or password.")
    }

    fun login(userName: String, password: String): User {
        val user = getByName(userName.toLowerCase())
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
        mandatoryUser("hacker", "", UserType.HACKER)
        mandatoryUser("h", "", UserType.HACKER)
    }

    fun mandatoryUser(userName: String, password: String, type: UserType) {
        val user = findByName(userName)
        if (user == null) {
            val newUser = User(
                    name = userName,
                    type = type,
                    id = createUserId()
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
