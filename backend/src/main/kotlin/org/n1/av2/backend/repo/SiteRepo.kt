package org.n1.av2.backend.repo

import org.n1.av2.backend.model.db.site.*
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository


@Repository
interface SiteDataRepo : CrudRepository<SiteData, String> {
    fun findByName(name: String): SiteData?
    fun findBySiteId(siteId: String): SiteData?
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
interface SiteStateRepo : CrudRepository<SiteState, String> {
    fun findBySiteId(siteId: String): SiteState?
}