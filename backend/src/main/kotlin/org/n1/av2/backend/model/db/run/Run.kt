package org.n1.av2.backend.model.db.run

import org.springframework.data.annotation.Id


data class ServiceStatusHolder (
        @Id val id: String,
        val serviceId: String,
        val runId: String,
        var hacked: Boolean,
        val hackedBy: MutableList<String>,
        val status: ServiceStatus)

abstract class ServiceStatus


//data class Run (
//        @Id val id: String,
//        val siteId: String,
//        var startTime: ZonedDateTime,
//        var endTime: ZonedDateTime
//
//)