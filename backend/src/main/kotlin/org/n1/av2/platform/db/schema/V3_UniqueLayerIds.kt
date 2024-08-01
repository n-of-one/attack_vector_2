package org.n1.av2.platform.db.schema

import com.mongodb.DuplicateKeyException
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.IndexOptions
import org.bson.Document
import org.n1.av2.platform.db.MigrationStep
import org.springframework.stereotype.Component

@Component
class V3_UniqueLayerIds : MigrationStep {

    override
    fun version() = 3

    private val logger = mu.KotlinLogging.logger {}

    override
    fun migrate(db: MongoDatabase): String {
        addUniqueConstraint(db, "icePasswordStatus")
        return "Added unique index to layerId in iceStatus collections"
    }

    fun addUniqueConstraint(db: MongoDatabase, collectionName: String) {

        try {
            db.getCollection(collectionName).createIndex(Document("layerId", 1), IndexOptions().unique(true))
            logger.info("Added unique index to layerId in $collectionName collection")
        }
        catch (exception: DuplicateKeyException) {
            val collection = db.getCollection(collectionName)
            val uniqueLayerIds = HashSet<String>()
            val nonUniqueLayerIds = HashSet<String>()
            collection.find().forEach { document ->
                val layerId = document.getString("layerId")
                if (uniqueLayerIds.contains(layerId)) {
                    nonUniqueLayerIds.add(layerId)
                    logger.info("Duplicate layerId found in $collectionName collection: $layerId")
                }
                else {
                    uniqueLayerIds.add(layerId)
                }
            }
            nonUniqueLayerIds.forEach { layerId: String ->
                collection.find().forEach { document: Document ->
                    collection.deleteOne(document)
                    logger.info("Removed ice status from $collectionName collection with $layerId")
                }
            }
            db.getCollection(collectionName).createIndex(Document("layerId", 1), IndexOptions().unique(true))

        }
    }
}
