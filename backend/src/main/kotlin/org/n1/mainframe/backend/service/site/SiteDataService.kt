package org.n1.mainframe.backend.service.site

import org.n1.mainframe.backend.model.site.Layout
import org.n1.mainframe.backend.model.site.SiteData
import org.n1.mainframe.backend.model.ui.site.command.EditSiteData
import org.n1.mainframe.backend.model.ui.NotyMessage
import org.n1.mainframe.backend.model.ui.ValidationException
import org.n1.mainframe.backend.repo.SiteDataRepo
import org.n1.mainframe.backend.service.ReduxActions
import org.n1.mainframe.backend.service.StompService
import org.n1.mainframe.backend.util.createId
import org.springframework.stereotype.Service
import java.security.Principal

@Service
class SiteDataService(
        val siteDataRepo: SiteDataRepo,
        val stompService: StompService
) {

    fun findByName(name: String): SiteData? {
        val all = siteDataRepo.findAllByName(name)
        return if (all.size == 1) all.first()
        else null
    }
    fun findAll(): MutableIterable<SiteData> {
        return siteDataRepo.findAll() ?: ArrayList()
    }


    fun update(command: EditSiteData, principal: Principal) {
        val data = siteDataRepo.findById(command.siteId).orElseThrow { throw IllegalStateException("Site ${command.siteId} not found") }
        val value = command.value

        try {
            when (command.field) {
                "name" -> updateName(data, value)
                "description" -> data.description = value
                "gm" -> data.creator = value
                "hackTime" -> updateHackTime(data, value)
                "startNode" -> updateStartNode(data, value)
                "hackable" -> updateHackable(data, value.toBoolean())
                else -> throw IllegalArgumentException("Site field ${command.field} unknown.")
            }

            siteDataRepo.save(data)
            stompService.toSite(data.id, ReduxActions.SERVER_UPDATE_SITE_DATA, data)
        }
        catch (validationException: ValidationException) {
            stompService.toSite(data.id, ReduxActions.SERVER_UPDATE_SITE_DATA, data)
            throw validationException
        }
    }

    private fun updateStartNode(data: SiteData, input: String) {
        data.startNodeId = input
    }

    private fun updateHackTime(data: SiteData, input: String) {
        val errorText = "time must be in the format (minutes):(seconds) for example: 12:00 "
        val parts = input.split(":")
        if (parts.size != 2) throw ValidationException(errorText)
        parts[0].toIntOrNull() ?: throw ValidationException(errorText)
        parts[1].toIntOrNull() ?: throw ValidationException(errorText)

        data.hackTime = input
    }


    fun updateHackable(data: SiteData, input: Boolean) {
        data.hackable = input
        val text = if (input) "Site is now hackable" else "Site no longer hackable"
        val message = NotyMessage("ok_right", "Notification", text)
        stompService.toSite(data.id, ReduxActions.SERVER_NOTIFICATION, message)
    }

    fun updateName(data: SiteData, input: String) {
        if (input.isEmpty()) throw ValidationException("Name cannot be empty")
        if (input.contains(" ")) throw ValidationException("Site name cannot contain a space")
        data.name = input
    }

    fun purgeAll() {
        siteDataRepo.deleteAll()
    }

    fun getById(id: String): SiteData {
        return siteDataRepo.findById(id).orElseThrow { throw IllegalArgumentException("No site found for id: ${id}") }
    }

    fun createId(): String {
        return createId("site", siteDataRepo::findById)
    }

    fun create(id: String, name: String): SiteData {
        val data = SiteData(id = id, name = name, hackTime = "15:00", startNodeId = "00")
        siteDataRepo.save(data)
        return data
    }

}