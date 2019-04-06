package org.n1.mainframe.backend.service.user

import org.n1.mainframe.backend.model.user.User
import org.n1.mainframe.backend.model.user.enums.UserType
import org.n1.mainframe.backend.repo.UserRepo
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
class UserService(
        val userRepo: UserRepo
) {

    val passwordEncoder = BCryptPasswordEncoder(4)

    fun getUserByUserName(userName: String): User {
        return userRepo.findByUserName(userName).orElseThrow { throw UsernameNotFoundException("Invalid username or password.") }
    }

    fun login(userName: String, password: String): User {
        val user = getUserByUserName(userName.toLowerCase())
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
        val admin = userRepo.findByUserName("admin")
        if (!admin.isPresent) {
            val adminUser = User(
                    userName = "admin",
                    type = UserType.ADMIN,
                    id = "-1"
            )
            createUser(adminUser, "77aa44")
        }
    }
}
