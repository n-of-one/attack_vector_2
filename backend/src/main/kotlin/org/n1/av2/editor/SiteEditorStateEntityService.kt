package org.n1.av2.editor

import org.springframework.stereotype.Service

@Service
class SiteEditorStateEntityService(
    private val repo: SiteEditorStateRepo
) {

    fun getById(siteId: String): SiteEditorState {
        return repo.findBySiteId(siteId) ?: error("Site not found: ${siteId}")
    }

    fun create(siteId: String) {
        val state = SiteEditorState(siteId = siteId)
        repo.save(state)
    }

    fun save(state: SiteEditorState) {
        repo.save(state)
    }

    fun delete(siteId: String) {
        repo.deleteById(siteId)
    }
}
