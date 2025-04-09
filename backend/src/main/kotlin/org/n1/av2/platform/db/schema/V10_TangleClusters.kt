package org.n1.av2.platform.db.schema

import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters.*
import com.mongodb.client.model.UpdateOptions
import com.mongodb.client.model.Updates.unset
import mu.KotlinLogging
import org.bson.Document
import org.bson.conversions.Bson
import org.n1.av2.platform.db.MigrationStep
import org.springframework.stereotype.Component

/**
 * V10
 *
 * Make tangle
 */
@Component
class V10_TangleClusters(
) : MigrationStep {

    override val version = 10

    private val logger = KotlinLogging.logger {}

    override
    fun migrate(db: MongoDatabase): String {

        removeClustersFromTangleLayers(db)
        removeClustersIceStatistics(db)
        return "Removed clusters from Tangle ICE layers and statistics."
    }

    fun removeClustersFromTangleLayers(db: MongoDatabase) {
        val collection = db.getCollection("node")

        val filter: Bson = elemMatch("layers", and(eq("type", "TANGLE_ICE"), exists("clusters")))

        val update: Bson = unset("layers.$[layer].clusters")

        val arrayFilters = listOf(
            Document("layer.type", "TANGLE_ICE").append("layer.clusters", Document("\$exists", true))
        )

        val updateOptions = UpdateOptions().arrayFilters(arrayFilters)

        val result = collection.updateMany(filter, update, updateOptions)

        logger.info { "Removed tangle clusters properties from node layers: ${result.matchedCount} times." }
    }

    fun removeClustersIceStatistics(db: MongoDatabase) {
        val collection = db.getCollection("iceHackStatistic")

        val matchAllDocuments = Document()
        val update: Bson = unset("tangleClusters")

        val result = collection.updateMany(matchAllDocuments, update)

        logger.info { "Removed tangle clusters properties from statistics: ${result.matchedCount} times." }
    }

}

