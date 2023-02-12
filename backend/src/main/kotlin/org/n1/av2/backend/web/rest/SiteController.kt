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

    data class SiteNameWrapper(val siteName: String)
    data class SiteIdWrapper(val id: String)

    @PostMapping("edit")
    fun post(@RequestBody wrapper: SiteNameWrapper): SiteIdWrapper {
        val siteId = editorService.getByNameOrCreate(wrapper.siteName)
        return SiteIdWrapper(siteId)
    }

    data class SiteListItem(val id: String, val name: String)

    @GetMapping("")
    fun siteList(): List<SiteListItem> {
        return sitePropertiesEntityService.findAll().map { SiteListItem(id = it.siteId, name = it.name) }
    }

}