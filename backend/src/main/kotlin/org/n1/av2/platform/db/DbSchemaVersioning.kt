package org.n1.av2.platform.db

import com.mongodb.client.MongoClient
import com.mongodb.client.MongoDatabase
import org.n1.av2.platform.config.StaticConfig
import org.springframework.stereotype.Service
import java.time.ZonedDateTime
import javax.annotation.PostConstruct


interface MigrationStep {
    fun version(): Int
    fun migrate(db: MongoDatabase): String
}


@Service("DbSchemaVersioning")
class DbSchemaVersioning(
    private val dbSchemaVersionRepository: DbSchemaVersionRepository,
    private val mongoClient: MongoClient,
    private val migrationSteps: List<MigrationStep>,
    private val staticConfig: StaticConfig,
) {

    private val logger = mu.KotlinLogging.logger {}

    @PostConstruct
    fun upgrade() {
        val existingVersions = dbSchemaVersionRepository.findAll()
        val currentDbVersion = existingVersions.maxByOrNull { it.version }?.version ?: 0
        val maxVersion = migrationSteps.maxOfOrNull { it.version() } ?: 0

        if (currentDbVersion == maxVersion) {
            logger.info("Database schema is up to date (v${currentDbVersion})")
            return
        }

        val db = mongoClient.getDatabase(staticConfig.mongoDbName)

        migrationSteps.sortedBy { it.version() }
            .filter { it.version() > currentDbVersion }
            .forEach { step ->
                val version = step.version()

                logger.info("Upgrading to version $version")
                val description = step.migrate(db)

                val versionRecord = DbSchemaVersion(version, description, ZonedDateTime.now())
                logger.info(" Upgraded to version $version : $description")
                dbSchemaVersionRepository.save(versionRecord)
            }
    }
}
