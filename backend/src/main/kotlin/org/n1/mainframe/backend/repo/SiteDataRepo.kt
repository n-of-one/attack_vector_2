package org.n1.mainframe.backend.repo

import org.n1.mainframe.backend.model.site.SiteData
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

@Repository
interface SiteDataRepo : PagingAndSortingRepository<SiteData, String> {
    fun findByName(name: String): SiteData
    fun findAllByName(name: String): Collection<SiteData>
}