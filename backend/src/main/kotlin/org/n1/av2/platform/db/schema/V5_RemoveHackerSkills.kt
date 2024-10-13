package org.n1.av2.platform.db.schema

import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Updates
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

        return "Changed UserEntity: removed obsolete skill fields."
    }

    private fun alterUserEntities(db: MongoDatabase) {
        val userEntities = db.getCollection("userEntity")

        val updateUserEntityFields = Updates.combine(
            Updates.unset("hacker.skill"),
        )
        val updateResult = userEntities.updateMany(allDocuments, updateUserEntityFields)
        logger.info("Updated ${updateResult.modifiedCount} UserEntity documents")
    }
}
