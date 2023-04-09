package org.n1.av2.backend.web.rest

import org.n1.av2.backend.entity.run.LayerStatusEntityService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/ice/")
class IceController(
    private val layerStatusEntityService: LayerStatusEntityService,
) {

    @GetMapping("{layerStatusReference}")
    fun getIce(@PathVariable layerStatusReference: String): LayerStatusEntityService.IceBasicInfo {
        return LayerStatusEntityService.IceBasicInfo("WORD_SEARCH_ICE", "wordSearch-10000-1000")
//        return layerStatusEntityService.getLayerType(layerStatusReference)
    }


}