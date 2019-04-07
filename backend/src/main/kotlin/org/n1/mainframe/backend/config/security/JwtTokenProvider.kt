package org.n1.mainframe.backend.config.security

import io.jsonwebtoken.*
import mu.KLogging
import org.n1.mainframe.backend.model.user.User
import org.springframework.stereotype.Component
import java.security.SignatureException
import java.util.*

@Component
class JwtTokenProvider {

    private val jwtSecret: String = "SuperSecretKeyForHS512NeedsToBeLongEnoughToContainAtLeast512BitsOfEntropySoThisShouldDoIt"


    val jwtEpirationInS: Int = 60 * 60 * 5 // 5 hours
    private val jwtExpirationInMs: Int = 1000 * jwtEpirationInS



    companion object : KLogging()

    fun generateJwt(user: User): String {


        val now = Date()
        val expiryDate = Date(now.time + jwtExpirationInMs)

        return Jwts.builder()
                .setSubject(user.userName)
                .setIssuedAt(Date())
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact()
    }

    fun getUserNameFromJWT(token: String): String {
        val claims = Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .body

        return claims.subject
    }

    fun validateToken(authToken: String): Boolean {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(authToken)
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