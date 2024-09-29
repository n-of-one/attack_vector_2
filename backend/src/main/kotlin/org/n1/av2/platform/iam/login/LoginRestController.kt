package org.n1.av2.platform.iam.login

import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.n1.av2.platform.config.ConfigItem
import org.n1.av2.platform.config.ConfigService
import org.n1.av2.platform.iam.authentication.expirationInS
import org.n1.av2.platform.inputvalidation.SafeJwt
import org.n1.av2.platform.inputvalidation.SafeString
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class LoginRestController(
    private val configService: ConfigService,
    private val loginService: LoginService,
) {

    private val logger = mu.KotlinLogging.logger {}

    class LoginInput(@get:SafeString val name: String, @get:SafeString val password: String)
    class LoginResponse(val success: Boolean, val message: String? = null)

    @PostMapping("/openapi/login")
    fun login(@RequestBody @Valid input: LoginInput, response: HttpServletResponse): LoginResponse {
        try {
            val cookies = loginService.login(input.name, input.password)
            response.addLoginCookies(cookies)

            return LoginResponse(true)
        } catch (exception: Exception) {
            return LoginResponse(false, exception.message)
        }
    }

    class GoogleClientIdResponse(val clientId: String)
    @GetMapping("/openapi/google/clientId")
    fun googleClientId(): GoogleClientIdResponse {
        return GoogleClientIdResponse(configService.get(ConfigItem.LOGIN_GOOGLE_CLIENT_ID))
    }

    class GoogleJwtToken(@get:SafeJwt val jwt: String)
    @PostMapping("/openapi/login/google")
    fun googleAuthenticate(@RequestBody @Valid request: GoogleJwtToken, response: HttpServletResponse): LoginResponse {
        try {
            val loginCookies = loginService.googleLogin(request.jwt)
            response.addLoginCookies(loginCookies)

            return LoginResponse(true)
        } catch (exception: Exception) {
            logger.error("Google login failed", exception)
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
