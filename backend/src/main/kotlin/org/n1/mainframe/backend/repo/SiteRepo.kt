package org.n1.mainframe.backend.repo

import org.n1.mainframe.backend.model.site.*
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository
import java.util.*


@Repository
interface SiteDataRepo : PagingAndSortingRepository<SiteData, String> {
    fun findByName(name: String): SiteData
    fun findAllByName(name: String): Collection<SiteData>
}

@Repository
interface NodeRepo : PagingAndSortingRepository<Node, String> {

    fun findBySiteId(siteId: String): List<Node>
    fun findBySiteIdAndNetworkId(siteId: String, networkId: String): List<Node>
}

@Repository
interface ConnectionRepo : PagingAndSortingRepository<Connection, String> {

    fun findBySiteId(siteId: String): List<Connection>
    fun findAllByFromId(from: String): List<Connection>
    fun findAllByToId(from: String): List<Connection>

}

@Repository
interface LayoutRepo : PagingAndSortingRepository<Layout, String>


@Repository
interface SiteStateRepo : PagingAndSortingRepository<SiteState, String> {
}
