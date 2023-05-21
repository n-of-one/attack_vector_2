package org.n1.av2.backend.web.html

import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.n1.av2.backend.service.LoginService
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.servlet.error.ErrorAttributes
import org.springframework.boot.web.servlet.error.ErrorController
import org.springframework.stereotype.Component
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseBody
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL


private const val INDEX = "../static/index.html"

@Controller
class HtmlController(
    private val loginService: LoginService
) : ErrorController {

    @GetMapping("/", "/login", "/login/", "/loggedOut", "/hacker", "/hacker/", "/gm", "/gm/", "/edit", "/edit/", "/edit/{siteId}", "ice/{iceId}")
    fun default(): String {
        return INDEX
    }

//    @GetMapping("/about")
//    @ResponseBody
//    fun about(request: HttpServletRequest): String {
//        val cookie = request.cookies.find { it.name == "5d69be776f972f618357ed7009ea7ccb" }?.value ?: return "No joomla cookie found"
//
//
////        return getInfo(cookie)
////        return "about"
//    }

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