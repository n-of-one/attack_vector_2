package org.n1.av2.platform.iam.login

import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.n1.av2.platform.config.ConfigItem
import org.n1.av2.platform.config.ConfigService
import org.n1.av2.platform.iam.authentication.expirationInS
import org.n1.av2.platform.inputvalidation.SafeJwt
import org.n1.av2.platform.inputvalidation.SafeString
import org.n1.av2.platform.util.getIp
import org.springframework.web.bind.annotation.*

@RestController
class LoginRestController(
    private val configService: ConfigService,
    private val loginService: LoginService,
) {

    private val logger = mu.KotlinLogging.logger {}

    class LoginInput(@get:SafeString val name: String, @get:SafeString val password: String)
    class LoginResponse(val success: Boolean, val message: String? = null)

    @PostMapping("/openapi/login")
    fun login(@RequestBody @Valid input: LoginInput, request: HttpServletRequest, response: HttpServletResponse): LoginResponse {
        val ip = getIp(request)
        try {
            val cookies = loginService.login(input.name, input.password, ip)
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

    @GetMapping("/login-oidc")
    fun openIdConnectAuthenticate(
        request: HttpServletRequest,
        response: HttpServletResponse,
        @RequestParam next: String?
    ): String {
        val redirectUri = buildOpenIdConnectCallbackUri(request, next)
        response.sendRedirect(loginService.getOpenIdConnectLoginUrl(redirectUri))
        return ""
    }

    @GetMapping("/openapi/login/oidc")
    fun openIdConnectCallback(
        request: HttpServletRequest,
        response: HttpServletResponse,
        @RequestParam code: String,
        @RequestParam next: String?
    ): String {
        val redirectUri = buildOpenIdConnectCallbackUri(request, next)
        val loginCookies = loginService.openIdConnectLogin(code, redirectUri)
        if (loginCookies.isEmpty()) {
            response.sendRedirect(appendNext("/login-oidc", next))
            return ""
        }
        response.addLoginCookies(loginCookies)
        response.sendRedirect(next ?: "/")
        return ""
    }

    private fun buildOpenIdConnectCallbackUri(request: HttpServletRequest, next: String?): String {
        val redirectUri = request.scheme + "://" + request.serverName + ":" + request.serverPort + "/openapi/login/oidc"
        return appendNext(redirectUri, next)
    }

    private fun appendNext(uri: String, next: String?): String {
        if (next == null) {
            return uri
        }
        return "$uri?next=$next"
    }

}

fun HttpServletResponse.addLoginCookies(cookies: List<Cookie>) {
    cookies.forEach { cookie ->
        cookie.path = "/"
        cookie.maxAge = expirationInS
        this.addCookie(cookie)
    }
}
