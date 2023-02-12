package org.n1.av2.backend.entity.site

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository


@Repository
interface SitePropertiesRepo : CrudRepository<SiteProperties, String> {
    fun findByName(name: String): SiteProperties?
    fun findBySiteId(siteId: String): SiteProperties?
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

@Repository
interface LayoutRepo : CrudRepository<Layout, String> {
    fun findBySiteId(siteId: String): Layout?
}


@Repository
interface SiteEditorStateRepo : CrudRepository<SiteEditorState, String> {
    fun findBySiteId(siteId: String): SiteEditorState?
}