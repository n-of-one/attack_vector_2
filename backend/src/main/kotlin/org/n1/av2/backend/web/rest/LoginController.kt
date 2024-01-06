package org.n1.av2.backend.web.rest

import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import org.n1.av2.backend.config.ServerConfig
import org.n1.av2.backend.config.security.expirationInS
import org.n1.av2.backend.service.security.LoginService
import org.springframework.web.bind.annotation.*

@RestController
class LoginController(
    private val serverConfig: ServerConfig,
    private val loginService: LoginService,
) {

    class LoginInput(val name: String, val password: String)
    class LoginResponse(val success: Boolean, val message: String? = null)

    @PostMapping("/openapi/login")
    fun login(@RequestBody input: LoginInput, response: HttpServletResponse): LoginResponse {
        try {
            val cookies = loginService.login(input.name, input.password)
            response.addLoginCookies(cookies)

            return LoginResponse(true)
        } catch (exception: Exception) {
            return LoginResponse(false, exception.message)
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
            val loginCookies = loginService.googleLogin(request.jwt)
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
