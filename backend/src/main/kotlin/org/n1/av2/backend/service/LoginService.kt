package org.n1.av2.backend.service

import jakarta.servlet.http.Cookie
import org.n1.av2.backend.config.security.JwtTokenProvider
import org.n1.av2.backend.entity.user.User
import org.springframework.stereotype.Service
import java.net.URLEncoder

@Service
class LoginService(
    private val jwtTokenProvider: JwtTokenProvider,
) {

    fun login(user: User): List<Cookie> {
        val cookies = mutableListOf<Cookie>()
        cookies.add(generateJwtCookie(user))
        cookies.add(generateUserNameCookie(user))
        cookies.add(generateTypeCookie(user))
        cookies.add(generateRoleCookie(user))

        return cookies
    }

    fun logout(): List<Cookie> {
        val cookies = mutableListOf<Cookie>()
        cookies.add(Cookie("jwt", ""))
        cookies.add(Cookie("userName", ""))
        cookies.add(Cookie("type", ""))
        cookies.add(Cookie("roles", ""))

        return cookies
    }

    private fun generateUserNameCookie(user: User): Cookie {
        val encodedUserName = URLEncoder.encode(user.name, "utf-8").replace("+", "%20")
        return Cookie("userName", encodedUserName)
    }

    private fun generateJwtCookie(user: User): Cookie {
        val jwt = jwtTokenProvider.generateJwt(user)
        return Cookie("jwt", jwt)
    }

    private fun generateTypeCookie(user: User): Cookie {
        val type = user.type.toString()
        return Cookie("type", type)
    }

    private fun generateRoleCookie(user: User): Cookie {
        val roles = user.type.authorities.joinToString(separator = "|") { it.authority }
        return Cookie("roles", roles)
    }

}