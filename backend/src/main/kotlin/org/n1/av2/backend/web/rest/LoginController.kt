package org.n1.av2.backend.web.rest

import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import org.n1.av2.backend.config.ServerConfig
import org.n1.av2.backend.config.security.JwtTokenProvider
import org.n1.av2.backend.config.security.expirationInS
import org.n1.av2.backend.entity.user.UserEntityService
import org.n1.av2.backend.service.security.LoginService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.web.bind.annotation.*


@RestController
class LoginController(
    private val userEntityService: UserEntityService,
    private val serverConfig: ServerConfig,
    private val loginService: LoginService,
    ) {

    class LoginInput(val name: String)
    class LoginResponse(val success:Boolean, val message: String? = null)
    @PostMapping("/api/login")
    fun login(@RequestBody input: LoginInput, response: HttpServletResponse): LoginResponse {
        try {
            val user = userEntityService.getByName(input.name)
            val cookies = loginService.login(user)
            response.addLoginCookies(cookies)

            return LoginResponse(true )
        }
        catch (exception: UsernameNotFoundException) {
            return LoginResponse(false, "UseName or password invalid")
        }
    }

    @GetMapping("/openapi/google/clientId")
    fun googleClientId(): String {
        return serverConfig.googleClientId
    }

}

fun HttpServletResponse.addLoginCookies(cookies: List<Cookie>) {
    cookies.forEach { cookie ->
        cookie.path = "/"
        cookie.maxAge = expirationInS
        this.addCookie(cookie)
    }


}