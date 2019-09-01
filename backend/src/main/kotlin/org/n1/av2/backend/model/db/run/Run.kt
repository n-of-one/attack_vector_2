package org.n1.av2.backend.model.db.run

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed


data class LayerStatus (
        @Id val id: String,
        val layerId: String,
        @Indexed val runId: String,
        var hacked: Boolean,
        val hackedBy: MutableList<String>)

data class NodeStatus (
        @Id val id: String,
        val nodeId: String,
        @Indexed val runId: String,
        var hacked: Boolean
)



//data class Run (
//        @Id val id: String,
//        val siteId: String,
//        var startTime: ZonedDateTime,
//        var endTime: ZonedDateTime
//
//)