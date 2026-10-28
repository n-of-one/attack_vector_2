package org.n1.av2.site.export

import org.n1.av2.site.SiteService
import org.springframework.stereotype.Service
import tools.jackson.databind.SerializationFeature
import tools.jackson.databind.json.JsonMapper

@Service
class ExportService(
    private val siteService: SiteService,
    private val v2Exporter: V2Exporter,
) {

    private val jsonMapper = JsonMapper.builder()
        .enable(SerializationFeature.INDENT_OUTPUT)
        .build()


    fun export(siteId: String): SiteExportResult {
        val siteFull = siteService.getSiteFull(siteId)
        val siteV1 = v2Exporter.toV2(siteFull)

        val json = jsonMapper.writeValueAsString(siteV1)

        return SiteExportResult(siteFull.siteProperties.name, V1, siteV1.exportDetails.exportTimeStamp, json)
    }

}
