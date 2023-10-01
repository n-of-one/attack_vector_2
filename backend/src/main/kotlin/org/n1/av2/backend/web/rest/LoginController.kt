package org.n1.av2.backend.web.rest

import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import org.n1.av2.backend.config.security.JwtTokenProvider
import org.n1.av2.backend.entity.user.UserEntityService
import org.n1.av2.backend.service.LoginService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/api")
class LoginController(
    private val userEntityService: UserEntityService,
    private val jwtTokenProvider: JwtTokenProvider,
    private val loginService: LoginService,
    ) {

    data class LoginInput(val name: String)
    data class LoginResponse(val success:Boolean, val message: String? = null)

    @PostMapping("/login")
    fun login(@RequestBody input: LoginInput, response: HttpServletResponse): LoginResponse {
        try {
            val user = userEntityService.getByName(input.name)
            val cookies = loginService.login(user)
            cookies.forEach{ addCookie(it, response)}

            return LoginResponse(true )
        }
        catch (exception: UsernameNotFoundException) {
            return LoginResponse(false, "UseName or password invalid")
        }
    }


    fun addCookie(cookie: Cookie, response: HttpServletResponse) {
        cookie.path = "/"
        cookie.maxAge = jwtTokenProvider.jwtExpirationInS
        response.addCookie(cookie)
    }
}