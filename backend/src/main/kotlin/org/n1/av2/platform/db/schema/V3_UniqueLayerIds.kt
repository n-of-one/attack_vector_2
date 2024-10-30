package org.n1.av2.platform.db.schema

import com.mongodb.DuplicateKeyException
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.IndexOptions
import org.bson.Document
import org.n1.av2.platform.db.MigrationStep
import org.springframework.stereotype.Component

/**
 * V3
 *
 * Adds a unique index to the layerId field in the iceStatus collections.
 * In case there are duplicate layerIds, a random record is kept and the rest are removed.
 */
@Component
class V3_UniqueLayerIds : MigrationStep {

    override val version = 3

    override
    fun migrate(db: MongoDatabase): String {
        UniqueConstraintAdder(db, "icePasswordStatus", "layerId").addUniqueConstraint()
        return "Added unique index to layerId in iceStatus collections"
    }
}

class UniqueConstraintAdder(
    db: MongoDatabase,
    private val collectionName: String,
    private val uniqueFieldName: String
) {

    private val logger = mu.KotlinLogging.logger {}

    val collection = db.getCollection(collectionName)

    fun addUniqueConstraint() {
        try {
            createUniqueIndex()
            logger.info { "Added unique index to $uniqueFieldName in $collectionName collection" }
        } catch (_: DuplicateKeyException) {
            fixCollidingLayerIds()
            createUniqueIndex()
        }
    }

    private fun createUniqueIndex() {
        collection.createIndex(Document(uniqueFieldName, 1), IndexOptions().unique(true))
    }

    private fun fixCollidingLayerIds() {
        val collisionMap = findCollidingDocuments()

        collisionMap.forEach { entry ->
            val key = entry.key
            val documents: List<Document> = entry.value

            documents
                .drop(1) // keep the first (arbitrary)
                .forEach { collidingDocument: Document ->
                    collection.deleteOne(collidingDocument)
                    logger.info { "Removed entry from $collectionName collection with $key to resolve duplications" }
                }
        }
    }

    fun findCollidingDocuments(): Map<String, List<Document>> {
        return collection.find()
            .map { document ->
                val layerId = document.getString(uniqueFieldName)
                layerId to document
            }
            .groupBy({ it.first }, { it.second })
            .filter { entry: Map.Entry<String, List<Document>> -> entry.value.size > 1 }
    }

}
