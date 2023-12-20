package org.n1.av2.backend.entity.site

import org.n1.av2.backend.entity.site.enums.NodeType
import org.n1.av2.backend.entity.site.layer.Layer
import org.n1.av2.backend.entity.site.layer.ice.IceLayer
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.ZonedDateTime
import java.util.*

@Document
data class SiteProperties(
    @Id val siteId: String,
    var name: String,
    var description: String = "",
    var creator: String = "",
    var hackTime: String = "15:00",
    var startNodeNetworkId: String = "00",
    var hackable: Boolean = true,
    val shutdownEnd: ZonedDateTime?,
)

@Document
data class Node(
    @Id val id: String,
    @Indexed val siteId: String,
    val type: NodeType,
    val x: Int,
    val y: Int,
    val layers: MutableList<Layer>,
    @Indexed val networkId: String
) {
    fun getLayerById(layerId: String): Layer {
        return layers.find { it.id == layerId }!!
    }

    val ice get() = layers.any { it is IceLayer }
    val hacked get() = if (!ice) true else layers.filterIsInstance<IceLayer>().all { it.hacked }

}

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
data class SiteEditorState(
    @Id val siteId: String,
    val ok: Boolean = true,
    val messages: MutableList<SiteStateMessage> = Collections.emptyList()
)

data class SiteStateMessage(
    val type: SiteStateMessageType,
    val text: String,
    val nodeId: String? = null,
    val layerId: String? = null
)

enum class SiteStateMessageType {
    ERROR,
    INFO
}

fun findLayerById(layerId: String, nodes: List<Node>): Layer? {
    val layers = nodes.flatMap { node -> node.layers }
    return layers.find { layer -> layer.id == layerId }
}