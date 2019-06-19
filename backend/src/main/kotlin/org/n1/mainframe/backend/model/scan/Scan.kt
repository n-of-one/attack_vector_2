package org.n1.mainframe.backend.model.scan

data class Scan(

    val id: String,
//    val initiatorId: String,
    val siteId: String,
    var complete: Boolean,
//    var totalDistanceScanned: Int,
//    var startTime: ZonedDateTime,
//    var duration: Duration,
    val nodeScanById: MutableMap<String, NodeScan>
)