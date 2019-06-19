package org.n1.av2.backend.service.site

import org.n1.av2.backend.model.db.site.SiteState
import org.n1.av2.backend.repo.SiteStateRepo
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

    fun purgeAll() {
        repo.deleteAll()
    }
}