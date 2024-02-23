package org.n1.av2.backend.entity.site

import org.n1.av2.backend.model.ui.ValidationException
import org.n1.av2.backend.service.user.CurrentUserService
import org.n1.av2.backend.util.createId
import org.springframework.stereotype.Service

@Service
class SitePropertiesEntityService(
    private val sitePropertiesRepo: SitePropertiesRepo,
    private val currentUserService: CurrentUserService,
) {

    fun existsById(siteId: String): Boolean {
        return sitePropertiesRepo.existsById(siteId)
    }

    fun getBySiteId(id: String): SiteProperties {
        return sitePropertiesRepo.findBySiteId(id) ?: error ("No SiteProperties found for id: ${id}")
    }

    fun findByName(name: String): SiteProperties? {
        return sitePropertiesRepo.findByName(name)
    }

    fun findAll(): MutableIterable<SiteProperties> {
        return sitePropertiesRepo.findAll() ?: ArrayList()
    }

    fun updateName(data: SiteProperties, input: String) {
        if (input.isEmpty()) throw ValidationException("Name cannot be empty")
        if (input.contains(" ")) throw ValidationException("Site name cannot contain a space")
        data.name = input
    }

    fun createId(): String {
        return createId("site", sitePropertiesRepo::findById)
    }

    fun create(id: String, name: String): SiteProperties {
        val currentUser = currentUserService.userEntity

        val data = SiteProperties(siteId = id, name = name, startNodeNetworkId = "00", shutdownEnd = null, ownerUserId = currentUser.id, purpose = "")
        sitePropertiesRepo.save(data)
        return data
    }

    fun save(properties: SiteProperties) {
        sitePropertiesRepo.save(properties)
    }

    fun delete(siteId: String) {
        sitePropertiesRepo.deleteById(siteId)
    }

    fun findByOwnerUserId(userId: String): List<SiteProperties> {
        return sitePropertiesRepo.findByownerUserId(userId)
    }

    fun updateSiteOk(siteId: String, siteOk: Boolean): SiteProperties {
        val siteProperties = getBySiteId(siteId)
        val updatedSiteProperties = siteProperties.copy(siteStructureOk = siteOk)
        sitePropertiesRepo.save(updatedSiteProperties)
        return updatedSiteProperties
    }
}
