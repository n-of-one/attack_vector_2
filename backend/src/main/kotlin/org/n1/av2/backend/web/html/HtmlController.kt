package org.n1.av2.backend.web.html

import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import org.n1.av2.backend.service.security.LoginService
import org.springframework.boot.web.servlet.error.ErrorController
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping


private const val INDEX = "../static/index.html"

@Controller
class HtmlController(
    private val loginService: LoginService
) : ErrorController {

    @GetMapping("/", "/login", "/adminLogin", "/loggedOut", "/about", "/privacy", "/hacker", "/hacker/", "/gm", "/gm/",
        "/edit", "/edit/", "/edit/{siteId}", "/x/{reference}", "/o/{reference}", "/website", "/website/{page}", "/larp/**")
    fun default(): String {
        return INDEX
    }

    @GetMapping("/localLogout")
    fun logout(response: HttpServletResponse) {
        val logoutCookies = loginService.logout()
        logoutCookies.forEach {
            val cookie = Cookie(it.name, "")
            cookie.path = "/"
            cookie.maxAge = 0
            response.addCookie(cookie)
        }

        response.sendRedirect("/loggedOut")
    }

    @GetMapping("/forceError")
    fun forceError(): String {
        throw RuntimeException("intentional problem for test.")
    }
}