package org.n1.av2.backend.util.dbschema

import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Updates
import org.bson.Document
import org.n1.av2.backend.entity.site.SitePropertiesEntityService
import org.n1.av2.backend.entity.user.UserEntityService
import org.n1.av2.backend.service.site.SiteValidationService
import org.springframework.stereotype.Component

@Component
class V2_PrepareForHackersManagingSites(
    private val siteValidationService: SiteValidationService,
    private val sitePropertiesEntityService: SitePropertiesEntityService,
    private val userEntityService: UserEntityService,
) : MigrationStep {

    private val logger = mu.KotlinLogging.logger {}

    private val allDocuments = Document()

    override
    fun version() = 2

    override
    fun migrate(db: MongoDatabase): String {
        alterUserEntities(db)
        alterSiteProperties(db)
        setNewSiteProperties()

        return "Changed UserEntity (removed obsolete fields) and changed SiteProperties (prepare for hacker making sites)."
    }

    private fun alterUserEntities(db: MongoDatabase) {
        val userEntities = db.getCollection("userEntity")

        val updateUserEntityFields = Updates.combine(
            Updates.unset("email"),
            Updates.unset("gmNote"),
        )
        val updateResult = userEntities.updateMany(allDocuments, updateUserEntityFields)
        logger.info("Updated ${updateResult.modifiedCount} UserEntity documents")

        userEntityService.createMandatoryUsers()
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

    private fun setNewSiteProperties() {
        val systemUser = userEntityService.getSystemUser()
        val gmUser = userEntityService.getByName("gm")

        val allSiteIds = sitePropertiesEntityService.findAll().map { it.siteId }
        allSiteIds.forEach { siteValidationService.validate(it, true) }
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
}