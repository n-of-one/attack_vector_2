package org.n1.av2.site.export

import jakarta.servlet.http.HttpServletResponse
import org.n1.av2.platform.config.ConfigItem
import org.n1.av2.platform.config.ConfigService
import org.n1.av2.platform.iam.UserPrincipal
import org.n1.av2.platform.util.FileNameUtil
import org.springframework.boot.web.servlet.error.ErrorController
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile


@Controller
class ExportImportController(
    private val configService: ConfigService,
    private val exportService: ExportService,
    private val fileNameUtil: FileNameUtil,
    private val importService: ImportService,
) : ErrorController {


    @GetMapping("/api/export/site/{siteId}", produces = ["text/json"])
    @ResponseBody
    fun export(@PathVariable siteId: String, response: HttpServletResponse): String {
        val export = exportService.export(siteId)

        val fileName = createFileName(export)
        response.addHeader("Content-Disposition", "attachment; filename=${fileName}")

        return export.json
    }

    private fun createFileName(export: SiteExportResult): String {
        val fileSafeSiteName = fileNameUtil.makeSafe(export.siteName)
        return "${export.version}-${configService.get(ConfigItem.LARP_NAME)}-${fileSafeSiteName} ${export.exportTime}.json"
    }

    @PostMapping("/api/import/site", consumes = ["multipart/form-data"])
    @ResponseBody
    fun import(@RequestParam("file") file: MultipartFile, userPrincipal: UserPrincipal): String {
        importService.import(file.inputStream.bufferedReader().use { it.readText() })
        return ""
    }

}
