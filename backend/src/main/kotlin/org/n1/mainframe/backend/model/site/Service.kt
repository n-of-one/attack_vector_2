package org.n1.mainframe.backend.model.site

import org.n1.mainframe.backend.model.site.enums.ServiceType

open class Service(
        val id: String,
        val type: ServiceType,
        val layer: Int,
        val data: MutableMap<String, String>
)