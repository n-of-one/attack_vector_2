package org.n1.mainframe.backend.repo

import org.n1.mainframe.backend.model.site.Node
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

@Repository
interface NodeRepo : PagingAndSortingRepository<Node, String> {

    fun findByIdIn(list: List<String>): List<Node>
}