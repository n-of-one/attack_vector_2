package org.n1.mainframe.backend.repo

import org.n1.mainframe.backend.model.site.Site
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

@Repository
interface SiteRepo : PagingAndSortingRepository<Site, String> {
    fun findByName(name: String): Site
    fun findAllByName(name: String): Collection<Site>

//    val sites = ArrayList<Site>()
//
//    fun getById(id: Int): Site {
//        return sites.find { it.id == id } ?: error("There is no site with id: ${id}")
//    }
//
//    fun getByLink(link: String): Site {
//        return sites.find { it.link == link } ?: error("There is no site with link: ${link}")
//    }
//
//    fun add(site: Site) {
//        this.sites.add(site)
//    }
//
//    fun purge() {
//        sites.clear()
//    }

}