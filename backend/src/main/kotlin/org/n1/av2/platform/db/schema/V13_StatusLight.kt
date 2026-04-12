package org.n1.av2.platform.db.schema

import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters
import mu.KotlinLogging
import org.bson.Document
import org.n1.av2.platform.db.MigrationStep
import org.springframework.stereotype.Component

/**
 * V13
 *
 * Prepare status light layer for multiple options.
 */
@Component
class V13_StatusLight : MigrationStep {

    override val version = 13

    private val logger = KotlinLogging.logger {}

    override
    fun migrate(db: MongoDatabase): String {
        updateNodeCollection(db)
        return "Change status light layer for multiple options."
    }

    private fun updateNodeCollection(db: MongoDatabase) {
        val collection = db.getCollection("node")

        logger.info { "Upgrading collection: node" }

        val `class` = "org.n1.av2.layer.app.status_light.StatusLightLayer"

        // Find documents needing update
        val filter = Filters.eq("layers._class", `class`)
        val documentsToUpdate = collection.find(filter).into(mutableListOf())

        for (doc in documentsToUpdate) {
            val layers = doc.getList("layers", Document::class.java)

            layers.forEach { layer ->
                if (layer.getString("_class") == `class`) {
                    layer.remove("appId") // no longer used, status is stored in the layer.

                    val option1Text = layer.getString("textForRed")
                    val option2Text = layer.getString("textForGreen")
                    layer.remove("textForRed")
                    layer.remove("textForGreen")

                    val option1 = Document()
                        .append("color", "red")
                        .append("text", option1Text)

                    val option2 = Document()
                        .append("color", "lime")
                        .append("text", option2Text)

                    layer["options"] = listOf(option1, option2)

                    val status = layer.getBoolean("status", false)
                    val currentOption = if (status) 1 else 0
                    layer["currentOption"] = currentOption
                    layer.remove("status")

                    layer["switchLabel"] = "Switch"
                }
            }

            val id = doc["_id"]
            collection.replaceOne(Filters.eq("_id", id), doc)
        }

        logger.info { "Updated ${documentsToUpdate.size} documents in collection: node" }

    }
}
