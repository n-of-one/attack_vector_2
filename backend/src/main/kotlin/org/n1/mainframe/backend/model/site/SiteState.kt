package org.n1.mainframe.backend.model.site

import java.util.*

data class SiteState(
        val id: String,
        val ok: Boolean = true,
        val messages: MutableList<SiteStateMessage> = Collections.emptyList()
        )

data class SiteStateMessage(
        val type: SiteStateMessageType,
        val text: String
)

enum class SiteStateMessageType {
    ERROR,
    INFO
}