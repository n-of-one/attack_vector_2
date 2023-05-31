package org.n1.av2.backend.entity.site.layer.other

import org.n1.av2.backend.entity.site.enums.LayerType
import org.n1.av2.backend.entity.site.layer.Layer

const val STATUS = "status"
const val TEXT_FOR_RED = "textForRed"
const val TEXT_FOR_GREEN = "textForGreen"


class StatusLightLayer(
    id: String,
    type: LayerType,
    level: Int,
    name: String,
    note: String,
    val appId: String,
    var status: Boolean,
    var textForRed: String,
    var textForGreen: String,

    ) : Layer(id, type, level, name, note) {

    constructor(id: String, type: LayerType, level: Int, defaultName: String, appId: String, textForRed: String, textForGreen: String) :
            this(id, type, level, defaultName, "", appId, false, textForRed, textForGreen)

    override fun updateInternal(key: String, value: String): Boolean {
        when(key) {
            STATUS -> status = value.toBoolean()
            TEXT_FOR_RED -> textForRed = value
            TEXT_FOR_GREEN -> textForGreen = value
            else -> return super.updateInternal(key, value)
        }
        return true
    }
}