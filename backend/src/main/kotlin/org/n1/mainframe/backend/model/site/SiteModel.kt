package org.n1.mainframe.backend.model.site

import com.fasterxml.jackson.annotation.JsonIgnore
import org.n1.mainframe.backend.model.service.Service
import org.n1.mainframe.backend.model.site.enums.NodeType
import org.n1.mainframe.backend.model.site.enums.ServiceType
import java.util.*

data class SiteData(
        val id: String, // this is the site-id
        var name: String,
        var description: String = "",
        var creator: String = "",
        var hackTime: String = "15:00",
        var startNodeNetworkId: String = "00",
        var hackable: Boolean = true
)

data class Node(
        val id: String,
        val siteId: String,
        val type: NodeType,
        val x: Int,
        val y: Int,
        val ice: Boolean,
        val services: MutableList<Service>,
        val networkId: String
)

data class Layout(
        val id: String,
        val nodeIds: MutableList<String> = ArrayList(),
        val connectionIds: MutableList<String> = ArrayList()
)

data class Connection(
        val id: String,
        val siteId: String,
        val fromId: String,
        val toId: String
)

data class SiteState(
        val id: String,
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

data class SiteRep(
        var node: Node,
        val nodes: List<Node>,
        val siteData: SiteData
) {

    fun findNodeByServiceId(serviceId: String): Node? {
        val node = nodes.find { node ->
            node.services.filter { service ->
                service.id == serviceId
            }.any()
        }
        return node
    }
}
