package org.n1.av2.backend.model.db.run

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

@Document
data class HackerPosition(
        @Id val runId: String,
        @Indexed val userId: String,
        val siteId: String,
        val currentNodeId: String,
        val previousNodeId: String,
        val inTransit: Boolean = false

)