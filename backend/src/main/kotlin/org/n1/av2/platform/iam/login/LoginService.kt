package org.n1.av2.platform.iam.login

import jakarta.servlet.http.Cookie
import org.n1.av2.platform.config.ConfigItem
import org.n1.av2.platform.config.ConfigService
import org.n1.av2.platform.connection.ConnectionType
import org.n1.av2.platform.iam.UserPrincipal
import org.n1.av2.platform.iam.authentication.JwtTokenProvider
import org.n1.av2.platform.iam.user.*
import org.n1.av2.site.tutorial.TutorialService
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import java.net.URLEncoder

@Service
class LoginService(
    private val userEntityService: UserEntityService,
    private val userAndHackerService: UserAndHackerService,
    private val configService: ConfigService,
    private val jwtTokenProvider: JwtTokenProvider,
    private val googleOauthService: GoogleOauthService,
    private val tutorialService: TutorialService,
    private val currentUserService: CurrentUserService,
) {

    fun login(userName: String, password: String): List<Cookie> {
        checkPassword(password)

        val user = userEntityService.getByName(userName)
        if (user.type == UserType.SYSTEM) error("Cannot login as system user")
        return getCookies(user)
    }

    private fun checkPassword(password: String) {
        if (password == "" && configService.get(ConfigItem.LOGIN_PATH) == "/devLogin") return
        if (configService.get(ConfigItem.LOGIN_PASSWORD) != password) error("Wrong password")
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
        val user = userAndHackerService.getOrCreateHackerUser(externalId)

        // Need to set current user for use in tutorialService
        val authentication = UserPrincipal("", "google-login", user, ConnectionType.STATELESS)
        SecurityContextHolder.getContext().authentication = authentication
        currentUserService.set(user)

        tutorialService.createIfNotExistsFor(user)

        return getCookies(user)
    }



    fun getCookies(userEntity: UserEntity): List<Cookie> {
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
