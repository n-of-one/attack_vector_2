package org.n1.av2.platform.iam.login

import com.nimbusds.jose.JOSEObjectType
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import com.sun.net.httpserver.HttpServer
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.n1.av2.platform.config.ConfigItem
import org.n1.av2.platform.config.ConfigService
import org.springframework.security.oauth2.jwt.JwtException
import java.net.InetSocketAddress
import java.security.MessageDigest
import java.time.Instant
import java.util.Base64
import java.util.Date

private const val CLIENT_ID = "google-client-id-1.apps.googleusercontent.com"

/**
 * Tests the Google login token handling against a real (Nimbus) JWT decoder.
 * A local http server plays the role of the Google JWKS endpoint, so that
 * signature verification and key rotation are exercised for real.
 */
class GoogleOauthServiceTest {

    private val signingKey: RSAKey = RSAKeyGenerator(2048).keyID("key-1").generate()
    private var publishedKeys: List<RSAKey> = emptyList()

    private lateinit var jwksServer: HttpServer
    private lateinit var issuer: String

    private val configService = mockk<ConfigService>()

    private lateinit var service: GoogleOauthService

    @BeforeEach
    fun setup() {
        publishedKeys = listOf(signingKey)
        jwksServer = HttpServer.create(InetSocketAddress(0), 0)
        jwksServer.createContext("/oauth2/v3/certs") { exchange ->
            val body = JWKSet(publishedKeys.map { it.toPublicJWK() }).toString().toByteArray()
            exchange.responseHeaders.add("Content-Type", "application/json")
            exchange.sendResponseHeaders(200, body.size.toLong())
            exchange.responseBody.use { it.write(body) }
        }
        jwksServer.start()

        issuer = "http://localhost:${jwksServer.address.port}"

        every { configService.get(ConfigItem.LOGIN_GOOGLE_CLIENT_ID) } returns CLIENT_ID

        val decoderProvider = GoogleJwtDecoderProvider(configService)
        decoderProvider.jwksUri = "$issuer/oauth2/v3/certs"
        decoderProvider.issuers = setOf(issuer)

        service = GoogleOauthService(configService, decoderProvider)
    }

    @AfterEach
    fun tearDown() {
        jwksServer.stop(0)
    }

    @Test
    fun `parse verifies the token and derives a stable external id`() {
        val externalId = service.parse(buildToken())

        val expected = Base64.getEncoder().encodeToString(
            MessageDigest.getInstance("SHA-512").digest("google-id:sub-1:neo@example.com:$CLIENT_ID".toByteArray())
        )
        assertThat(externalId).isEqualTo(expected)
    }

    @Test
    fun `token signed with a forged key is rejected`() {
        val forgedKey = RSAKeyGenerator(2048).keyID(signingKey.keyID).generate() // same kid, different key

        assertThatThrownBy { service.parse(buildToken(key = forgedKey)) }.isInstanceOf(JwtException::class.java)
    }

    @Test
    fun `expired token is rejected`() {
        assertThatThrownBy { service.parse(buildToken(expiresAt = Instant.now().minusSeconds(120))) }.isInstanceOf(JwtException::class.java)
    }

    @Test
    fun `token for another client id is rejected`() {
        assertThatThrownBy { service.parse(buildToken(audience = "other-client-id")) }.isInstanceOf(JwtException::class.java)
    }

    @Test
    fun `token from another issuer is rejected`() {
        assertThatThrownBy { service.parse(buildToken(tokenIssuer = "https://evil.example.com")) }.isInstanceOf(JwtException::class.java)
    }

    @Test
    fun `token signed with a rotated key is accepted`() {
        service.parse(buildToken()) // primes the decoder with the old key

        val rotatedKey = RSAKeyGenerator(2048).keyID("key-2").generate()
        publishedKeys = listOf(signingKey, rotatedKey)

        val externalId = service.parse(buildToken(key = rotatedKey))

        val expected = Base64.getEncoder().encodeToString(
            MessageDigest.getInstance("SHA-512").digest("google-id:sub-1:neo@example.com:$CLIENT_ID".toByteArray())
        )
        assertThat(externalId).isEqualTo(expected)
    }

    private fun buildToken(
        key: RSAKey = signingKey,
        audience: String = CLIENT_ID,
        tokenIssuer: String? = null,
        expiresAt: Instant = Instant.now().plusSeconds(300),
    ): String {
        val claims = JWTClaimsSet.Builder()
            .issuer(tokenIssuer ?: issuer)
            .subject("sub-1")
            .audience(audience)
            .issueTime(Date.from(Instant.now().minusSeconds(10)))
            .expirationTime(Date.from(expiresAt))
            .claim("email", "neo@example.com")
            .build()
        val jwt = SignedJWT(
            JWSHeader.Builder(JWSAlgorithm.RS256).keyID(key.keyID).type(JOSEObjectType.JWT).build(),
            claims,
        )
        jwt.sign(RSASSASigner(key))
        return jwt.serialize()
    }
}
