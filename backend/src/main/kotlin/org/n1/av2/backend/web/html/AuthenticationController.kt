package org.n1.av2.backend.web.html

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.n1.av2.backend.config.security.JwtTokenProvider
import org.n1.av2.backend.model.validation.LoginRedirectParam
import org.n1.av2.backend.service.larp.FrontierService
import org.n1.av2.backend.service.security.GoogleOauthService
import org.n1.av2.backend.service.security.LoginService
import org.n1.av2.backend.service.user.UserService
import org.n1.av2.backend.web.rest.addLoginCookies
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody

@Controller
class AuthenticationController(
    private val frontierService: FrontierService,
    private val loginService: LoginService,
    private val jwtTokenProvider: JwtTokenProvider,
    private val userService: UserService,
    private val googleOauthService: GoogleOauthService,
    ) {


    @GetMapping("/login/frontier")
    @ResponseBody
    fun frontierSso(request: HttpServletRequest, response: HttpServletResponse, @LoginRedirectParam @RequestParam next: String?): String {
        val cookies = request.cookies ?: emptyArray()
        val frontierHackerInfo = frontierService.getFrontierHackerInfo(cookies)
        if (frontierHackerInfo == null) {
            response.sendRedirect("https://www.eosfrontier.space/return-to-attack-vector")
            return ""
        }
        val user = userService.getOrCreateUser(frontierHackerInfo)
        val updatedUser = userService.updateUserInfo(user, frontierHackerInfo)

        val loginCookies = loginService.login(updatedUser)
        response.addLoginCookies(loginCookies)

        val redirectPath = next ?: "/"
        response.sendRedirect(redirectPath)
        return ""
    }
}
