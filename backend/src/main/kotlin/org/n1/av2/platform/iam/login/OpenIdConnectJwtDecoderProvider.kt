package org.n1.av2.platform.iam.login

import io.github.oshai.kotlinlogging.KotlinLogging
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
 * Provides a JwtDecoder backed by the JWKS endpoint of the OIDC provider (Keycloak).
 *
 * The OIDC URL is a runtime admin config value, not a static Spring property, so the
 * decoder is built lazily from the current config value and rebuilt when the URL changes.
 * Nimbus caches the JWKS keys and re-fetches them on an unknown kid, so key rotation
 * by the OIDC provider is handled automatically.
 */
@Component
class OpenIdConnectJwtDecoderProvider(
    private val configService: ConfigService,
) {
    private val logger = KotlinLogging.logger {}

    @Volatile
    private var cachedUrl: String? = null

    @Volatile
    private var cachedDecoder: JwtDecoder? = null

    fun decoder(): JwtDecoder {
        val protocolUrl = configService.get(ConfigItem.LARP_SPECIFIC_OPENID_CONNECT_URL)
        require(protocolUrl.isNotBlank()) { "OIDC URL not configured" }

        cachedDecoder?.let { if (cachedUrl == protocolUrl) return it }

        // the config stores the issuer with the /protocol/openid-connect suffix
        val issuer = protocolUrl.removeSuffix("/protocol/openid-connect")
        val clientId = configService.get(ConfigItem.LARP_SPECIFIC_OPENID_CONNECT_CLIENT_ID)

        val decoder = NimbusJwtDecoder
            .withJwkSetUri("$protocolUrl/certs")
            .build()

        decoder.setJwtValidator(
            DelegatingOAuth2TokenValidator(
                JwtValidators.createDefaultWithIssuer(issuer), // exp, nbf, iss
                azpValidator(clientId),
            )
        )

        cachedUrl = protocolUrl
        cachedDecoder = decoder
        logger.info { "Built OIDC JWT decoder for issuer $issuer" }
        return decoder
    }

    /* Keycloak access tokens carry the client id in the azp claim, not in aud */
    private fun azpValidator(clientId: String) = OAuth2TokenValidator<Jwt> { jwt ->
        if (jwt.getClaimAsString("azp") == clientId) {
            OAuth2TokenValidatorResult.success()
        } else {
            OAuth2TokenValidatorResult.failure(OAuth2Error("invalid_token", "Unexpected azp", null))
        }
    }
}
