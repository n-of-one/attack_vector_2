package org.n1.av2.backend.web.rest

import org.n1.av2.backend.entity.site.SitePropertiesEntityService
import org.n1.av2.backend.service.EditorService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/site/")
class SiteController(
    val sitePropertiesEntityService: SitePropertiesEntityService,
    val editorService: EditorService
) {

    data class SiteName(val siteName: String)
    data class SiteDataId(val id: String)

    @PostMapping("edit")
    fun post(@RequestBody data: SiteName): SiteDataId {
        return SiteDataId(editorService.getByNameOrCreate(data.siteName))
    }

    data class SiteListItem(val id: String, val name: String)

    @GetMapping("")
    fun siteList(): List<SiteListItem> {
        return sitePropertiesEntityService.findAll().map { SiteListItem(id = it.siteId, name = it.name) }
    }

}