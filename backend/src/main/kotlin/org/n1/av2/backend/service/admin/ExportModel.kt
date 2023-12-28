package org.n1.av2.backend.service.admin


class SiteExportResult(
    val siteName: String,
    val version: String,
    val exportTime: String,
    val json: String
)

class ExportDetails(
    val version: String,
    val exportTimeStamp: String,
)

