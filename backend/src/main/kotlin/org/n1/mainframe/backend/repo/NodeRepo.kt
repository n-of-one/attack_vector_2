package org.n1.mainframe.backend.repo

import org.n1.mainframe.backend.model.site.Node
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

@Repository
interface NodeRepo : PagingAndSortingRepository<Node, String> {

    fun findBySiteId(siteId: String): List<Node>
    fun findBySiteIdAndNetworkId(siteId: String, networkId: String): List<Node>
}