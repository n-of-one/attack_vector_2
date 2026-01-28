package org.n1.av2.platform.db.schema

import com.mongodb.client.FindIterable
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Updates
import org.bson.Document
import org.n1.av2.hacker.skill.SkillType
import org.n1.av2.larp.frontier.LOLA_USER_NAME
import org.n1.av2.platform.db.MigrationStep
import org.n1.av2.platform.iam.user.UserTag
import org.springframework.stereotype.Component

/**
 * V5
 *
 * Refactoring how hacker skills are modelled and stored in the database.
 */
@Component
class V05_HackerSkills() : MigrationStep {

    override val version = 5

    private val logger = mu.KotlinLogging.logger {}

    override
    fun migrate(db: MongoDatabase): String {
        createHackerCollection(db)
        removeHackerPartFromUserEntity(db)
        addRolesToUserEntities(db)

        return "Moved hacker fields from UserEntity to Hacker collection."
    }

    private fun createHackerCollection(db: MongoDatabase) {
        val hackerUserEntities = findHackerUserEntities(db)
        val hackerDocuments = createHackerDocuments(hackerUserEntities)


        if (hackerDocuments.isNotEmpty()) {
            val hackers = db.getCollection("hacker")
            hackers.insertMany(hackerDocuments)
        }

        logger.info("Created ${hackerDocuments.size} hacker entities")
    }

    private fun findHackerUserEntities(db: MongoDatabase): FindIterable<Document> {
        val userEntitiesCollection = db.getCollection("userEntity")
        val matchTypeHacker = Document().append("type", "HACKER")
        val hackerUserEntities = userEntitiesCollection.find(matchTypeHacker)
        return hackerUserEntities
    }

    private fun createHackerDocuments(userEntities: FindIterable<Document>): List<Document> {
        return userEntities.map { document: Document ->
            val userId = document.getString("_id")
            val hackerInfo = document.get("hacker") as Document?

            if (hackerInfo == null) { // in case the database was created after V5. In that case no migration is needed.
                null
            } else {
                val characterName = hackerInfo.getString("characterName")
                val icon = hackerInfo.getString("icon")

                val defaultSkills = listOf(
                    Document("type", SkillType.SCAN.name),
                    Document("type", SkillType.SEARCH_SITE.name),
                )

                val hackerId = userId.replaceBefore("-", "hacker")
                Document("_id", hackerId)
                    .append("hackerUserId", userId)
                    .append("icon", icon)
                    .append("characterName", characterName)
                    .append("skills", defaultSkills)
            }
        }.filterNotNull().toList()
    }

    private fun removeHackerPartFromUserEntity(db: MongoDatabase) {
        val userEntitiesCollection = db.getCollection("userEntity")
        val matchTypeHacker = Document().append("type", "HACKER")
        val update = Updates.unset("hacker")

        var updatedCount = userEntitiesCollection.updateMany(matchTypeHacker, update).matchedCount

        logger.info("Removed hacker part from ${updatedCount} user entities")
    }

    private fun addRolesToUserEntities(db: MongoDatabase) {
        val userEntities = db.getCollection("userEntity")

        var userCount = 0L
        userEntities.find().forEach { document: Document ->
            val userId = document.getString("_id")
            val name = document.getString("name")
            val tag = when (name.lowercase()) {
                "system" -> UserTag.MANDATORY
                "admin" -> UserTag.MANDATORY
                "gm" -> UserTag.MANDATORY
                LOLA_USER_NAME -> UserTag.EXTERNAL_SYSTEM
                "template" -> UserTag.SKILL_TEMPLATE
                else -> UserTag.REGULAR
            }

            userCount += userEntities.updateOne(
                Document("_id", userId),
                Document("\$set", Document("tag", tag))
            ).matchedCount
        }

        logger.info("Added tags to ${userCount} user entities")
    }


}
