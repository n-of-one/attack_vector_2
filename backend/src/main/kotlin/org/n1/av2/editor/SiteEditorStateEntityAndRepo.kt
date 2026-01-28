package org.n1.av2.editor

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Document
data class SiteEditorState(
    @Id val siteId: String,
    val messages: MutableList<SiteStateMessage> = Collections.emptyList()
)

data class SiteStateMessage(
    val type: SiteStateMessageType,
    val text: String,
    val nodeId: String? = null,
    val layerId: String? = null
)

enum class SiteStateMessageType {
    ERROR,
    INFO
}

@Repository
interface SiteEditorStateRepo : CrudRepository<SiteEditorState, String> {
    fun findBySiteId(siteId: String): SiteEditorState?
}

