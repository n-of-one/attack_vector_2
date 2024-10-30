package org.n1.av2.platform.db.schema

import com.mongodb.client.FindIterable
import com.mongodb.client.MongoDatabase
import org.bson.Document
import org.n1.av2.platform.db.MigrationStep
import org.n1.av2.platform.iam.user.DefaultUserService
import org.springframework.stereotype.Component

@Component
class V5_RemoveHackerSkills(
    private val defaultUserService: DefaultUserService,
) : MigrationStep {

    private val logger = mu.KotlinLogging.logger {}

    private val allDocuments = Document()

    override
    fun version() = 5

    override
    fun migrate(db: MongoDatabase): String {
        alterUserEntities(db)

        return "Moved hacker fields from UserEntity to Hacker collection."
    }

    private fun alterUserEntities(db: MongoDatabase) {
        val userEntities = db.getCollection("userEntity")
        val hackers = db.getCollection("hacker")


        val byTypeHacker = Document().append("type", "HACKER")
        val hackerUserEntities = userEntities.find(byTypeHacker)
        val hackerDocuments = createHackerDocument(hackerUserEntities)
        hackers.insertMany(hackerDocuments)

        logger.info("Created  ${hackerDocuments.size} hacker entities")
    }

    private fun createHackerDocument(userEntities: FindIterable<Document>): List<Document> {
        return userEntities.map { document: Document ->
            val userId = document.getString("_id")
            val hackerInfo = document.get("hacker") as Document
            val characterName = hackerInfo.getString("characterName")
            val icon = hackerInfo.getString("icon")

            val hackerId = userId.replaceBefore("-", "hacker")
            Document("_id", hackerId)
                .append("hackerUserId", userId)
                .append("icon", icon)
                .append("characterName", characterName)
                .append("skills", listOf<String>())
        }.toList()
    }

}
