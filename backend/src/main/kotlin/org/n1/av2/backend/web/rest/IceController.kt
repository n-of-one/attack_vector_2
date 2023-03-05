package org.n1.av2.backend.web.rest

import org.n1.av2.backend.service.layerhacking.ice.password.IceTangleService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/ice/")
class IceController(
    private val iceTangleService: IceTangleService
) {

    data class SiteNameWrapper(val siteName: String)
    data class SiteIdWrapper(val id: String)

    data class SiteListItem(val id: String, val name: String)

    @GetMapping("{iceId}")
    fun getIce(@PathVariable iceId: String): String {
        return "tangle"
    }

}