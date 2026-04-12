package org.n1.av2.platform.iam.authentication

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.n1.av2.platform.config.ConfigItem
import org.n1.av2.platform.config.ConfigService
import org.n1.av2.platform.iam.user.UserEntity
import org.springframework.stereotype.Component
import java.util.*

const val expirationInS: Int = 60 * 60 * 5 // 5 hours

@Component
class JwtTokenProvider(
    configService: ConfigService,
) {

    private val logger = mu.KotlinLogging.logger {}

    private val jwtSecret = configService.get(ConfigItem.SYSTEM_JWT_SECRET)
    private val key = Keys.hmacShaKeyFor(jwtSecret.toByteArray())
    private val jwtParser = Jwts.parser().verifyWith(key).build()

    private val jwtExpirationInMs: Int = 1000 * expirationInS

    fun generateJwt(userEntity: UserEntity): String {
        val now = Date()
        val expiryDate = Date(now.time + jwtExpirationInMs)
        return generateJwt(userEntity, expiryDate)
    }

    fun generateJwt(userEntity: UserEntity, expiryDate: Date): String {
        return Jwts.builder()
                .subject(userEntity.id)
                .issuedAt(Date())
                .expiration(expiryDate)
                .signWith(key)
                .compact()
    }

    fun getUserIdFromJWT(token: String): String {
        val claims = jwtParser
                .parseSignedClaims(token)
                .payload
        return claims.subject
    }

    fun validateToken(authToken: String): Pair<Boolean, String> {
        try {
            jwtParser.parseSignedClaims(authToken)
            return true to ""
        } catch (ex: Exception) {
            return false to (ex.message ?: "Unknown problem validating JWT token")
        }
    }
}
