package org.n1.mainframe.backend.model.service

import org.n1.mainframe.backend.model.site.Service
import org.n1.mainframe.backend.model.site.enums.ServiceType
import java.util.*

const val KEY_NAME = "name"
const val KEY_GM_NOTE = "gmNote"

class OsService(
        id: String,
        layer: Int = 0,
        type: ServiceType = ServiceType.OS,
        data: Map<String, String> = Collections.emptyMap()
) : Service(id, type, layer, data) {

    val name: String
        get() {
            return data[KEY_NAME] ?: ""
        }

    val gmNote: String
        get() {
            return data[KEY_GM_NOTE] ?: ""
        }
}