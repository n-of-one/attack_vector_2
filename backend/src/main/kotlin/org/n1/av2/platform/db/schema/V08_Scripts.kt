package org.n1.av2.platform.db.schema

import com.mongodb.client.FindIterable
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters
import mu.KotlinLogging
import org.bson.Document
import org.bson.conversions.Bson
import org.n1.av2.platform.db.MigrationStep
import org.springframework.stereotype.Component


/**
 * V8
 *
 * Delete all timer documents to allow changes to this entity: package moved.
 *
 * Delete all TangleIceStatus documents to allow changes to this entity: added clusterId to each point.
 * Delete all associated  IcePassword documents and IcePasswordStatus documents so that there are no dangling references.
 */
@Component
class V08_Scripts(
) : MigrationStep {

    override val version = 8

    private val logger = KotlinLogging.logger {}

    override
    fun migrate(db: MongoDatabase): String {
        db.getCollection("timer").drop()
        logger.info { "Dropped collection timer." }

        db.getCollection("tangleIceStatus").drop()
        logger.info { "Dropped collection tangleIceStatus." }

        dropDocuments(db, "icePassword", Filters.regex("iceId", "^tangle"))
        dropDocuments(db, "icePasswordStatus", Filters.regex("_id", "^tangle"))

        return "Dropped timer and tangleIceStatus collections to allow refactoring to support scripts."
    }

    private fun dropDocuments(db: MongoDatabase, collectionName: String, filter: Bson) {
        val collection = db.getCollection(collectionName)
        val result: FindIterable<Document> = collection.find(filter)

        var count = 0
        result.forEach {
            collection.deleteOne(it)
            count++
        }
        logger.info { "Deleted ${count} tangle documents from ${collectionName}." }
    }
}

