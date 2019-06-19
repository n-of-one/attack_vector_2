package org.n1.av2.backend.web.rest

import org.n1.av2.backend.service.EditorService
import org.n1.av2.backend.service.site.SiteDataService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/site/")
class SiteController(
        val siteDataService: SiteDataService,
        val editorService: EditorService
) {

    data class SiteName(val siteName: String)
    data class SiteDataId(val id: String)

    @PostMapping("edit")
    fun post(@RequestBody data: SiteName): SiteDataId {
        return SiteDataId(editorService.getByNameOrCreate(data.siteName))
    }

    data class SiteListItem(val id: String, val name: String)

    @PostMapping("")
    fun siteList(): List<SiteListItem> {
        return siteDataService.findAll().map { SiteListItem(id = it.id, name = it.name) }
    }

}