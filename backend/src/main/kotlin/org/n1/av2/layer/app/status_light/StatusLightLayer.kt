package org.n1.av2.layer.app.status_light

import org.n1.av2.layer.Layer
import org.n1.av2.layer.app.status_light.StatusLightField.CURRENT_OPTION
import org.n1.av2.layer.app.status_light.StatusLightField.SWITCH_LABEL
import org.n1.av2.site.entity.enums.LayerType

enum class StatusLightField {
    CURRENT_OPTION, SWITCH_LABEL
}

const val TEXT_FOR = "TEXT_FOR_"
const val COLOR_FOR = "COLOR_FOR_"

class StatusLightOption(
    var text: String,
    var color: String
)

class StatusLightLayer(
    id: String,
    type: LayerType,
    level: Int,
    name: String,
    note: String,
    var switchLabel: String,
    var currentOption: Int,
    var options: MutableList<StatusLightOption>,

    ) : Layer(id, type, level, name, note) {

    constructor(id: String, type: LayerType, level: Int, defaultName: String, switchLabel: String, textForOption1: String, textForOption2: String) :
        this(
            id, type, level, defaultName, "", switchLabel, 0,
            listOf(
                StatusLightOption(text = textForOption1, color = "red"),
                StatusLightOption(text = textForOption2, color = "lime")
            ).toMutableList()
        )

    constructor(id: String, toClone: StatusLightLayer) :
        this(id, LayerType.STATUS_LIGHT, toClone.level, toClone.name, toClone.note, toClone.switchLabel, toClone.currentOption, toClone.options)

    override fun updateInternal(key: String, value: String): Boolean {

        if (key == SWITCH_LABEL.name) {
            this.switchLabel = value
            return true
        }

        if (key == CURRENT_OPTION.name) {
            val newOption = value.toInt()
            checkOptionIndex(newOption)
            this.currentOption = newOption
            return true
        }

        if (key.startsWith(TEXT_FOR)) {
            val optionIndex = key.substringAfter(TEXT_FOR).toInt()
            checkOptionIndex(optionIndex)
            options[optionIndex].text = value
            return true
        } else if (key.startsWith(COLOR_FOR)) {
            val optionIndex = key.substringAfter(COLOR_FOR).toInt()
            checkOptionIndex(optionIndex)
            options[optionIndex].color = value
            return true
        } else {
            return super.updateInternal(key, value)
        }
    }

    private fun checkOptionIndex(index: Int) {
        if (index < 0 || index >= options.size) {
            throw IllegalArgumentException("Invalid option index: $index with options size: ${options.size}")
        }
    }
}
