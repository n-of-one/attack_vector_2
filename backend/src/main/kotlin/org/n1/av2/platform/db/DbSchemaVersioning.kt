package org.n1.av2.platform.db

import com.mongodb.client.MongoClient
import com.mongodb.client.MongoDatabase
import org.n1.av2.platform.config.ServerConfig
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import java.time.ZonedDateTime


interface MigrationStep {
    val version: Int
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

    @EventListener(ContextRefreshedEvent::class)
    fun upgrade() {
        val existingVersions = dbSchemaVersionRepository.findAll()
        val currentDbVersion = existingVersions.maxByOrNull { it.version }?.version ?: 0

        val stepsToRun = filterStepsToRun(currentDbVersion)

        if (stepsToRun.isEmpty()) {
            logger.info { "Database schema is up to date (v${currentDbVersion})" }
        } else {
            val db = mongoClient.getDatabase(config.mongoDbName)
            stepsToRun.forEach { runUpgradeStep(it, db) }
        }
    }

    fun filterStepsToRun(currentDbVersion: Int): List<MigrationStep> {
        return migrationSteps
            .filter { it.version > currentDbVersion }
            .sortedBy { it.version }
    }

    private fun runUpgradeStep(step: MigrationStep, db: MongoDatabase) {
        val version = step.version

        logger.info { "Upgrading to version $version" }

        val description = step.migrate(db)

        val versionRecord = DbSchemaVersion(version, description, ZonedDateTime.now())
        dbSchemaVersionRepository.save(versionRecord)
        logger.info { " Upgraded to version $version : $description" }
    }
}
