package org.n1.av2.backend.service

import jakarta.servlet.http.Cookie
import org.n1.av2.backend.config.security.JwtTokenProvider
import org.n1.av2.backend.entity.user.UserEntity
import org.springframework.stereotype.Service
import java.net.URLEncoder

@Service
class LoginService(
    private val jwtTokenProvider: JwtTokenProvider,
) {

    fun login(userEntity: UserEntity): List<Cookie> {
        val cookies = mutableListOf<Cookie>()
        cookies.add(generateJwtCookie(userEntity))
        cookies.add(generateUserNameCookie(userEntity))
        cookies.add(generateTypeCookie(userEntity))
        cookies.add(generateRoleCookie(userEntity))

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

    private fun generateUserNameCookie(userEntity: UserEntity): Cookie {
        val encodedUserName = URLEncoder.encode(userEntity.name, "utf-8").replace("+", "%20")
        return Cookie("userName", encodedUserName)
    }

    private fun generateJwtCookie(userEntity: UserEntity): Cookie {
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