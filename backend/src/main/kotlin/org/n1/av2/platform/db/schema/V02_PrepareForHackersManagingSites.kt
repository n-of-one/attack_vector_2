package org.n1.av2.platform.db.schema

import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Updates
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
 * V2
 *
 * Changes are made to prepare for hackers creating their own sites. In addition, some obsolete fields are removed.
 *
 * - Remove obsolete fields from UserEntity (email, gmNote)
 * - Remove HACKER_MANAGER user type (they are now HACKER)
 * - Remove obsolete fields from SiteProperties (hackTime)
 * - Rename field in SiteProperties (creator -> purpose)
 * - Add fields to SiteProperties (siteStructureOk, ownerUserId) so that we can display these to the user in the UI.
 * - Set the ownerUserId for all sites.
 *    - Tutorial template is owned by System
 *    - Tutorial instances are owned by the user
 *    - All other sites are owned by GM
 */
@Component
class V2_PrepareForHackersManagingSites(
    private val defaultUserService: DefaultUserService,
    private val siteValidationService: SiteValidationService,
    private val sitePropertiesEntityService: SitePropertiesEntityService,
    private val userEntityService: UserEntityService,
    private val currentUserService: CurrentUserService,
) : MigrationStep {

    override val version = 2

    override
    fun migrate(db: MongoDatabase): String {
        V2UserEntityUpdater(db, defaultUserService).updateUserEntities()

        val sitePropertiesUpdater = V2SitePropertiesUpdater(
            db,
            siteValidationService,
            sitePropertiesEntityService,
            userEntityService,
            currentUserService
        )

        sitePropertiesUpdater.alterSiteProperties()
        sitePropertiesUpdater.setSiteOwnerIds()
        sitePropertiesUpdater.updateSiteValidations()

        return "Changed UserEntity (removed obsolete fields) and changed SiteProperties (prepare for hacker making sites)."
    }
}

class V2UserEntityUpdater(
    db: MongoDatabase,
    private val defaultUserService: DefaultUserService,
) {
    private val userEntityCollection = db.getCollection("userEntity")
    private val logger = mu.KotlinLogging.logger {}

    fun updateUserEntities() {
        removeObsoleteUserEntityFields()
        changeHackerManagersToHackers()
        defaultUserService.createMandatoryUsers()
    }

    private fun removeObsoleteUserEntityFields() {
        val updateUserEntityFields = Updates.combine(
            Updates.unset("email"),
            Updates.unset("gmNote"),
        )
        val updateResult = userEntityCollection.updateMany(matchAllDocuments, updateUserEntityFields)
        logger.info { "Updated ${updateResult.modifiedCount} UserEntity documents" }
    }

    private fun changeHackerManagersToHackers() {
        val hackerManagers = Document().append("type", "HACKER_MANAGER")
        val updateToHacker = Updates.set("type", UserType.HACKER.name)

        val updateResult = userEntityCollection.updateMany(hackerManagers, updateToHacker)
        logger.info { "Replaced ${updateResult.modifiedCount} Hacker managers" }
    }
}


class V2SitePropertiesUpdater(
    db: MongoDatabase,
    private val siteValidationService: SiteValidationService,
    private val sitePropertiesEntityService: SitePropertiesEntityService,
    private val userEntityService: UserEntityService,
    private val currentUserService: CurrentUserService,
) {
    private val sitePropertiesCollection = db.getCollection("siteProperties")
    private val allSiteIds = sitePropertiesEntityService.findAll().map { it.siteId }
    private val logger = mu.KotlinLogging.logger {}

    fun alterSiteProperties() {

        val updateSitePropertiesFields = Updates.combine(
            Updates.unset("hackTime"),
            Updates.rename("creator", "purpose"),
            Updates.set("siteStructureOk", false),
            Updates.set("ownerUserId", "")
        )

        val updateResult = sitePropertiesCollection.updateMany(matchAllDocuments, updateSitePropertiesFields)
        logger.info { "Updated ${updateResult.modifiedCount} SiteProperty documents" }
    }

    fun setSiteOwnerIds() {
        val systemUser = userEntityService.getSystemUser()
        val gmUser = userEntityService.getByName("gm")

        allSiteIds.forEach {
            val siteProperties = sitePropertiesEntityService.getBySiteId(it)

            if (siteProperties.name == TUTORIAL_TEMPLATE_NAME) {
                // This is the tutorial template, to be owned by System.
                updateSiteOwner(siteProperties, systemUser.id)

            } else if (siteProperties.name.startsWith(TUTORIAL_INSTANCE_PREFIX)) {
                // This is a tutorial instance for a specific user.
                updateSiteOwnerForTutorialInstance(siteProperties)

            } else {
                // These are regular sites, to be owned by GM.
                updateSiteOwner(siteProperties, gmUser.id)
            }
        }
    }

    private fun updateSiteOwner(siteProperties: SiteProperties, ownerUserId: String) {
        sitePropertiesEntityService.save(siteProperties.copy(ownerUserId = ownerUserId))
    }

    private fun updateSiteOwnerForTutorialInstance(siteProperties: SiteProperties) {
        val siteUserId = siteProperties.name.substringAfter(TUTORIAL_INSTANCE_PREFIX)
        val ownerUserEntity = userEntityService.searchById(siteUserId)
        val ownerUserId = ownerUserEntity?.id ?: userEntityService.getSystemUser().id
        updateSiteOwner(siteProperties, ownerUserId)
    }

    fun updateSiteValidations() {
        allSiteIds.forEach {
            val siteProperties = sitePropertiesEntityService.getBySiteId(it)

            /** Site validation depends on current user, see [SiteValidationService.validateSiteCreator] */
            try {
                val user = userEntityService.getById(siteProperties.ownerUserId)
                currentUserService.set(user)
                siteValidationService.validate(it, true)
            } finally {
                currentUserService.remove()
            }
        }
    }
}
