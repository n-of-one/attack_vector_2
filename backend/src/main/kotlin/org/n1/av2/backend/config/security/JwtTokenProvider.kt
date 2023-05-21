package org.n1.av2.backend.config.security

import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.UnsupportedJwtException
import io.jsonwebtoken.security.Keys
import org.n1.av2.backend.entity.user.User
import org.springframework.stereotype.Component
import java.security.Key
import java.security.SignatureException
import java.util.*


@Component
class JwtTokenProvider {

    private val logger = mu.KotlinLogging.logger {}

    private val jwtSecret: String = "SuperSecretKeyForHS512NeedsToBeLongEnoughToContainAtLeast512BitsOfEntropySoThisShouldDoIt"
    private val key: Key = Keys.hmacShaKeyFor(jwtSecret.toByteArray())
    private val JwtParser = Jwts.parserBuilder().setSigningKey(key).build()


    val jwtExpirationInS: Int = 60 * 60 * 5 // 5 hours
    private val jwtExpirationInMs: Int = 1000 * jwtExpirationInS

    fun generateJwt(user: User): String {

        val now = Date()
        val expiryDate = Date(now.time + jwtExpirationInMs)

        return Jwts.builder()
                .setSubject(user.id)
                .setIssuedAt(Date())
                .setExpiration(expiryDate)
                .signWith(key)
                .compact()
    }

    fun getUserIdFromJWT(token: String): String {
        val claims = JwtParser
                .parseClaimsJws(token)
                .body
        return claims.subject
    }

    fun validateToken(authToken: String): Boolean {
        try {
            JwtParser. parseClaimsJws(authToken)
            return true
        } catch (ex: SignatureException) {
            logger.error("Invalid JWT signature")
        } catch (ex: MalformedJwtException) {
            logger.error("Invalid JWT token")
        } catch (ex: ExpiredJwtException) {
            logger.error("Expired JWT token")
        } catch (ex: UnsupportedJwtException) {
            logger.error("Unsupported JWT token")
        } catch (ex: IllegalArgumentException) {
            logger.error("JWT claims string is empty.")
        }

        return false
    }

}