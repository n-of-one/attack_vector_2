package org.n1.mainframe.backend.web.rest

import org.n1.mainframe.backend.service.EditorService
import org.n1.mainframe.backend.service.site.SiteService
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/site/")
class SiteController(
        val editorService: EditorService
) {

    @RequestMapping("{link:.+}")
    fun get(@PathVariable("link") link: String): String {
        return editorService.getByLinkOrCreate(link).id
    }


}