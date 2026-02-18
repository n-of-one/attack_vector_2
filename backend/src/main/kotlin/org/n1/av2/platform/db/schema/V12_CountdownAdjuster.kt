package org.n1.av2.platform.db.schema

import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters
import mu.KotlinLogging
import org.bson.Document
import org.n1.av2.platform.db.MigrationStep
import org.springframework.stereotype.Component

/**
 * V12
 *
 * Refactor ShutdownAmplifier to TimerAdjuster
 */
@Component
class V12_CountdownAdjuster : MigrationStep {

    override val version = 12

    private val logger = KotlinLogging.logger {}

    override
    fun migrate(db: MongoDatabase): String {
        updateNodeCollection(db)
        return "Change ShutdownAmplifierLayer to TimerAdjusterLayer."
    }

    private fun updateNodeCollection(db: MongoDatabase) {
        val collection = db.getCollection("node")

        logger.info { "Upgrading collection: node" }

        val oldClass = "org.n1.av2.layer.other.shutdownAccelerator.ShutdownAcceleratorLayer"
        val newClass = "org.n1.av2.layer.other.timeradjuster.TimerAdjusterLayer"

        // Find documents needing update
        val filter = Filters.eq("layers._class", oldClass)
        val documentsToUpdate = collection.find(filter).into(mutableListOf())

        for (doc in documentsToUpdate) {
            val layers = doc.getList("layers", Document::class.java)

            layers.forEach { layer ->
                if (layer.getString("_class") == oldClass) {
                    val increaseValue = layer.get("increase")
                    layer.remove("increase")
                    layer["amount"] = increaseValue

                    layer["_class"] = newClass
                    layer["type"] = "TIMER_ADJUSTER"
                    layer["adjustmentType"] = "SPEED_UP"
                    layer["recurring"] = "EVERY_ENTRY"

                    if ( layer["name"] == "Shutdown amplifier") {
                        layer["name"] = "Timer accelerator"
                    }
                }
            }

            val id = doc["_id"]
            collection.replaceOne(Filters.eq("_id", id), doc)
        }

        logger.info { "Updated ${documentsToUpdate.size} documents in collection: node" }

    }
}
