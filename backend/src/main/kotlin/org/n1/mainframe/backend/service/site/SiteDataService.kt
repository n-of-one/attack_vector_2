package org.n1.mainframe.backend.service.site

import org.n1.mainframe.backend.model.site.Site
import org.n1.mainframe.backend.model.ui.EditSiteData
import org.n1.mainframe.backend.model.ui.NotyMessage
import org.n1.mainframe.backend.model.ui.ValidationException
import org.n1.mainframe.backend.repo.SiteRepo
import org.n1.mainframe.backend.service.StompService
import org.springframework.stereotype.Service
import java.security.Principal

@Service
class SiteDataService(
        val siteRepo: SiteRepo,
        val stompService: StompService
) {

    fun update(command: EditSiteData, principal: Principal) {
        val site = siteRepo.findOne(command.siteId) ?: throw IllegalStateException("Site ${command.siteId} not found")
        val value = command.value

        try {
            when (command.field) {
                "link" -> updateLink(site, value, principal)
                "name" -> updateName(site, value)
                "description" -> site.description = value
                "gm" -> site.creator = value
                "hackTime" -> updateHackTime(site, value)
                "startNode" -> updateStartNode(site, value)
                "hackable" -> updateHackable(site, value.toBoolean())
                else -> throw IllegalArgumentException("Site field ${command.field} unknown.")
            }

            siteRepo.save(site)
            stompService.toSite(site.id, "SERVER_UPDATE_SITE_DATA", site)
        }
        catch (validationException: ValidationException) {
            stompService.toSite(site.id, "SERVER_UPDATE_SITE_DATA", site)
            throw validationException
        }
    }

    private fun updateStartNode(site: Site, input: String) {
        site.startNode = input
    }

    private fun updateHackTime(site: Site, input: String) {
        val errorText = "time must be in the format (minutes):(seconds) for example: 12:00 "
        val parts = input.split(":")
        if (parts.size != 2) throw ValidationException(errorText)
        parts[0].toIntOrNull() ?: throw ValidationException(errorText)
        parts[1].toIntOrNull() ?: throw ValidationException(errorText)

        site.hackTime = input
    }

    fun updateLink(site: Site, input: String, principal: Principal) {
        if (input.isEmpty()) {
            throw ValidationException("Site link cannot be empty.")
        }

        val properUrl = checkLink(input)

        if (!properUrl) {
            val message = NotyMessage("advice_right", "advice",
                    "The standard format for a link is:<br>" +
                            "(url):(planet)<br>" +
                            "<br>" +
                            "(url) = normal url.<br>" +
                            "(planet) = three letter planet abbreviation,<br>" +
                            "which is just the first three letters of the<br>" +
                            "planet name.<br>")
            stompService.notyToUser(principal, message)
        }

        site.link = input
    }

    fun checkLink(link: String): Boolean {
        if (link.length < 5) {
            return false
        }
        val colonIndex = link.lastIndexOf(':')
        if (colonIndex != link.length - 4) {
            return false
        }

        val planet = link.substringAfterLast(":")
        return true
    }

    fun updateHackable(site: Site, input: Boolean) {
        site.hackable = input
        val text = if (input) "Site is now hackable" else "Site no longer hackable"
        val message = NotyMessage("ok_right", "Notification", text)
        stompService.toSite(site.id, "SERVER_NOTIFICATION", message)
    }

    fun updateName(site: Site, input: String) {
        if (input.isEmpty()) throw ValidationException("Name cannot be empty")
        site.name = input
    }

}