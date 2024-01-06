package org.n1.av2.backend.web.rest

import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import org.n1.av2.backend.config.ServerConfig
import org.n1.av2.backend.config.security.expirationInS
import org.n1.av2.backend.entity.user.UserEntityService
import org.n1.av2.backend.service.security.GoogleOauthService
import org.n1.av2.backend.service.security.LoginService
import org.n1.av2.backend.service.user.UserService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.web.bind.annotation.*


@RestController
class LoginController(
    private val userEntityService: UserEntityService,
    private val serverConfig: ServerConfig,
    private val loginService: LoginService,
    private val googleOauthService: GoogleOauthService,
    private val userService: UserService,
) {

    class LoginInput(val name: String)
    class LoginResponse(val success: Boolean, val message: String? = null)

    @PostMapping("/openapi/login")
    fun login(@RequestBody input: LoginInput, response: HttpServletResponse): LoginResponse {
        try {
            val user = userEntityService.getByName(input.name)
            val cookies = loginService.login(user)
            response.addLoginCookies(cookies)

            return LoginResponse(true)
        } catch (exception: UsernameNotFoundException) {
            return LoginResponse(false, "UseName or password invalid")
        }
    }

    @GetMapping("/openapi/google/clientId")
    fun googleClientId(): String {
        return serverConfig.googleClientId
    }

    class GoogleJwtToken(val jwt: String)

    @RequestMapping("/openapi/login/google")
    fun googleAuthenticate(@RequestBody request: GoogleJwtToken, response: HttpServletResponse): LoginResponse {
        try {
            val externalId = googleOauthService.parse(request.jwt)
            val user = userService.getOrCreateUser(externalId)

            val loginCookies = loginService.login(user)
            response.addLoginCookies(loginCookies)

            return LoginResponse(true)
        } catch (exception: Exception) {
            return LoginResponse(false, exception.message)
        }
    }

}

fun HttpServletResponse.addLoginCookies(cookies: List<Cookie>) {
    cookies.forEach { cookie ->
        cookie.path = "/"
        cookie.maxAge = expirationInS
        this.addCookie(cookie)
    }


}