package org.n1.av2.backend.entity.site

import org.n1.av2.backend.model.ui.ValidationException
import org.n1.av2.backend.util.createId
import org.springframework.stereotype.Service

@Service
class SitePropertiesEntityService(
    private val sitePropertiesRepo: SitePropertiesRepo,
) {

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
        val data = SiteProperties(siteId = id, name = name, hackTime = "15:00", startNodeNetworkId = "00")
        sitePropertiesRepo.save(data)
        return data
    }

    fun save(properties: SiteProperties) {
        sitePropertiesRepo.save(properties)
    }

    fun delete(siteId: String) {
        sitePropertiesRepo.deleteById(siteId)
    }
}