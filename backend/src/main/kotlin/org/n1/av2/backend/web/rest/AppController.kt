package org.n1.av2.backend.web.rest

import org.n1.av2.backend.entity.app.StatusLightEntity
import org.n1.av2.backend.entity.site.enums.LayerType
import org.n1.av2.backend.service.layerhacking.app.status_light.StatusLightService
import org.n1.av2.backend.service.layerhacking.ice.IceService
import org.n1.av2.backend.util.createPathWithQuery
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/api/app/")
class AppController(
    private val iceService: IceService,
    private val statusLightService: StatusLightService,
) {

    /** See UrlPaths.md for an explanation on how this works */

    class AppResponse(val action: String, val type: LayerType?, val iceId: String?, val nextUrl: String?)
    @GetMapping("{appId}")
    fun getIce(
        @PathVariable appId: String,
        @RequestParam("level") level: Int?,
        @RequestParam("hacking") hacking: Boolean?,
    ): AppResponse {

        val statusLight: StatusLightEntity = statusLightService.findById(appId)
        val layerId = statusLight.layerId

        val protectionInfo = iceService.checkIceProtectingApp(appId, level, layerId) ?: return AppResponse(
            "app", null, null, null
        )

        val nextUrl = createPathWithQuery(protectionInfo.path, mapOf("hacking" to hacking, "level" to protectionInfo.level))
        return AppResponse("auth", protectionInfo.type, protectionInfo.iceId, nextUrl)
    }
}
