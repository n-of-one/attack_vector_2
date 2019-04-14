package org.n1.mainframe.backend.web.rest

import org.n1.mainframe.backend.service.EditorService
import org.n1.mainframe.backend.service.site.SiteService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/site/")
class SiteController(
        val siteService: SiteService,
        val editorService: EditorService
) {

    data class SiteData(val siteName: String)
    data class SiteDataId(val id: String)

    @PostMapping("edit")
    fun post(@RequestBody data: SiteData): SiteDataId {
        return SiteDataId(editorService.getByNameOrCreate(data.siteName).id)
    }

    data class SiteListItem(val id: String, val name:String)

    @GetMapping("")
    fun siteList(): List<SiteListItem> {
        return siteService.findAll().map { SiteListItem(id=it.id, name=it.name )}
    }

}