package org.n1.av2.backend.entity.site

import org.n1.av2.backend.entity.site.enums.NodeType
import org.n1.av2.backend.entity.site.layer.Layer
import org.n1.av2.backend.entity.site.layer.ice.IceLayer
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.ZonedDateTime
import java.util.*

@Document
data class SiteProperties(
    @Id val siteId: String,
    var name: String,
    var description: String = "",
    var purpose: String = "",
    var ownerUserId: String,
    var startNodeNetworkId: String = "00",
    var hackable: Boolean = false,
    val shutdownEnd: ZonedDateTime? = null,
    val siteStructureOk: Boolean = true,
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

    @Transient
    val ice  = layers.any { it is IceLayer } // used in frontend as well

    @Transient
    val hacked = if (ice) true else layers.filterIsInstance<IceLayer>().all { it.hacked }

    @Transient
    val unhackedIce = layers.filterIsInstance<IceLayer>().any { !it.hacked }

}

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