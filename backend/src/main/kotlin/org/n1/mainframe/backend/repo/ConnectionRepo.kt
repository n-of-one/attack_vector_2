package org.n1.mainframe.backend.repo

import org.n1.mainframe.backend.model.site.Connection
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

@Repository
interface ConnectionRepo : PagingAndSortingRepository<Connection, String> {

    fun findByIdIn(list: List<String>): List<Connection>
    fun findAllByFromId(from: String): List<Connection>
    fun findAllByToId(from: String): List<Connection>

}