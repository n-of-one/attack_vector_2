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
 * V9
 *
 * Fix typo in UserTag enum: REGUlAR -> REGULAR
 */
@Component
class V9_Typos(
) : MigrationStep {

    override val version = 9

    private val logger = KotlinLogging.logger {}

    override
    fun migrate(db: MongoDatabase): String {

        fixUserTypeTypo(db)
        removeTemplateCharacterName(db)
        return "Fixed UserEntity.Regular typo and remove template character name."
    }

    private fun fixUserTypeTypo(db: MongoDatabase) {
        val userCollection = db.getCollection("userEntity")
        val filter: Bson = Filters.eq("tag", "REGUlAR")
        val result: FindIterable<Document> = userCollection.find(filter)
        var count = 0
        result.forEach {
            userCollection.updateOne(it, Document("\$set", Document("tag", "REGULAR")))
            count++
        }
        logger.info { "Updated ${count} user documents with userTag REGUlAR to REGULAR." }
    }

    private fun removeTemplateCharacterName(db: MongoDatabase) {
        val userCollection = db.getCollection("hacker")
        val filter: Bson = Filters.eq("characterName", "template")
        val result: FindIterable<Document> = userCollection.find(filter)
        var count = 0
        result.forEach {
            userCollection.updateOne(it, Document("\$set", Document("characterName", "")))
            count++
        }
        logger.info { "Remove the character name from the template hacker ${count} times." }
    }
}

