package org.n1.av2.layer.app.status_light

import org.n1.av2.layer.Layer
import org.n1.av2.layer.app.status_light.StatusLightField.*
import org.n1.av2.site.entity.enums.LayerType

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
    val appId: String? = null, // obsolete
    var status: Boolean,
    var textForRed: String,
    var textForGreen: String,

    ) : Layer(id, type, level, name, note) {

    constructor(id: String, type: LayerType, level: Int, defaultName: String, appId: String?, textForRed: String, textForGreen: String) :
            this(id, type, level, defaultName, "", appId, false, textForRed, textForGreen)

    constructor(id: String, toClone: StatusLightLayer) :
            this(id, LayerType.STATUS_LIGHT, toClone.level, toClone.name, toClone.note, toClone.appId, toClone.status, toClone.textForRed, toClone.textForGreen)

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
