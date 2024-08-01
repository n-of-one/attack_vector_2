package org.n1.av2.platform.iam.login

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import io.jsonwebtoken.JwtParser
import io.jsonwebtoken.Jwts
import org.n1.av2.platform.config.ServerConfig
import org.springframework.stereotype.Service
import java.net.URI
import java.security.MessageDigest
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.*

@Service
class GoogleOauthService(
    private val serverConfig: ServerConfig,
) {
    private val objectMapper = ObjectMapper()
    private val jwtParserByKeyId: MutableMap<String, JwtParser> = HashMap()

    fun parse(jwt: String): String {
        val (sub, email) = validateJwt(jwt)
        val googleId = "google-id:$sub:$email:${serverConfig.googleClientId}"
        val hashBytes = MessageDigest.getInstance("SHA-512").digest(googleId.toByteArray())
        val externalId = Base64.getEncoder().encodeToString(hashBytes)
        return externalId
    }

    private fun validateJwt(jwt: String): Pair<String, String> {
        val headerString = jwt.split(".")[0]
        val headerNode = parseToJson(headerString)
        val keyId = headerNode.get("kid").asText()
        val jwtParser = getParser(keyId)
        val claims = jwtParser.parseSignedClaims(jwt).payload

        if (!claims.containsKey("sub")) error("No sub field in JWT, cannot login")
        if (!claims.containsKey("email")) error("No email field in JWT, cannot login")
        val aud = (claims.get("aud") as Set<*>).firstOrNull() as String? ?: error("No aud field in JWT, cannot login")
        if (aud != serverConfig.googleClientId) error("Google oauth jwt not intended for AV. aud=$aud")
        val sub = claims["sub"] as String
        val email = claims["email"] as String
        return Pair(sub, email)
    }

    private fun parseToJson(base64Encoded: String): JsonNode {
        val decodedBytes = Base64.getDecoder().decode(base64Encoded)
        val json = String(decodedBytes, Charsets.UTF_8)
        val root = objectMapper.readTree(json)
        return root
    }

    private fun getParser(kid: String): JwtParser {
        if (jwtParserByKeyId.containsKey(kid)) {
            return jwtParserByKeyId[kid]!!
        }
        loadGooglePublicKeys()
        return jwtParserByKeyId[kid] ?: error("Google oath key not found: $kid")
    }

    private fun loadGooglePublicKeys() {
        jwtParserByKeyId.clear()
        val root = objectMapper.readTree(URI("https://www.googleapis.com/oauth2/v1/certs").toURL())
        val keyIds = root.fieldNames().asSequence().toList()
        keyIds.forEach { keyId ->
            val pem = root.get(keyId).asText()
            val certificateFactory = CertificateFactory.getInstance("X.509")
            val certificates = certificateFactory.generateCertificates(pem.byteInputStream())
            val firstCertificate = certificates.firstOrNull() as? X509Certificate ?: error("no cert found for keyId: $keyId")
            val jwtParser = Jwts.parser().verifyWith(firstCertificate.publicKey).build()

            jwtParserByKeyId[keyId] = jwtParser
        }
    }
}
