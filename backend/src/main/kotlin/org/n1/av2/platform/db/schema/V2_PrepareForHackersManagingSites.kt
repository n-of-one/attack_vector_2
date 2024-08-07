package org.n1.av2.platform.db.schema

import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Updates
import org.bson.Document
import org.n1.av2.editor.SiteValidationService
import org.n1.av2.platform.db.MigrationStep
import org.n1.av2.platform.iam.user.CurrentUserService
import org.n1.av2.platform.iam.user.DefaultUserService
import org.n1.av2.platform.iam.user.UserEntityService
import org.n1.av2.site.entity.SitePropertiesEntityService
import org.springframework.stereotype.Component

@Component
class V2_PrepareForHackersManagingSites(
    private val siteValidationService: SiteValidationService,
    private val sitePropertiesEntityService: SitePropertiesEntityService,
    private val userEntityService: UserEntityService,
    private val defaultUserService: DefaultUserService,
    private val currentUserService: CurrentUserService,
) : MigrationStep {

    private val logger = mu.KotlinLogging.logger {}

    private val allDocuments = Document()

    override
    fun version() = 2

    override
    fun migrate(db: MongoDatabase): String {
        alterUserEntities(db)
        alterSiteProperties(db)

        val allSiteIds = sitePropertiesEntityService.findAll().map { it.siteId }

        setSiteOwnerIds(allSiteIds)
        validateSites(allSiteIds)

        return "Changed UserEntity (removed obsolete fields) and changed SiteProperties (prepare for hacker making sites)."
    }

    private fun alterUserEntities(db: MongoDatabase) {
        val userEntities = db.getCollection("userEntity")

        val updateUserEntityFields = Updates.combine(
            Updates.unset("email"),
            Updates.unset("gmNote"),
        )
        var updateResult = userEntities.updateMany(allDocuments, updateUserEntityFields)
        logger.info("Updated ${updateResult.modifiedCount} UserEntity documents")


        val hackerManagers = Document().append("type", "HACKER_MANAGER")
        val updateToHacker = Updates.set("type", "HACKER")

        updateResult = userEntities.updateMany(hackerManagers, updateToHacker)
        logger.info("Replaced ${updateResult.modifiedCount} Hacker managers")

        defaultUserService.createMandatoryUsers()
    }

    private fun alterSiteProperties(db: MongoDatabase) {
        val siteProperties = db.getCollection("siteProperties")

        val updateSitePropertiesFields = Updates.combine(
            Updates.unset("hackTime"),
            Updates.rename("creator", "purpose"),
            Updates.set("siteStructureOk", false),
            Updates.set("ownerUserId", "")
        )

        val updateResult = siteProperties.updateMany(allDocuments, updateSitePropertiesFields)
        logger.info("Updated ${updateResult.modifiedCount} SiteProperty documents")
    }

    private fun setSiteOwnerIds(allSiteIds: List<String>) {
        val systemUser = userEntityService.getSystemUser()
        val gmUser = userEntityService.getByName("gm")

        allSiteIds.forEach {
            val siteProperties = sitePropertiesEntityService.getBySiteId(it)
            if (siteProperties.name == "tutorial") {
                sitePropertiesEntityService.save(siteProperties.copy(ownerUserId = systemUser.id))
            } else if (siteProperties.name.startsWith("tutorial-")) {

                val siteUserId = siteProperties.name.substringAfter("tutorial-")
                val ownerUserEntity = userEntityService.searchById(siteUserId)
                val ownerUserId = ownerUserEntity?.id ?: systemUser.id
                sitePropertiesEntityService.save(siteProperties.copy(ownerUserId = ownerUserId))
            } else {
                sitePropertiesEntityService.save(siteProperties.copy(ownerUserId = gmUser.id))
            }
        }
    }

    private fun validateSites(allSiteIds: List<String>) {
        allSiteIds.forEach {
            val siteProperties = sitePropertiesEntityService.getBySiteId(it)
            val user = userEntityService.getById(siteProperties.ownerUserId)
            try {
                currentUserService.set(user) // Site validation depends on current user, see validateSiteCreator()
                siteValidationService.validate(it, true)
            }
            finally {
                currentUserService.remove()
            }
        }
    }
}
