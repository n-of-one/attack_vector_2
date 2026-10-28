package org.n1.av2.platform.iam.login

import org.n1.av2.platform.config.ConfigItem
import org.n1.av2.platform.config.ConfigService
import org.springframework.stereotype.Service
import java.security.MessageDigest
import java.util.*

@Service
class GoogleOauthService(
    private val configService: ConfigService,
    private val jwtDecoderProvider: GoogleJwtDecoderProvider,
) {

    fun parse(jwtString: String): String {
        val jwt = jwtDecoderProvider.decoder().decode(jwtString) // throws JwtException if invalid

        val sub = jwt.getClaimAsString("sub") ?: error("No sub field in JWT, cannot login")
        val email = jwt.getClaimAsString("email") ?: error("No email field in JWT, cannot login")

        val googleId = "google-id:$sub:$email:${configService.get(ConfigItem.LOGIN_GOOGLE_CLIENT_ID)}"
        val hashBytes = MessageDigest.getInstance("SHA-512").digest(googleId.toByteArray())
        val externalId = Base64.getEncoder().encodeToString(hashBytes)
        return externalId
    }
}
