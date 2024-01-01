package org.n1.av2.backend

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import io.jsonwebtoken.Jwts
import org.junit.jupiter.api.Test
import java.net.URL
import java.security.SecureRandom
import java.security.Security
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.*
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import kotlin.system.measureNanoTime


class SimpleTest {

    /*
    https://developers.google.com/identity/sign-in/web/sign-in
    https://www.googleapis.com/oauth2/v1/certs
     */

    @Test
    fun test() {
        Security.setProperty("crypto.policy", "unlimited");
        val maxKeySize = Cipher.getMaxAllowedKeyLength("AES")
        println("Max Key Size for AES : $maxKeySize")
        val keyUrl = URL("https://www.googleapis.com/oauth2/v1/certs")
        val objectMapper = ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        val root = objectMapper.readTree(keyUrl)
        val keyIds = root.fieldNames().asSequence().toList()
        val keyMap = keyIds.map { keyId ->
            val pem = root.get(keyId).asText()
            val certificateFactory = CertificateFactory.getInstance("X.509")
            val certificates = certificateFactory.generateCertificates(pem.byteInputStream())
            val firstCertificate = certificates.firstOrNull() as? X509Certificate ?: error("no cert found")

            keyId to firstCertificate.publicKey
        }.toMap()

        val key = keyMap["456b52c81e36fead259231a6947e040e03ea1262"]!!


        val jwtParser = Jwts.parserBuilder().setSigningKey(key).build()

        val jwt =
            "eyJhbGciOiJSUzI1NiIsImtpZCI6IjQ1NmI1MmM4MWUzNmZlYWQyNTkyMzFhNjk0N2UwNDBlMDNlYTEyNjIiLCJ0eXAiOiJKV1QifQ.eyJpc3MiOiJodHRwczovL2FjY291bnRzLmdvb2dsZS5jb20iLCJhenAiOiI2NDMyNjAxNzI3OTMtZTUwbHFjZGcxMGp1OHM4OTNsM3UyMmx0aTltMTJzdnAuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJhdWQiOiI2NDMyNjAxNzI3OTMtZTUwbHFjZGcxMGp1OHM4OTNsM3UyMmx0aTltMTJzdnAuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJzdWIiOiIxMDc1MzA2NDEzNTM5NTYwODExOTgiLCJlbWFpbCI6ImVyaWsuYWFkLnZpc3NlckBnbWFpbC5jb20iLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwibmJmIjoxNzAzOTMwNDk4LCJuYW1lIjoiRXJpayBWaXNzZXIiLCJnaXZlbl9uYW1lIjoiRXJpayIsImZhbWlseV9uYW1lIjoiVmlzc2VyIiwibG9jYWxlIjoiZW4iLCJpYXQiOjE3MDM5MzA3OTgsImV4cCI6MTcwMzkzNDM5OCwianRpIjoiMGUwOWZhMTZiMzE4NzdkY2U1YzJiMzdmMjU3MGE2ZGZjMjM5MzFlMyJ9.OK9Cq-OVH5MyIganbG39jcLlEkiNClenDA0x5aJQRNmWq6AJNbRXmLxdz-eojcq2MpAEZ-o_Z53IFMB5GblCS6ZLLYq2n7g15NB6jdayHhwRbZh2dW8RSd01TZDQPGSW5XiKjHcGR1Itx1oXEEhGp4E48BqpSjY0-bcQFSElTAQIRSF5Xqrv6i4svGd2aQVnuZsL0Pl-_TMPEZAFlvMTyqj2eyqGjgOa9iM1yr5XBAmk56oX6aVO9XRPiOba6eRyZkP0bpX-Ns2hbjlK7GicJ-KO3bxRyXV3JQHuRkxIRRV8I-_sW1uw3dwJgg1tCcZIR17G7frTwKe7YvyBZrZr4w"

        val claims = jwtParser.parseClaimsJws(jwt)
        println(claims.body.get("email"))

    }

    @Test
    fun test2() {
        val userSalt = createSalt()
        val password = "erik.aad.visser@gmail.com".toCharArray()
        val secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512")

        for (i in 1..3) {
            val iterations = 300_000 // Adjust the number of iterations according to your security requirements
            val nanos = measureNanoTime {
                val keyLength = 256 // Key length in bits

                val keySpec = PBEKeySpec(password, userSalt, iterations, keyLength)
                val encoded = secretKeyFactory.generateSecret(keySpec).encoded
                val base64 = Base64.getEncoder().encodeToString(encoded)
                println(base64)
            }
            val millis = nanos / 1000000
            println("iterations: $iterations, millis: $millis")
        }
    }

    private fun createSalt(): ByteArray {
        val salt = ByteArray(32)
        val random = SecureRandom()
        random.nextBytes(salt)
        return salt
    }


}