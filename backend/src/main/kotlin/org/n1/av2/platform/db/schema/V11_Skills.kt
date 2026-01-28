package org.n1.av2.platform.db.schema

import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Updates.unset
import mu.KotlinLogging
import org.bson.Document
import org.n1.av2.platform.db.MigrationStep
import org.n1.av2.platform.util.createId
import org.springframework.stereotype.Component

/**
 * V11
 *
 * Skills are refactored from being a property of hacker to their own collection.
 */
@Component
class V11_Skills : MigrationStep {

    override val version = 11

    private val logger = KotlinLogging.logger {}

    override
    fun migrate(db: MongoDatabase): String {

        migrateSkills(db)

        return "Migrated skills to their own collection."
    }

    private fun migrateSkills(db: MongoDatabase) {
        val hackerCollection = db.getCollection("hacker")
        val skillCollection = db.getCollection("skill")

        val usedIds = emptySet<String>().toMutableSet()

        var skillsCreated = 0

        hackerCollection.find()
            .asSequence()
            .mapNotNull { hacker ->
                val userId = hacker.getString("hackerUserId") ?: return@mapNotNull null
                val skills = hacker.getList("skills", Document::class.java) ?: return@mapNotNull null

                userId to skills
            }
            .flatMap { (userId, skills) ->
                skills.asSequence()
                    .mapNotNull { skill ->
                        val type: String = skill.getString("type") ?: return@mapNotNull null
                        val value: String? = skill.getString("value")
                        val id = createId("skill") { toTest: String -> if (usedIds.contains(toTest)) toTest else null }
                        usedIds.add(id)

                        Document().apply {
                            put("_id", id)
                            put("userId", userId)
                            put("type", type)
                            put("value", value)
                            put("usedOnSiteIds", emptyList<String>())
                        }
                    }
            }
            .forEach { skillDoc ->
                skillCollection.insertOne(skillDoc)
                skillsCreated++
            }

        val matchAllDocuments = Document()
        val hackersUpdated = hackerCollection.updateMany(matchAllDocuments, unset("skills")).modifiedCount

        logger.info { "Created new skill documents:  $skillsCreated, updated hackers: $hackersUpdated" }
    }
}
