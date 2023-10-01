package org.n1.av2.backend.web.html

import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.n1.av2.backend.config.security.JwtTokenProvider
import org.n1.av2.backend.entity.user.*
import org.n1.av2.backend.model.validation.LoginRedirectParam
import org.n1.av2.backend.service.FrontierHackerInfo
import org.n1.av2.backend.service.FrontierService
import org.n1.av2.backend.service.LoginService
import org.n1.av2.backend.service.user.UserService
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody

@Controller
class SsoController(
    private val frontierService: FrontierService,
    private val loginService: LoginService,
    private val userEntityService: UserEntityService,
    private val jwtTokenProvider: JwtTokenProvider,
    private val userService: UserService,
) {


    @GetMapping("/sso")
    @ResponseBody
    fun sso(request: HttpServletRequest, response: HttpServletResponse, @LoginRedirectParam @RequestParam next: String?): String {
        val cookies = request.cookies ?: emptyArray()
        val frontierHackerInfo =
            frontierService.getFrontierHackerInfo(cookies) ?: return "Please log in to <a href=\"https://eosfrontier.space\" target=\"eosFrontier\">Eos Frontier</a> first, and then come back to this page."

        val user = getOrCreateUser(frontierHackerInfo)
        val updatedUser = updateUserInfo(user, frontierHackerInfo)

        val loginCookies = loginService.login(updatedUser)
        loginCookies.forEach { addCookie(it, response) }

        val redirectPath = next ?: "/"
        response.sendRedirect(redirectPath)
        return ""
    }

    private fun getOrCreateUser(hackerInfo: FrontierHackerInfo): UserEntity {
        val existingUserEntity: UserEntity? = userEntityService.findByExternalId(hackerInfo.id)
        if (existingUserEntity != null) {
            return existingUserEntity
        }

        if (hackerInfo.isGm) {
            return userService.create(hackerInfo.id, hackerInfo.id, UserType.GM)
        }

        val name = findFreeUserName(hackerInfo.characterName!!)

        val hacker = Hacker(
            icon = HackerIcon.FROG,
            skill = HackerSkill(0,0,0),
            characterName = "not yet set"
        )

        return userService.create(name, hackerInfo.id, UserType.HACKER, hacker)
    }

    private fun updateUserInfo(userEntity: UserEntity, hackerInfo: FrontierHackerInfo): UserEntity {
        if (hackerInfo.isGm) return userEntity

        val hacker = userEntity.hacker!!
        val skills = hackerInfo.skills!!

        val updatedHacker = hacker.copy(
            characterName = hackerInfo.characterName!!,
            skill = HackerSkill(hacker = skills.hacker, elite = skills.elite, architect = skills.architect),
        )
        val updatedUser = userEntity.copy(hacker = updatedHacker)
        return userService.update(updatedUser)
    }

    private fun addCookie(cookie: Cookie, response: HttpServletResponse) {
        cookie.path = "/"
        cookie.maxAge = jwtTokenProvider.jwtExpirationInS
        response.addCookie(cookie)
    }

    private fun findFreeUserName(input: String): String {
        if (userEntityService.findByNameIgnoreCase(input) == null) return input

        for (i in 1..100) {
            val name = "$input$i"
            if (userEntityService.findByNameIgnoreCase(name) == null) return name

        }
        error("Failed to logon, failed to create user account. No free user name found")
    }
}

