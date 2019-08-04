package org.n1.av2.backend.model.db.run

import org.springframework.data.annotation.Id


// FIXME: primary key must be connected to run. Currently there is only one status holder per service over all runs.
data class ServiceStatusHolder (
        @Id val serviceId: String,
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