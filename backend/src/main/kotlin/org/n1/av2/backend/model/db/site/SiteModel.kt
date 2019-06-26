package org.n1.av2.backend.model.db.site

import org.n1.av2.backend.model.db.service.Service
import org.n1.av2.backend.model.db.site.enums.NodeType
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.util.*

@Document
data class SiteData(
        @Id val siteId: String,
        var name: String,
        var description: String = "",
        var creator: String = "",
        var hackTime: String = "15:00",
        var startNodeNetworkId: String = "00",
        var hackable: Boolean = true
)

@Document
data class Node(
        @Id val id: String,
        @Indexed val siteId: String,
        val type: NodeType,
        val x: Int,
        val y: Int,
        val ice: Boolean,
        val services: MutableList<Service>,
        @Indexed val networkId: String
)

@Document
data class Layout(
        @Id val siteId: String,
        val nodeIds: MutableList<String> = ArrayList(),
        val connectionIds: MutableList<String> = ArrayList()
)

@Document
data class Connection(
        @Id val id: String,
        @Indexed val siteId: String,
        val fromId: String,
        val toId: String
)

@Document
data class SiteState(
        @Id val siteId: String,
        val ok: Boolean = true,
        val messages: MutableList<SiteStateMessage> = Collections.emptyList()
)

data class SiteStateMessage(
        val type: SiteStateMessageType,
        val text: String,
        val nodeId: String? = null,
        val serviceId: String? = null
)

enum class SiteStateMessageType {
    ERROR,
    INFO
}

