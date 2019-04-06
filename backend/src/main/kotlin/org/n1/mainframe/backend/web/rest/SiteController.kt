package org.n1.mainframe.backend.web.rest

import org.n1.mainframe.backend.service.EditorService
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/site/")
class SiteController(
        val editorService: EditorService
) {

    @RequestMapping("{name:.+}")
    fun get(@PathVariable("name") name: String): String {
        return editorService.getByNameOrCreate(name).id
    }


}