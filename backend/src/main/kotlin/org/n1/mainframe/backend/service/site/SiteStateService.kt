package org.n1.mainframe.backend.service.site

import org.n1.mainframe.backend.model.site.SiteState
import org.n1.mainframe.backend.repo.SiteStateRepo
import org.springframework.stereotype.Service

@Service
class SiteStateService(val repo: SiteStateRepo) {

    fun getById(siteId: String): SiteState {
        return repo.findById(siteId).orElseThrow { error("Site not found: ${siteId}") }
    }

    fun create(siteId: String) {
        val state = SiteState(siteId)
        repo.save(state)
    }

    fun save(state: SiteState) {
        repo.save(state)
    }
}