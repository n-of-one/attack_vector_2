package org.n1.av2.backend.service.security

import jakarta.servlet.http.Cookie
import org.n1.av2.backend.config.ServerConfig
import org.n1.av2.backend.config.security.JwtTokenProvider
import org.n1.av2.backend.config.websocket.ConnectionType
import org.n1.av2.backend.entity.user.UserEntity
import org.n1.av2.backend.entity.user.UserEntityService
import org.n1.av2.backend.entity.user.UserType
import org.n1.av2.backend.model.iam.UserPrincipal
import org.n1.av2.backend.service.larp.FrontierHackerInfo
import org.n1.av2.backend.service.site.TutorialService
import org.n1.av2.backend.service.user.CurrentUserService
import org.n1.av2.backend.service.user.UserService
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import java.net.URLEncoder

@Service
class LoginService(
    private val userEntityService: UserEntityService,
    private val userService: UserService,
    private val serverConfig: ServerConfig,
    private val jwtTokenProvider: JwtTokenProvider,
    private val googleOauthService: GoogleOauthService,
    private val tutorialService: TutorialService,
    private val currentUserService: CurrentUserService,
) {

    fun login(userName: String, password: String?): List<Cookie> {
        if (!serverConfig.dev) {
            if (serverConfig.adminPassword == "disabled" ) error("Cannot login, no admin password set. Please ask the system administrators to set an admin password.")
            if (password == null || serverConfig.adminPassword != password)error("Wrong password")
        }

        val user = userEntityService.getByName(userName)
        if (user.type == UserType.SYSTEM) error("Cannot login as system user")
        return getCookies(user)
    }


    fun logout(): List<Cookie> {
        val cookies = mutableListOf<Cookie>()
        cookies.add(Cookie("jwt", ""))
        cookies.add(Cookie("userName", ""))
        cookies.add(Cookie("type", ""))
        cookies.add(Cookie("roles", ""))

        return cookies
    }

    fun googleLogin(jwt: String): List<Cookie> {
        val externalId = googleOauthService.parse(jwt)
        val user = userService.getOrCreateUser(externalId)

        // Need to set current user for use in tutorialService
        val authentication = UserPrincipal("", "google-login", user, ConnectionType.STATELESS)
        SecurityContextHolder.getContext().authentication = authentication
        currentUserService.set(user)

        tutorialService.createIfNotExistsFor(user)

        return getCookies(user)
    }

    fun frontierLogin(frontierHackerInfo: FrontierHackerInfo): List<Cookie> {
        val user = userService.getOrCreateUser(frontierHackerInfo)
        val updatedUser = userService.updateUserInfo(user, frontierHackerInfo)
        return getCookies(updatedUser)
    }


    private fun getCookies(userEntity: UserEntity): List<Cookie> {
        val cookies = mutableListOf<Cookie>()
        cookies.add(generateJwtCookie(userEntity))
        cookies.add(generateUserNameCookie(userEntity))
        cookies.add(generateTypeCookie(userEntity))
        cookies.add(generateRoleCookie(userEntity))

        return cookies
    }


    private fun generateUserNameCookie(userEntity: UserEntity): Cookie {
        val encodedUserName = URLEncoder.encode(userEntity.name, "utf-8").replace("+", "%20")
        return Cookie("userName", encodedUserName)
    }

    fun generateJwtCookie(userEntity: UserEntity): Cookie {
        val jwt = jwtTokenProvider.generateJwt(userEntity)
        return Cookie("jwt", jwt)
    }

    private fun generateTypeCookie(userEntity: UserEntity): Cookie {
        val type = userEntity.type.toString()
        return Cookie("type", type)
    }

    private fun generateRoleCookie(userEntity: UserEntity): Cookie {
        val roles = userEntity.type.authorities.joinToString(separator = "|") { it.authority }
        return Cookie("roles", roles)
    }
}
