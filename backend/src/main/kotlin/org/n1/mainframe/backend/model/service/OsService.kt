package org.n1.mainframe.backend.model.service

import com.fasterxml.jackson.annotation.JsonIgnore
import org.n1.mainframe.backend.model.site.Service
import org.n1.mainframe.backend.model.site.enums.ServiceType

const val KEY_NAME = "name"
const val KEY_GM_NOTE = "gmNote"

class OsService(
        id: String,
        layer: Int = 0,
        type: ServiceType = ServiceType.OS,
        data: MutableMap<String, String> = HashMap(0)
) : Service(id, type, layer, data) {

    val name: String
        @JsonIgnore
        get() {
            return data[KEY_NAME] ?: ""
        }

    val gmNote: String
        @JsonIgnore
        get() {
            return data[KEY_GM_NOTE] ?: ""
        }
}