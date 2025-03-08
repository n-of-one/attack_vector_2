package org.n1.av2.platform.db.schema

import com.mongodb.client.MongoDatabase
import mu.KotlinLogging
import org.n1.av2.platform.db.MigrationStep
import org.springframework.stereotype.Component

/**
 * V8
 *
 * Delete all timer documents to allow changes to this entity. This should be OK, no game should be updated mid-game.
 */
@Component
class V8_Scripts(
) : MigrationStep {

    override val version = 8

    private val logger = KotlinLogging.logger {}

    override
    fun migrate(db: MongoDatabase): String {
        db.getCollection("timer").drop()
        logger.info { "Dropped collection timer." }

        db.getCollection("tangleIceStatus").drop()
        logger.info { "Dropped collection tangleIceStatus." }

        return "Dropped collections to allow refactoring to support scripts."
    }
}

