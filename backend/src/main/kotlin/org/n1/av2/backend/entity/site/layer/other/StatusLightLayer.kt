package org.n1.av2.backend.entity.site.layer.other

import org.n1.av2.backend.entity.site.enums.LayerType
import org.n1.av2.backend.entity.site.layer.Layer
import org.n1.av2.backend.entity.site.layer.other.StatusLightField.*

enum class StatusLightField {
    STATUS,
    TEXT_FOR_RED,
    TEXT_FOR_GREEN
}

class StatusLightLayer(
    id: String,
    type: LayerType,
    level: Int,
    name: String,
    note: String,
    val appId: String?, // obsolete
    var status: Boolean,
    var textForRed: String,
    var textForGreen: String,

    ) : Layer(id, type, level, name, note) {

    constructor(id: String, type: LayerType, level: Int, defaultName: String, appId: String?, textForRed: String, textForGreen: String) :
            this(id, type, level, defaultName, "", null, false, textForRed, textForGreen)

    override fun updateInternal(key: String, value: String): Boolean {
        when(key) {
            STATUS.name -> status = value.toBoolean()
            TEXT_FOR_RED.name -> textForRed = value
            TEXT_FOR_GREEN.name -> textForGreen = value
            else -> return super.updateInternal(key, value)
        }
        return true
    }
}