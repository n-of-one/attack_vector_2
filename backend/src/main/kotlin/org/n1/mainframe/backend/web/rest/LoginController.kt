package org.n1.mainframe.backend.web.rest

import org.n1.mainframe.backend.config.security.JwtTokenProvider
import org.n1.mainframe.backend.model.user.User
import org.n1.mainframe.backend.service.user.UserService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletResponse


@RestController
@RequestMapping("/api")
class LoginController(
                      private val userService: UserService,
                      private val jwtTokenProvider: JwtTokenProvider) {

    data class LoginInput(val name: String = "", val password: String = "")
    data class LoginResponse(val success:Boolean, val message: String? = null)

    @PostMapping("/login")
    fun login(@RequestBody input: LoginInput, response: HttpServletResponse): LoginResponse {
        try {
            val user = userService.login(input.name, input.password)
            generateJwtCookie(user, response)
            generateUserNameCookie(user, response)
            generateTypeCookie(user, response)
            generateRoleCookie(user, response)

            return LoginResponse(true )
        }
        catch (exception: UsernameNotFoundException) {
            return LoginResponse(false, "UseName or password invalid")
        }
    }

    private fun generateUserNameCookie(user: User, response: HttpServletResponse) {
        addCookie("userName", user.userName, response)
    }

    fun generateJwtCookie(user: User, response: HttpServletResponse) {
        val jwt = jwtTokenProvider.generateJwt(user)
        addCookie("jwt", jwt, response)
    }

    fun generateTypeCookie(user: User, response: HttpServletResponse) {
        val type = user.type.toString()
        addCookie("type", type, response)
    }

    fun generateRoleCookie(user: User, response: HttpServletResponse) {
        val roles = user.type.authorities.joinToString(separator = "|") { it.authority }
        addCookie("roles", roles, response)
    }

    fun addCookie(name: String, value: String, response: HttpServletResponse) {
        val cookie = Cookie(name, value)
        cookie.path = "/"
        cookie.maxAge = jwtTokenProvider.jwtEpirationInS
        response.addCookie(cookie)
    }
}