package org.n1.av2.backend.repo

import org.n1.av2.backend.model.db.site.*
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository


@Repository
interface SiteDataRepo : PagingAndSortingRepository<SiteData, String> {
    fun findByName(name: String): SiteData?
    fun findBySiteId(siteId: String): SiteData?
}

@Repository
interface NodeRepo : PagingAndSortingRepository<Node, String> {
    fun findBySiteId(siteId: String): List<Node>
    fun findBySiteIdAndNetworkId(siteId: String, networkId: String): Node?
    fun findBySiteIdAndNetworkIdIgnoreCase(siteId: String, networkId: String): Node?
}

@Repository
interface ConnectionRepo : PagingAndSortingRepository<Connection, String> {
    fun findBySiteId(siteId: String): List<Connection>
    fun findAllByFromIdOrToId(from: String, to: String): List<Connection>

}

@Repository
interface LayoutRepo : PagingAndSortingRepository<Layout, String> {
    fun findBySiteId(siteId: String): Layout?
}


@Repository
interface SiteStateRepo : PagingAndSortingRepository<SiteState, String> {
    fun findBySiteId(siteId: String): SiteState?
}