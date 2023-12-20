package org.n1.av2.backend.web.rest

import org.n1.av2.backend.engine.UserTaskRunner
import org.n1.av2.backend.entity.site.SitePropertiesEntityService
import org.n1.av2.backend.model.iam.UserPrincipal
import org.n1.av2.backend.service.site.EditorService
import org.n1.av2.backend.service.site.SiteService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/site/")
class SiteController(
    val sitePropertiesEntityService: SitePropertiesEntityService,
    val editorService: EditorService,
    val userTaskRunner: UserTaskRunner,
    val siteService: SiteService,
) {

    data class SiteNameWrapper(val siteName: String)
    data class SiteIdWrapper(val id: String)

    @PostMapping("edit")
    fun post(@RequestBody wrapper: SiteNameWrapper): SiteIdWrapper {
        val siteId = editorService.getByNameOrCreate(wrapper.siteName)
        return SiteIdWrapper(siteId)
    }

    data class DeleteResponse(val success: Boolean = true)
    @DeleteMapping("{id}")
    fun post(@PathVariable id: String, principal: UserPrincipal): DeleteResponse {
        userTaskRunner.runTask(principal) { editorService.deleteSite(id, principal) }
        return DeleteResponse()
    }

}
