package org.n1.mainframe.backend.web.rest

import org.n1.mainframe.backend.model.user.UserAuthentication
import org.n1.mainframe.backend.service.user.UserService
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/api")
class LoginController(val userService: UserService) {

    enum class LoginRole { ADMIN, GM, SUPER_USER, USER }
    data class LoginInput(val name: String = "", val password: String = "")
    data class LoginResponse(val token:String?, val role: LoginRole?,  val message: String? = null)

    @PostMapping("/login")
    fun login(@RequestBody input: LoginInput): LoginResponse {
        try {
            val user = userService.login(input.name, input.password)
            val authentication = UserAuthentication(user)
            SecurityContextHolder.getContext().authentication = authentication
            return LoginResponse("token-3923-32424-23423", LoginRole.ADMIN )
        }
        catch (exception: UsernameNotFoundException) {
            return LoginResponse(null, null, exception.message)
        }
    }
}