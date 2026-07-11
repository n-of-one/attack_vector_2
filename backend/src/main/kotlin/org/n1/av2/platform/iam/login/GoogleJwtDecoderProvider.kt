package org.n1.av2.platform.iam.login

import org.n1.av2.platform.config.ConfigItem
import org.n1.av2.platform.config.ConfigService
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator
import org.springframework.security.oauth2.core.OAuth2Error
import org.springframework.security.oauth2.core.OAuth2TokenValidator
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtValidators
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.stereotype.Component

/**
 * Provides a JwtDecoder backed by Google's JWKS endpoint.
 *
 * The Google client id is a runtime admin config value, not a static Spring property, so the
 * decoder is built lazily from the current config value and rebuilt when the client id changes.
 * Nimbus caches the JWKS keys and re-fetches them on an unknown kid, so key rotation
 * by Google is handled automatically.
 */
@Component
class GoogleJwtDecoderProvider(
    private val configService: ConfigService,
) {
    private val logger = mu.KotlinLogging.logger {}

    // overridable for tests
    internal var jwksUri: String = "https://www.googleapis.com/oauth2/v3/certs"

    // Google id tokens use either issuer form, see
    // https://developers.google.com/identity/openid-connect/openid-connect#validatinganidtoken
    internal var issuers: Set<String> = setOf("https://accounts.google.com", "accounts.google.com")

    @Volatile
    private var cachedClientId: String? = null

    @Volatile
    private var cachedDecoder: JwtDecoder? = null

    fun decoder(): JwtDecoder {
        val clientId = configService.get(ConfigItem.LOGIN_GOOGLE_CLIENT_ID)
        require(clientId.isNotBlank()) { "Google client id not configured" }

        cachedDecoder?.let { if (cachedClientId == clientId) return it }

        val decoder = NimbusJwtDecoder
            .withJwkSetUri(jwksUri)
            .build()

        decoder.setJwtValidator(
            DelegatingOAuth2TokenValidator(
                JwtValidators.createDefault(), // exp, nbf
                issuerValidator(),
                audienceValidator(clientId),
            )
        )

        cachedClientId = clientId
        cachedDecoder = decoder
        logger.info { "Built Google JWT decoder for client id $clientId" }
        return decoder
    }

    private fun issuerValidator() = OAuth2TokenValidator<Jwt> { jwt ->
        if (jwt.getClaimAsString("iss") in issuers) {
            OAuth2TokenValidatorResult.success()
        } else {
            OAuth2TokenValidatorResult.failure(OAuth2Error("invalid_token", "Unexpected iss", null))
        }
    }

    /* Google id tokens carry the client id in the aud claim */
    private fun audienceValidator(clientId: String) = OAuth2TokenValidator<Jwt> { jwt ->
        if (jwt.audience?.contains(clientId) == true) {
            OAuth2TokenValidatorResult.success()
        } else {
            OAuth2TokenValidatorResult.failure(OAuth2Error("invalid_token", "Unexpected aud", null))
        }
    }
}
