package org.n1.av2.platform.iam.login.frontier

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.n1.av2.platform.iam.login.LoginService
import org.n1.av2.platform.iam.login.addLoginCookies
import org.n1.av2.platform.inputvalidation.LoginRedirectParam
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody

@Controller
class FrontierHtmlController(
    private val frontierService: FrontierService,
    private val loginService: LoginService,
) {


    @GetMapping("/login-frontier")
    @ResponseBody
    fun frontierSso(request: HttpServletRequest, response: HttpServletResponse, @LoginRedirectParam @RequestParam next: String?): String {
        val cookies = request.cookies ?: emptyArray()
        val frontierHackerInfo = frontierService.getFrontierHackerInfo(cookies)
        if (frontierHackerInfo == null) {
            response.sendRedirect("https://www.eosfrontier.space/return-to-attack-vector")
            return ""
        }
        val loginCookies = loginService.frontierLogin(frontierHackerInfo)
        response.addLoginCookies(loginCookies)

        val redirectPath = next ?: "/"
        response.sendRedirect(redirectPath)
        return ""
    }
}
