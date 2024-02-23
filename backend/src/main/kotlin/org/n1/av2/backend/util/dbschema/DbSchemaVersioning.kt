package org.n1.av2.backend.util.dbschema

import com.mongodb.client.MongoClient
import com.mongodb.client.MongoDatabase
import org.n1.av2.backend.config.ServerConfig
import org.n1.av2.backend.entity.util.DbSchemaVersion
import org.n1.av2.backend.entity.util.DbSchemaVersionRepository
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
    private val config: ServerConfig,
    private val mongoClient: MongoClient,
    private val migrationSteps: List<MigrationStep>
) {

    private val logger = mu.KotlinLogging.logger {}

    @PostConstruct
    fun upgrade() {
        val latestVersion = dbSchemaVersionRepository.findAll().maxByOrNull { it.version }?.version ?: 0
        val maxVersion = migrationSteps.maxOfOrNull { it.version() } ?: 0

        if (latestVersion == maxVersion) {
            logger.info("Database schema is up to date")
            return
        }


        val session = mongoClient.startSession()
        val db = mongoClient.getDatabase(config.mongoDbName)

        migrationSteps.sortedBy { it.version() }
            .forEach { step ->
                val version = step.version()
                if (latestVersion > version) {
                    return@forEach
                }

                logger.info("Upgrading to version $version")
                val description = session.withTransaction {
                    step.migrate(db)
                }

                val versionRecord = DbSchemaVersion(version, description, ZonedDateTime.now())
                logger.info(" Upgraded to version $version : $description")
                dbSchemaVersionRepository.save(versionRecord)
            }
    }
}
