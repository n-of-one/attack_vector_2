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
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.n1.av2.hacker.hacker.Hacker
import org.n1.av2.hacker.hacker.HackerEntityService
import org.n1.av2.hacker.skill.SkillService
import org.n1.av2.hacker.skill.SkillType
import org.n1.av2.platform.config.ConfigItem
import org.n1.av2.platform.config.ConfigService
import org.n1.av2.platform.iam.user.HackerIcon
import org.n1.av2.platform.iam.user.UserEntity
import org.n1.av2.platform.iam.user.UserEntityService
import org.n1.av2.platform.iam.user.UserType
import org.springframework.security.oauth2.jwt.JwtException
import java.net.InetSocketAddress
import java.time.Instant
import java.util.Date

private const val CLIENT_ID = "attack_vector"

/**
 * Tests the OIDC login flow against a real (Nimbus) JWT decoder.
 * A local http server plays the role of the Keycloak JWKS endpoint, so that
 * signature verification and key rotation are exercised for real.
 * The token and logout endpoints are not part of what is tested here, so the HttpClient is mocked.
 */
class OpenIdConnectServiceTest {

    private val signingKey: RSAKey = RSAKeyGenerator(2048).keyID("key-1").generate()
    private var publishedKeys: List<RSAKey> = emptyList()

    private lateinit var jwksServer: HttpServer
    private lateinit var issuer: String
    private lateinit var protocolUrl: String

    private val configService = mockk<ConfigService>()
    private val userEntityService = mockk<UserEntityService>()
    private val hackerEntityService = mockk<HackerEntityService>()
    private val skillService = mockk<SkillService>()
    private val httpClient = mockk<HttpClient>()

    private lateinit var service: OpenIdConnectService

    @BeforeEach
    fun setup() {
        publishedKeys = listOf(signingKey)
        jwksServer = HttpServer.create(InetSocketAddress(0), 0)
        jwksServer.createContext("/realms/larp/protocol/openid-connect/certs") { exchange ->
            val body = JWKSet(publishedKeys.map { it.toPublicJWK() }).toString().toByteArray()
            exchange.responseHeaders.add("Content-Type", "application/json")
            exchange.sendResponseHeaders(200, body.size.toLong())
            exchange.responseBody.use { it.write(body) }
        }
        jwksServer.start()

        issuer = "http://localhost:${jwksServer.address.port}/realms/larp"
        protocolUrl = "$issuer/protocol/openid-connect"

        every { configService.get(ConfigItem.LARP_SPECIFIC_OPENID_CONNECT_URL) } returns protocolUrl
        every { configService.get(ConfigItem.LARP_SPECIFIC_OPENID_CONNECT_CLIENT_ID) } returns CLIENT_ID

        service = OpenIdConnectService(
            configService,
            userEntityService,
            hackerEntityService,
            skillService,
            httpClient,
            OpenIdConnectJwtDecoderProvider(configService),
        )
    }

    @AfterEach
    fun tearDown() {
        jwksServer.stop(0)
    }

    @Test
    fun `login verifies the token and extracts user info`() {
        givenIdpReturnsToken(buildToken())
        givenNoExistingUserOrHacker()

        val user = service.login("auth-code", "http://localhost/callback")!!

        assertThat(user.externalId).isEqualTo("external-id-1")
        assertThat(user.name).isEqualTo("neo")
        assertThat(user.type).isEqualTo(UserType.HACKER)
        verify { hackerEntityService.createHacker(user, HackerIcon.FROG, "The One") }
        verify { skillService.addSkillsForUser(user, listOf(SkillType.SEARCH_SITE, SkillType.SCAN)) }
    }

    @Test
    fun `GM realm role results in a GM user without hacker`() {
        givenIdpReturnsToken(buildToken(clientRoles = emptyList(), realmRoles = listOf("GM")))
        givenNoExistingUserOrHacker()

        val user = service.login("auth-code", "http://localhost/callback")!!

        assertThat(user.type).isEqualTo(UserType.GM)
        verify(exactly = 0) { hackerEntityService.createHacker(any(), any(), any()) }
    }

    @Test
    fun `token signed with a forged key is rejected`() {
        val forgedKey = RSAKeyGenerator(2048).keyID(signingKey.keyID).generate() // same kid, different key
        givenIdpReturnsToken(buildToken(key = forgedKey))

        assertThatThrownBy { service.login("auth-code", "http://localhost/callback") }.isInstanceOf(JwtException::class.java)

        verify(exactly = 0) { userEntityService.createUser(any(), any(), any(), any()) }
    }

    @Test
    fun `expired token is rejected`() {
        givenIdpReturnsToken(buildToken(expiresAt = Instant.now().minusSeconds(120)))

        assertThatThrownBy { service.login("auth-code", "http://localhost/callback") }.isInstanceOf(JwtException::class.java)

        verify(exactly = 0) { userEntityService.createUser(any(), any(), any(), any()) }
    }

    @Test
    fun `token signed with a rotated key is accepted`() {
        givenNoExistingUserOrHacker()
        givenIdpReturnsToken(buildToken())
        service.login("auth-code", "http://localhost/callback") // primes the decoder with the old key

        val rotatedKey = RSAKeyGenerator(2048).keyID("key-2").generate()
        publishedKeys = listOf(signingKey, rotatedKey)
        givenIdpReturnsToken(buildToken(key = rotatedKey))

        val user = service.login("auth-code", "http://localhost/callback")!!

        assertThat(user.externalId).isEqualTo("external-id-1")
    }

    private fun buildToken(
        key: RSAKey = signingKey,
        clientRoles: List<String> = listOf("HACKER"),
        realmRoles: List<String> = emptyList(),
        expiresAt: Instant = Instant.now().plusSeconds(300),
    ): String {
        val claims = JWTClaimsSet.Builder()
            .issuer(issuer)
            .subject("external-id-1")
            .issueTime(Date.from(Instant.now().minusSeconds(10)))
            .expirationTime(Date.from(expiresAt))
            .claim("azp", CLIENT_ID)
            .claim("preferred_username", "neo")
            .claim("character_name", "The One")
            .claim("realm_access", mapOf("roles" to realmRoles))
            .claim("resource_access", mapOf(CLIENT_ID to mapOf("roles" to clientRoles)))
            .build()
        val jwt = SignedJWT(
            JWSHeader.Builder(JWSAlgorithm.RS256).keyID(key.keyID).type(JOSEObjectType.JWT).build(),
            claims,
        )
        jwt.sign(RSASSASigner(key))
        return jwt.serialize()
    }

    private fun givenIdpReturnsToken(accessToken: String) {
        every { httpClient.post(match { it.endsWith("/token") }, any(), any(), any()) } returns
            HttpClient.Response(200, """{"access_token":"$accessToken","refresh_token":"refresh-token-1"}""")
        every { httpClient.post(match { it.endsWith("/logout") }, any(), any(), any()) } returns
            HttpClient.Response(204, "")
    }

    private fun givenNoExistingUserOrHacker() {
        every { userEntityService.findByExternalId(any()) } returns null
        every { userEntityService.findFreeUserName(any()) } answers { firstArg() }
        every { userEntityService.createUser(any(), any(), any(), any()) } answers {
            UserEntity(id = "user-1", name = firstArg(), type = secondArg(), externalId = thirdArg())
        }
        every { hackerEntityService.findForUserOrNull(any()) } returns null
        every { hackerEntityService.createHacker(any(), any(), any()) } just Runs
        every { hackerEntityService.findForUser(any()) } returns Hacker("hacker-1", "user-1", HackerIcon.FROG, "The One")
        every { skillService.addSkillsForUser(any(), any()) } just Runs
    }
}
