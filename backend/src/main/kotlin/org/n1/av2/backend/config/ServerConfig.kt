package org.n1.av2.backend.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.ZoneId

@Component
class ServerConfig(

    @Value("\${ENVIRONMENT:default}")
    val environment: String,

    @Value("\${MONGODB_URI:mongodb://attackvector2:attackvector2@localhost/admin?authMechanism=SCRAM-SHA-1}")
    val mongoDbUrl: String,

    @Value("\${MONGODB_NAME:av2}")
    val mongoDbName: String,

    @Value("\${environment.TIME_ZONE:default}")
    private val timeZoneInput: String,

    // SSO Frontier larp
    @Value("\${FRONTIER_ORTHANK_TOKEN:none}")
    val orthankToken: String,

    // Google oauth
    @Value("\${GOOGLE_CLIENT_ID:none}")
    val googleClientId: String,

    // Google oauth
    @Value("\${ADMIN_PASSWORD:disabled}")
    val adminPassword: String,



) {
    val timeZoneId: ZoneId = if (timeZoneInput == "default") ZoneId.systemDefault() else ZoneId.of(timeZoneInput)

    val dev: Boolean = environment.startsWith("dev")
}
