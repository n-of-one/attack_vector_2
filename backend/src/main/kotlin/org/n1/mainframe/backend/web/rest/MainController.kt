package org.n1.mainframe.backend.web.rest

import org.n1.mainframe.backend.service.EditorService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class MainController(
        val editorService: EditorService
) {

    @GetMapping("/health")
    fun state(): String {
        return "ok"
    }

    @RequestMapping("purgeAll/{confirm}")
    fun get(@PathVariable("confirm") confirm: String): String {
        if (confirm != "confirm") return "please confirm."
        editorService.purgeAll()
        return "It is done."
    }
}