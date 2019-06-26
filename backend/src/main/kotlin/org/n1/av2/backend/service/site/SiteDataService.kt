package org.n1.av2.backend.service.site

import org.n1.av2.backend.model.db.site.SiteData
import org.n1.av2.backend.model.ui.EditSiteData
import org.n1.av2.backend.model.ui.ValidationException
import org.n1.av2.backend.repo.SiteDataRepo
import org.n1.av2.backend.service.ReduxActions
import org.n1.av2.backend.service.StompService
import org.n1.av2.backend.util.createId
import org.springframework.stereotype.Service

@Service
class SiteDataService(
        val siteDataRepo: SiteDataRepo,
        val stompService: StompService
) {

    fun getBySiteId(id: String): SiteData {
        return siteDataRepo.findBySiteId(id) ?: error ("No SiteData found for id: ${id}")
    }


    fun findByName(name: String): SiteData? {
        return siteDataRepo.findByName(name)
    }

    fun findAll(): MutableIterable<SiteData> {
        return siteDataRepo.findAll() ?: ArrayList()
    }

    fun update(command: EditSiteData) {
        val data = getBySiteId(command.siteId)
        val value = command.value.trim()

        try {
            when (command.field) {
                "name" -> updateName(data, value)
                "description" -> data.description = value
                "creator" -> data.creator = value
                "hackTime" -> data.hackTime = value
                "startNode" -> data.startNodeNetworkId = value
                "hackable" -> data.hackable = value.toBoolean()
                else -> throw IllegalArgumentException("Site field ${command.field} unknown.")
            }

            siteDataRepo.save(data)
            stompService.toSite(data.siteId, ReduxActions.SERVER_UPDATE_SITE_DATA, data)

        }
        catch (validationException: ValidationException) {
            stompService.toSite(data.siteId, ReduxActions.SERVER_UPDATE_SITE_DATA, data)
            throw validationException
        }
    }

    fun updateName(data: SiteData, input: String) {
        if (input.isEmpty()) throw ValidationException("Name cannot be empty")
        if (input.contains(" ")) throw ValidationException("Site name cannot contain a space")
        data.name = input
    }

    fun purgeAll() {
        siteDataRepo.deleteAll()
    }

    fun createId(): String {
        return createId("site", siteDataRepo::findById)
    }

    fun create(id: String, name: String): SiteData {
        val data = SiteData(siteId = id, name = name, hackTime = "15:00", startNodeNetworkId = "00")
        siteDataRepo.save(data)
        return data
    }
}