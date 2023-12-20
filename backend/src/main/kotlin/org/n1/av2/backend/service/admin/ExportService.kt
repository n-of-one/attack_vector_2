package org.n1.av2.backend.service.admin

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import org.n1.av2.backend.service.site.SiteService
import org.springframework.stereotype.Service

@Service
class ExportService(
    private val siteService: SiteService,
    private val v1ExportHelper: V1ExportHelper,
) {

    private val objectMapper = ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .configure(SerializationFeature.INDENT_OUTPUT, true)


    fun export(siteId: String): SiteExportResult {
        val siteFull = siteService.getSiteFull(siteId)
        val siteV1 = v1ExportHelper.toV1(siteFull)

        val json = objectMapper.writeValueAsString(siteV1)

        return SiteExportResult(siteFull.siteProperties.name, V1, siteV1.exportDetails.exportTimeStamp, json)
    }

}