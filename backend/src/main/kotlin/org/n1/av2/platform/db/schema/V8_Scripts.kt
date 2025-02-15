package org.n1.av2.platform.db.schema

import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Updates
import mu.KotlinLogging
import org.bson.Document
import org.n1.av2.editor.SiteValidationService
import org.n1.av2.platform.db.MigrationStep
import org.n1.av2.platform.iam.user.CurrentUserService
import org.n1.av2.platform.iam.user.DefaultUserService
import org.n1.av2.platform.iam.user.UserEntityService
import org.n1.av2.platform.iam.user.UserType
import org.n1.av2.site.entity.SiteProperties
import org.n1.av2.site.entity.SitePropertiesEntityService
import org.n1.av2.site.tutorial.TUTORIAL_INSTANCE_PREFIX
import org.n1.av2.site.tutorial.TUTORIAL_TEMPLATE_NAME
import org.springframework.stereotype.Component

private val matchAllDocuments = Document() // Match all documents in a mongoDB query

/**
 * V8
 *
 * Delete all timer documents to allow changes to this entity. This should be OK, no game should be updated mid-game.
 */
@Component
class V8_Scripts(
) : MigrationStep {

    override val version = 8

    private val logger = KotlinLogging.logger {}

    override
    fun migrate(db: MongoDatabase): String {
        db.getCollection("timer").drop()

        logger.info { "Dropped collection timer." }

        return "Dropped timer collection to allow restructuring of how timers are defined."
    }
}

