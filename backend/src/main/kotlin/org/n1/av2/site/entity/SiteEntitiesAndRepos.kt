package org.n1.av2.site.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import org.n1.av2.layer.Layer
import org.n1.av2.layer.ice.common.IceLayer
import org.n1.av2.site.entity.enums.NodeType
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.time.Duration
import java.time.ZonedDateTime

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
    val alertnessTimerAdjustment: Duration? = null,
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

    @get:JsonProperty("hacked")
    val hacked: Boolean get() = if (!ice) true else layers.filterIsInstance<IceLayer>().all { it.hacked }

    @JsonIgnore
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


fun findLayerById(layerId: String, nodes: List<Node>): Layer? {
    val layers = nodes.flatMap { node -> node.layers }
    return layers.find { layer -> layer.id == layerId }
}

@Repository
interface SitePropertiesRepo : CrudRepository<SiteProperties, String> {
    fun findByName(name: String): SiteProperties?
    fun findBySiteId(siteId: String): SiteProperties?
    fun findByownerUserId(ownerId: String): List<SiteProperties>
}

@Repository
interface NodeRepo : CrudRepository<Node, String> {
    fun findBySiteId(siteId: String): List<Node>
    fun findBySiteIdAndNetworkId(siteId: String, networkId: String): Node?
    fun findBySiteIdAndNetworkIdIgnoreCase(siteId: String, networkId: String): Node?
}

@Repository
interface ConnectionRepo : CrudRepository<Connection, String> {
    fun findBySiteId(siteId: String): List<Connection>
    fun findAllByFromIdOrToId(from: String, to: String): List<Connection>

}

