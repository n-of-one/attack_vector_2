package org.n1.av2.backend.config

import org.n1.av2.backend.service.larp.Larp
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.io.File
import java.time.ZoneId

@Component
class ServerConfig(

    @Value("\${ENVIRONMENT:default}")
    val environment: String,

    @Value("\${LARP:GENERIC}")
    val larp: Larp,

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

    @Value("\${LOCAL_CONTENT_FOLDER:local}")
    val localContentFolder: String,


    ) {
    val timeZoneId: ZoneId = if (timeZoneInput == "default") ZoneId.systemDefault() else ZoneId.of(timeZoneInput)

    val dev: Boolean = environment.startsWith("dev")

    private val logger = mu.KotlinLogging.logger {}

    init {
        logger.info("Larp: ${larp}")
        val file = File(localContentFolder)
        if (!file.exists() || !file.isDirectory) {
            logger.error("Local content folder does not exist: '${localContentFolder}' -> '${file.canonicalPath}' . Downloading local files is disabled")
        }
    }
}
